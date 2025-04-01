package com.sethdev.cbpm.service;

import com.sethdev.cbpm.security.services.UserDetailsImpl;

public interface ContextService {
    UserDetailsImpl getCurrentUser();

    Integer getCurrentUserId();

    String getCurrentUserFullName();
}
