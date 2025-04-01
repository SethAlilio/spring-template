package com.sethdev.spring_template.service;

import com.sethdev.spring_template.security.services.UserDetailsImpl;

public interface ContextService {
    UserDetailsImpl getCurrentUser();

    Integer getCurrentUserId();

    String getCurrentUserFullName();
}
