package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired
    UserService userService;

    /*@PostMapping("/update")
    public ResultMsg<?> updateUserDetails(@RequestBody User user) {
        return userService.updateUserDetails(user);
    }*/

    @PostMapping("/usersGroup")
    public ResultPage<User> getUsersFromGroup(@RequestBody PagingRequest<Map<String, Object>> request) {
        return userService.getUsersFromGroup(request);
    }

    @PostMapping("/create")
    public ResultMsg<?> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/list")
    public ResultPage<User> getUserList(@RequestBody PagingRequest<User> request) {
        return userService.getUserList(request);
    }

    @PostMapping("/get")
    public ResultMsg<User> getUser(@RequestParam Integer id) {
        return userService.getUser(id);
    }

    /**
     * Updates user details and positions
     * @param user
     * @return
     */
    @PostMapping("/update")
    public ResultMsg<?> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    /**
     * Use to update just the basic user details (Username, Full Name, Email)
     * Called in the profile page
     * @param user
     * @return
     */
    @PostMapping("/update/details")
    public ResultMsg<?> updateUserDetails(@RequestBody User user) {
        return userService.updateUserDetails(user);
    }

    @PostMapping("/update/password")
    public ResultMsg<?> updatePassword(@RequestParam Integer userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        return userService.updatePassword(userId, oldPassword, newPassword);
    }

    @PostMapping("/update/activePosition")
    public ResultMsg<?> updateActivePosition(@RequestParam Integer userId, @RequestParam Integer relationId) {
        return userService.updateUserActivePosition(userId, relationId);
    }

    @PostMapping("/enable")
    public ResultMsg<?> updateUserEnabled(@RequestParam Integer id, @RequestParam Boolean enable) {
        try {
            userService.updateUserEnabled(id, enable);
            return new ResultMsg<>().success("Account " + (enable ? "enabled" : "disabled"));
        } catch (Exception e) {
            return new ResultMsg<>().failure("Error " + (enable ? "enabling" : "disabling") + " account");
        }
    }

    @PostMapping("/delete")
    public ResultMsg<?> deleteUser(@RequestParam Integer id) {
        return userService.deleteUser(id);
    }
}
