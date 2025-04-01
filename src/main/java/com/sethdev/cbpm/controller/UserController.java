package com.sethdev.cbpm.controller;

import com.sethdev.cbpm.models.ResultMsg;
import com.sethdev.cbpm.models.User;
import com.sethdev.cbpm.service.UserService;
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
