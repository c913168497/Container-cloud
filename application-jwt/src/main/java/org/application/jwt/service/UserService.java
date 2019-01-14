package org.application.jwt.service;

import org.application.jwt.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private List<String> getAllUrlByUserId(){
        String url = "/test/getUserInfo";
        String url1 = "test/getManagerInfo";
        String url2 = "test/getAdminInfo";
        List<String> strings = new ArrayList<>();
        strings.add(url);
        strings.add(url1);
        strings.add(url2);
        return strings;
    }

    public User getUserInfoByUserName(){
        User user = new User();
        user.setPassword("1234");
        user.setUserName("root");
        return user;
    }

    public List<String> getUserRoles(String userId){
        String roleId1 = "1";
        String roleId2 = "2";

        List<String> roles = new ArrayList<>();
        roles.add(roleId1);
        roles.add(roleId2);
        return roles;
    }
}
