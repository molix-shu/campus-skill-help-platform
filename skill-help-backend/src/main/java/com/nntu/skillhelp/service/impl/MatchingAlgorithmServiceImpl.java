package com.nntu.skillhelp.service.impl;

import com.nntu.skillhelp.entity.Demand;
import com.nntu.skillhelp.entity.Skill;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.service.DemandService;
import com.nntu.skillhelp.service.MatchingAlgorithmService;
import com.nntu.skillhelp.service.SkillService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingAlgorithmServiceImpl implements MatchingAlgorithmService {

    private final DemandService demandService;
    private final SkillService skillService;
    private final UserService userService;

    @Override
    public List<Skill> recommendSkills(Long currentUserId, List<Skill> allActiveSkills) {
        if (currentUserId == null) {
            return fallbackSortSkills(allActiveSkills); // 未登录回退到基础排序
        }

        // 1. 获取当前用户正在寻找中的需求 (我的需求)
        List<Demand> myDemands = demandService.lambdaQuery()
                .eq(Demand::getUserId, currentUserId)
                .eq(Demand::getStatus, 1) // 寻找中
                .list();

        if (myDemands == null || myDemands.isEmpty()) {
            return fallbackSortSkills(allActiveSkills); // 没有发布需求，回退
        }

        // 2. 双向融合排序：计算每个目标技能与我的各个需求的最高匹配分
        Map<Skill, Double> scoreMap = new HashMap<>();
        for (Skill skill : allActiveSkills) {
            double maxScore = 0.0;
            // 获取技能方信用分加权
            User publisher = userService.getById(skill.getUserId());
            double creditBonus = (publisher != null && publisher.getCreditScore() != null) ? publisher.getCreditScore() * 0.1 : 10.0;

            for (Demand myDemand : myDemands) {
                double score = calculateMatchScore(
                        skill.getCategoryId(), skill.getTags(), skill.getServiceMethod(), skill.getTitle(), skill.getDescription(), skill.getCreateTime(),
                        myDemand.getCategoryId(), myDemand.getTags(), myDemand.getServiceMethod(), myDemand.getTitle(), myDemand.getDescription()
                );
                maxScore = Math.max(maxScore, score);
            }
            scoreMap.put(skill, maxScore + creditBonus); // 综合得分
        }

        // 按得分倒序，得分相同按时间倒序
        return allActiveSkills.stream()
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(scoreMap.getOrDefault(b, 0.0), scoreMap.getOrDefault(a, 0.0));
                    if (scoreCompare != 0) return scoreCompare;
                    if (a.getCreateTime() != null && b.getCreateTime() != null) return b.getCreateTime().compareTo(a.getCreateTime());
                    return 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Demand> recommendDemands(Long currentUserId, List<Demand> allActiveDemands) {
        if (currentUserId == null) {
            return fallbackSortDemands(allActiveDemands);
        }

        // 1. 获取当前用户发布过的技能（我的技能，代表供给侧画像）
        List<Skill> mySkills = skillService.lambdaQuery()
                .eq(Skill::getUserId, currentUserId)
                .eq(Skill::getStatus, 1) // 正常接单中
                .list();

        if (mySkills == null || mySkills.isEmpty()) {
            return fallbackSortDemands(allActiveDemands); // 如果我没发过技能，无法精确匹配，回退常规排序
        }

        Map<Demand, Double> scoreMap = new HashMap<>();
        for (Demand demand : allActiveDemands) {
            double maxScore = 0.0;
            User publisher = userService.getById(demand.getUserId());
            double creditBonus = (publisher != null && publisher.getCreditScore() != null) ? publisher.getCreditScore() * 0.1 : 10.0;

            for (Skill mySkill : mySkills) {
                // 供给方视角：看需求是否适合我的技能
                double score = calculateMatchScore(
                        demand.getCategoryId(), demand.getTags(), demand.getServiceMethod(), demand.getTitle(), demand.getDescription(), demand.getCreateTime(),
                        mySkill.getCategoryId(), mySkill.getTags(), mySkill.getServiceMethod(), mySkill.getTitle(), mySkill.getDescription()
                );
                maxScore = Math.max(maxScore, score);
            }
            scoreMap.put(demand, maxScore + creditBonus);
        }

        return allActiveDemands.stream()
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(scoreMap.getOrDefault(b, 0.0), scoreMap.getOrDefault(a, 0.0));
                    if (scoreCompare != 0) return scoreCompare;
                    if (a.getCreateTime() != null && b.getCreateTime() != null) return b.getCreateTime().compareTo(a.getCreateTime());
                    return 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * 核心计算：双向约束综合评分 (结构化 + 文本相似度)
     */
    private double calculateMatchScore(
            Long tCategory, String tTags, String tMethod, String tTitle, String tDesc, LocalDateTime tCreateTime,
            Long mCategory, String mTags, String mMethod, String mTitle, String mDesc) {

        double score = 0.0;

        // 1. 结构化匹配 - 类别
        if (tCategory != null && tCategory.equals(mCategory)) {
            score += 20.0;
        }

        // 2. 结构化匹配 - 服务方式
        if (StringUtils.hasText(tMethod) && StringUtils.hasText(mMethod) && tMethod.equals(mMethod)) {
            score += 10.0; // 例如都是"线上"或"线下"
        }

        // 3. 结构化匹配 - 标签交集
        Set<String> targetTags = parseTags(tTags);
        Set<String> myTags = parseTags(mTags);
        for (String tag : targetTags) {
            if (myTags.contains(tag)) {
                score += 10.0; // 分词完全命中加10分
            }
        }

        // 4. TF-IDF + 余弦相似度 (模拟)
        String text1 = (tTitle != null ? tTitle : "") + " " + (tDesc != null ? tDesc : "");
        String text2 = (mTitle != null ? mTitle : "") + " " + (mDesc != null ? mDesc : "");
        double cosineSim = calculateCosineSimilarity(text1, text2);
        score += (cosineSim * 40.0); // 文本相似度最高贡献40分

        // 5. 时间衰减惩罚 (越新权重越高)
        if (tCreateTime != null) {
            long days = Duration.between(tCreateTime, LocalDateTime.now()).toDays();
            double timeBonus = Math.max(0, 10.0 - days); // 最好有10分加成，每天掉1分
            score += timeBonus;
        }

        return score;
    }

    /**
     * 基于 Bi-gram 的轻量级余弦相似度计算
     * 适合受限条件下的中文文本匹配（替代BM25和专业分词器）
     */
    private double calculateCosineSimilarity(String s1, String s2) {
        if (!StringUtils.hasText(s1) || !StringUtils.hasText(s2)) return 0.0;

        Map<String, Integer> freq1 = getBigramFreq(s1);
        Map<String, Integer> freq2 = getBigramFreq(s2);

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(freq1.keySet());
        allKeys.addAll(freq2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : allKeys) {
            int v1 = freq1.getOrDefault(key, 0);
            int v2 = freq2.getOrDefault(key, 0);
            dotProduct += (v1 * v2);
            norm1 += (v1 * v1);
            norm2 += (v2 * v2);
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private Map<String, Integer> getBigramFreq(String text) {
        Map<String, Integer> map = new HashMap<>();
        text = text.replaceAll("[\\s\\p{P}]", "").toLowerCase(); // 去除标点和空格
        if (text.length() == 0) return map;
        if (text.length() == 1) {
            map.put(text, 1);
            return map;
        }
        for (int i = 0; i < text.length() - 1; i++) {
            String bigram = text.substring(i, i + 2);
            map.put(bigram, map.getOrDefault(bigram, 0) + 1);
        }
        return map;
    }

    private Set<String> parseTags(String tagsStr) {
        if (!StringUtils.hasText(tagsStr)) return new HashSet<>();
        return Arrays.stream(tagsStr.split("[,，]"))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private List<Skill> fallbackSortSkills(List<Skill> list) {
        return list.stream()
                .sorted((a, b) -> {
                    if (a.getCreateTime() != null && b.getCreateTime() != null) return b.getCreateTime().compareTo(a.getCreateTime());
                    return 0;
                })
                .collect(Collectors.toList());
    }

    private List<Demand> fallbackSortDemands(List<Demand> list) {
        return list.stream()
                .sorted((a, b) -> {
                    if (a.getCreateTime() != null && b.getCreateTime() != null) return b.getCreateTime().compareTo(a.getCreateTime());
                    return 0;
                })
                .collect(Collectors.toList());
    }
}
