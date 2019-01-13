package com.application.docker.controller;

import lombok.Data;

@Data
public class Test {

    private String name;

    private static Test test= new Test();

    public Test() {
    }

    public static Test  newInstance(){
        return test;
    }
}
