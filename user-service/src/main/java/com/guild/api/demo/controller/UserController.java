package com.guild.api.demo.controller;

import com.guild.api.demo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping(value = "/users/{userId}")
    public @ResponseBody User getUser(@PathVariable String userId) throws InterruptedException { Thread.sleep(1000);
        String userInfo = String.format("{Id: %s, Name: James}", userId);
        User user = new User(userId, "James");
        LOGGER.info(user.getId() + user.getName());
        return user;
    }

}
