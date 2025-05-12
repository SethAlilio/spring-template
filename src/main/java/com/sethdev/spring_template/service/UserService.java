package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.models.sys.SysRelation;

import java.util.Map;

public interface UserService {

    ResultPage<User> getUsersFromGroup(PagingRequest<Map<String, Object>> request);

    ResultPage<User> getUserList(PagingRequest<User> request);

    ResultMsg<User> getUser(Integer id);

    ResultMsg<?> createUser(User user);

    ResultMsg<?> updateUser(User user);

    ResultMsg<?> updateUserDetails(User user);

    ResultMsg<?> updatePassword(Integer userId, String oldPassword, String newPassword);

    ResultMsg<?> updateUserActivePosition(Integer userId, Integer relationId);

    void updateUserEnabled(Integer userId, Boolean enabled);

    ResultMsg<?> deleteUser(Integer id);

    ResultMsg<?> addUserToGroup(SysRelation relation);

    ResultMsg<?> removeUserRelation(Integer relationId);
}
