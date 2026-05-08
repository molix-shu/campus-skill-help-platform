package com.nntu.skillhelp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SkillHelpApplication {

    public static void main(String[] args) {
        // 项目启动引擎
        SpringApplication.run(SkillHelpApplication.class, args);
        System.out.println("====== 南通大学高校技能互助平台后端启动成功！ ======");
    }


    @GetMapping("/hello")
    public String sayHello() {
        return "Hello 南通大学! Spring Boot 后端搭建成功!";
    }
}
