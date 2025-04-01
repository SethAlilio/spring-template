package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.security.services.UserDetailsImpl;
import com.sethdev.spring_template.service.ContextService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ContextServiceImpl implements ContextService {

    @Override
    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (UserDetailsImpl) authentication.getPrincipal(); // Assuming you have a UserDetails implementation
        }
        return null; // Or throw an exception based on your needs
    }
    @Override
    public Integer getCurrentUserId() {
        UserDetailsImpl user = this.getCurrentUser();
        return user != null ? user.getId() : null;
    }
    @Override
    public String getCurrentUserFullName() {
        UserDetailsImpl user = this.getCurrentUser();
        return user != null ? user.getFullName() : null;
    }

}
