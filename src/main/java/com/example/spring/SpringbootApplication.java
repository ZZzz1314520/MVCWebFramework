package com.example.spring;

import com.example.spring.annotation.CQUSpringBootApplication;
import com.example.spring.framework.CQUApplication;

//启动项目
@CQUSpringBootApplication(scanBasePackages = "com.example.spring", configPath = "/application.properties")
public class SpringbootApplication {
    public static void main(String[] args) throws Exception {
        CQUApplication.run(SpringbootApplication.class, args);
    }
}