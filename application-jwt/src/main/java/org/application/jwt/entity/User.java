package org.application.jwt.entity;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String userId;
    private String userName ;
    private String password ;
    private List<String> roleIds;
    private String salt;
}
