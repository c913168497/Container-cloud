package org.application.jwt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/test")
public class TestController {

    @GetMapping("/getUserInfo")
    public String getUserInfo(){
        return "测试用户信息";
    }

    @GetMapping("/getManagerInfo")
    public String getManagerInfo(){
        return "管理员信息";
    }

    @GetMapping("/getAdminInfo")
    public String getAdminInfo(){
        return "超级管理员信息";
    }
}
