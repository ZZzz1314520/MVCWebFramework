package com.example.spring.controller;

import com.example.spring.annotation.CQUController;
import com.example.spring.annotation.CQURequestMapper;

import javax.annotation.Nullable;


@CQUController
public class TestController {


    @CQURequestMapper("/hello")
    @Nullable
    public String sayHello() {
        return "Hello";
    }

    @CQURequestMapper("/sayHello")
    @Nullable
    public String sayHello(String name) {

        return "Hello " + name;
    }

    @CQURequestMapper("/index")
    @Nullable
    public String index() {
        String path = "<!doctype html>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>首页</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <a href=\"http://localhost:8081/mySpring_war_exploded/\">文件上传<a />\n" +
                "        <a href=\"https://imgtu.com/i/fWohd0\"><img src=\"https://z3.ax1x.com/2021/08/16/fWohd0.png\" alt=\"fWohd0.png\"\n" +
                "                border=\"0\" /></a>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        return path;
    }

}
