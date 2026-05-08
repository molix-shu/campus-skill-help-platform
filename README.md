<div align="center">

# 🎓 Campus Skill Help Platform

### 基于微信小程序的高校技能互助平台

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Vue-3.x-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white" />
  <img src="https://img.shields.io/badge/Uni--App-WeChat%20Mini%20Program-07C160?style=flat-square&logo=wechat&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.5-blue?style=flat-square" />
  <img src="https://img.shields.io/github/license/molix-shu/campus-skill-help-platform?style=flat-square" />
</p>

<p>
  <img src="https://img.shields.io/github/stars/molix-shu/campus-skill-help-platform?style=flat-square" />
  <img src="https://img.shields.io/github/forks/molix-shu/campus-skill-help-platform?style=flat-square" />
  <img src="https://img.shields.io/github/repo-size/molix-shu/campus-skill-help-platform?style=flat-square" />
  <img src="https://img.shields.io/github/last-commit/molix-shu/campus-skill-help-platform?style=flat-square" />
</p>

</div>

---

## 📌 项目简介

**Campus Skill Help Platform** 是一个面向高校学生的技能互助平台，围绕校园内常见的技能共享、学习求助、生活协助等场景，提供从信息发布、智能匹配、订单流转、即时沟通到评价反馈的完整业务闭环。

系统以 **微信小程序** 作为学生端入口，以 **Vue + Element Plus** 构建后台管理端，以 **Spring Boot + MyBatis-Plus + MySQL** 作为后端服务与数据支撑，适用于毕业设计、课程设计以及校园服务类系统实践。

---

## ✨ 核心功能

- 👤 **用户登录与资料管理**：支持微信小程序用户登录、资料维护和校园身份认证
- 🧩 **技能发布**：学生可以发布自己擅长的技能服务，如编程辅导、设计剪辑、课程答疑等
- 📮 **需求发布**：学生可以发布求助需求，描述所需帮助、时间要求和酬谢方式
- 🎯 **供需匹配**：基于分类、标签、时间、服务方式和信用分进行规则评分匹配
- 📦 **订单管理**：将互助过程转化为可记录、可跟踪、可评价的订单流程
- 💬 **即时沟通**：支持围绕订单的聊天消息记录，方便双方协作
- ⭐ **评价反馈**：互助完成后可进行评分与评价，沉淀用户信用
- 🛡️ **后台管理**：支持用户、技能、需求、订单、评价和认证信息的管理

---

## 🧱 技术栈

### 前端 / 小程序端

- `Uni-App`
- `Vue`
- `JavaScript`
- `WeChat Mini Program`

### 后台管理端

- `Vue 3`
- `Vite`
- `Element Plus`
- `Vue Router`
- `Axios`

### 后端服务

- `Java 17`
- `Spring Boot 3.2.3`
- `MyBatis-Plus 3.5.5`
- `MySQL`
- `Maven`

### 数据库

- `MySQL`
- 用户表
- 分类表
- 技能供给表
- 互助需求表
- 互助订单表
- 评价反馈表
- 聊天消息表

---

## 📂 项目结构

```text
campus-skill-help-platform/
├── SKILL-HELP/              # 微信小程序端 / Uni-App 前端
├── skill-help-admin/        # 后台管理端 Vue 项目
├── skill-help-backend/      # Spring Boot 后端服务
├── init_db.sql              # 数据库初始化脚本
├── alter_avatar.sql         # 头像字段更新脚本
├── alter_eval.sql           # 评价相关更新脚本
├── update_chat.sql          # 聊天模块更新脚本
├── clear_all_data.sql       # 数据清理脚本
├── LICENSE                  # MIT License
└── README.md                # 项目说明文档
```

---

## 🗃️ 数据库设计概览

系统围绕高校互助业务设计了多个核心数据表：

| 数据表 | 说明 |
|---|---|
| `user` | 用户基础信息、校园认证信息与信用分 |
| `category` | 技能和需求的分类信息 |
| `skill` | 用户发布的技能供给信息 |
| `demand` | 用户发布的互助需求信息 |
| `help_order` | 互助订单与流程状态 |
| `evaluation` | 订单评价与用户反馈 |
| `chat_message` | 订单内聊天消息记录 |

---

## 🔄 业务流程

```text
用户登录
  ↓
完善资料 / 校园认证
  ↓
发布技能 或 发布需求
  ↓
系统进行供需匹配
  ↓
创建互助订单
  ↓
双方沟通并完成互助
  ↓
提交评价
  ↓
更新信用与平台记录
```

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/molix-shu/campus-skill-help-platform.git
cd campus-skill-help-platform
```

---

### 2. 初始化数据库

请先在 MySQL 中执行数据库初始化脚本：

```bash
init_db.sql
```

如果需要补充头像、评价、聊天等字段，可根据实际情况继续执行：

```bash
alter_avatar.sql
alter_eval.sql
update_chat.sql
```

---

### 3. 启动后端服务

进入后端目录：

```bash
cd skill-help-backend
```

修改数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/skill_help_db
    username: your_username
    password: your_password
```

启动后端：

```bash
mvn spring-boot:run
```

默认服务地址：

```text
http://localhost:8088
```

---

### 4. 启动后台管理端

进入后台管理端目录：

```bash
cd skill-help-admin
```

安装依赖：

```bash
npm install
```

启动开发环境：

```bash
npm run dev
```

---

### 5. 运行微信小程序端

进入小程序端目录：

```bash
cd SKILL-HELP
```

然后使用微信开发者工具或 HBuilderX 打开项目，根据实际后端地址修改接口配置后运行。

---

## 🧠 项目亮点

- 🎓 **校园场景明确**：围绕高校学生技能互助需求设计，业务目标清晰
- 🔁 **流程闭环完整**：覆盖发布、匹配、下单、沟通、完成、评价全过程
- 📊 **规则评分匹配**：结合分类、标签、时间、服务方式和信用分进行推荐
- 🧾 **订单化管理**：将松散的互助行为转化为可追踪的业务流程
- ⭐ **信用机制设计**：通过认证、评价和信用分提升平台可信度
- 🧩 **前后端分离**：小程序端、后台管理端、后端服务职责清晰

---

## 📸 页面预览

> 可以在这里补充系统截图，例如小程序首页、技能大厅、需求大厅、订单页面、后台管理页面等。

```text
docs/images/
├── miniapp-home.png
├── skill-list.png
├── demand-list.png
├── order-detail.png
└── admin-dashboard.png
```

示例写法：

```markdown
![小程序首页](docs/images/miniapp-home.png)
![后台管理端](docs/images/admin-dashboard.png)
```

---

## 📌 后续优化方向

- [ ] 优化供需匹配算法，引入用户行为数据提升推荐准确性
- [ ] 完善订单状态流转与异常处理逻辑
- [ ] 增强聊天模块的实时性
- [ ] 增加后台数据统计与可视化看板
- [ ] 优化移动端页面交互体验
- [ ] 增强接口安全与用户权限控制
- [ ] 将敏感配置迁移至环境变量或本地配置文件

---

## ⚠️ 安全说明

本项目为学习用途。  
在正式部署前，请注意：

- 不要将真实数据库密码提交到 GitHub
- 不要将微信小程序 `AppSecret` 提交到 GitHub
- 建议使用 `.env` 或本地配置文件管理敏感信息
- 生产环境需要补充权限校验、参数校验和接口鉴权

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**Campus Skill Help Platform**  
让校园技能互助更高效、更可信、更可追踪。

</div>
