package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/update")
    public ResultMsg<?> updateUserDetails(@RequestBody User user) {
        return userService.updateUserDetails(user);
    }

    @PostMapping("/changePassword")
    public ResultMsg<?> updatePassword(@RequestBody User user) {
        return userService.updatePassword(user);
    }

}
