package com.sethdev.spring_template.service;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.Role;

public interface RoleService {
    void createRole(Role role) throws BusinessException;

    Role getRole(Integer id) throws BusinessException;

    ResultPage<Role> getRoleList(PagingRequest<Role> request);

    void updateRole(Role role) throws BusinessException;

    void deleteRole(Integer id);
}
