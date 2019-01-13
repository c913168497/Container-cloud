package com.application.docker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/get")
public class TestController {

    @GetMapping("/get")
    public String searchLogByInstId(){
        return "123";
    }
}
