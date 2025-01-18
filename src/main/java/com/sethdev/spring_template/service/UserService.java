package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.User;

public interface UserService {
    ResultMsg<?> updateUserDetails(User user);

    ResultMsg<?> updatePassword(User user);
}
