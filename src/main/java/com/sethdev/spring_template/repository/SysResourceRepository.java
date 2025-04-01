package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysResourceRepository {
    List<SysResource> getAllResources();

    /** Sys Permission **/

    void insertSysPermissions(@Param("permissions")List<SysPermission> permissions);

    List<SysPermission> getSysPermissionsByRoleId(Integer roleId);

    void deleteSysPermissionsByRoleId(Integer roleId);

    void deleteSysPermissionsByIds(@Param("ids") List<Integer> ids);

}
