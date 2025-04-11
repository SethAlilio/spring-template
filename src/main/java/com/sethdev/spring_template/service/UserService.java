package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.User;

import java.util.Map;

public interface UserService {
    ResultMsg<?> updateUserDetails(User user);

    ResultMsg<?> updatePassword(User user);

    ResultPage<User> getUsersFromGroup(PagingRequest<Map<String, Object>> request);

    ResultPage<User> getUserList(PagingRequest<User> request);

    ResultMsg<User> getUser(Integer id);

    ResultMsg<?> createUser(User user);

    ResultMsg<?> updateUser(User user);

    ResultMsg<?> deleteUser(Integer id);
}
