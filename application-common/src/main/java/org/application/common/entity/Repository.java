package org.application.common.entity;

import lombok.Data;

@Data
public class Repository {
    
    private String url ="";
    
    private String userName;
    
    private String password;
    
    private String branch;

    private String version;

}
