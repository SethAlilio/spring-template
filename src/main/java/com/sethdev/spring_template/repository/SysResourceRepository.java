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

    /**
     * @param roleId
     * @return Resources based on role's permissions
     */
    List<SysResource> getResourcesByRole(Integer roleId);

    /**
     * @param userId
     * @return Resources based on user specific permissions
     */
    List<SysResource> getResourcesByUser(Integer userId);

    /**
     * @param userId
     * @return Resources based on user's role's permissions
     */
    List<SysResource> getResourcesByUserRole(Integer userId);

    /** Sys Permission **/

    void insertSysPermissionsRoleBased(@Param("permissions")List<SysPermission> permissions);
    void insertSysPermissionsUserBased(@Param("permissions")List<SysPermission> permissions);

    List<SysPermission> getSysPermissionsByRoleId(Integer roleId);
    List<SysPermission> getSysPermissionsByUserId(Integer userId);

    String getUserResourcePermission(Integer userId);

    void deleteSysPermissionsByRoleId(Integer roleId);

    void deleteSysPermissionsByIds(@Param("ids") List<Integer> ids);

}
