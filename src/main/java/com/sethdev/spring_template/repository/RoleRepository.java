package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.ERole;
import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.Role;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Mapper
@Repository
public interface RoleRepository {
    Optional<Role> findByName(ERole name);

    void createRole(Role role);

    Role getRole(Integer id);

    List<Role> getRoleList(PagingRequest<Role> request);

    int getRoleListTotalCount(PagingRequest<Role> request);

    void updateRole(Role role);

    void deleteRole(Integer id);

    //List<SysResource> getAllResources();

    List<SysPermission> getRolePermissions(@Param("id") Integer id);

}
