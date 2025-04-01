package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.repository.UserRepository;
import com.sethdev.spring_template.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public ResultMsg<?> updateUserDetails(User user) {
        if (!user.isCompleteInput()) {
            return new ResultMsg<>().failure("Required fields must be filled");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("needRelog", false);
        User currentUser = userRepository.getById(user.getId());
        if (!currentUser.getUsername().equals(user.getUsername())) {
            Integer id = userRepository.getIdByUsername(user.getUsername());
            if (id != null && !id.equals(user.getId())) {
                return new ResultMsg<>().failure("Username is already taken");
            }
            result.put("needRelog", true); //Force user to relog when there's a change in username
        }
        if (StringUtils.isNotBlank(user.getEmail()) && !StringUtils.equals(currentUser.getEmail(), user.getUsername())) {
            Integer id = userRepository.getIdByEmail(user.getEmail());
            if (id != null && !id.equals(user.getId())) {
                return new ResultMsg<>().failure("Email is already in use");
            }
        }
        userRepository.updateDetails(user);
        return new ResultMsg<>().success(result, "Details updated");
    }

    @Override
    public ResultMsg<?> updatePassword(User user) {
        if (StringUtils.isBlank(user.getPassword())) {
            return new ResultMsg<>().failure("Password cannot be empty");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.updatePassword(user);
        return new ResultMsg<>().success("Password updated");
    }


}
