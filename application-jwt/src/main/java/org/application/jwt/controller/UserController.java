package org.application.jwt.controller;

import org.application.jwt.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/user")
public class UserController {

    @PostMapping(value = "/login")
    public String login(@RequestBody User user){



    }
}
