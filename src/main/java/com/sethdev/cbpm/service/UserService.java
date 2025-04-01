package com.sethdev.cbpm.service;

import com.sethdev.cbpm.models.ResultMsg;
import com.sethdev.cbpm.models.User;

public interface UserService {
    ResultMsg<?> updateUserDetails(User user);

    ResultMsg<?> updatePassword(User user);
}
