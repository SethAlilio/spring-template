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

    void insertSysResource(SysResource sysResource);

    List<SysResource> getAllResources();

    /**
     * @param roleId
     * @return Resources based on role's permissions
     */
    List<SysResource> getResourcesByRole(@Param("category") String category,
                                         @Param("roleId") Integer roleId);

    /**
     * @param userId
     * @return Resources based on user specific permissions
     */
    List<SysResource> getResourcesByUser(@Param("category") String category,
                                         @Param("userId") Integer userId);

    /**
     * @param userId
     * @return Resources based on user's role's permissions
     */
    List<SysResource> getResourcesByUserRole(@Param("category") String category,
                                             @Param("userId") Integer userId);

    String getPath(Integer id);

    String getCategory(Integer id);

    void updateSysResource(SysResource sysResource);

    void updatePath(@Param("id") Integer id,
                    @Param("path") String path);

    void deleteSysResource(@Param("id") Integer id,
                           @Param("path") String path);

    void deleteSysPermissionByResource(@Param("sysResourceId") Integer sysResourceId,
                                       @Param("path") String path);

    /** Sys Permission **/

    void insertSysPermissionsRoleBased(@Param("permissions")List<SysPermission> permissions);
    void insertSysPermissionsUserBased(@Param("permissions")List<SysPermission> permissions);

    List<SysPermission> getSysPermissionsByRoleId(Integer roleId);
    List<SysPermission> getSysPermissionsByUserId(Integer userId);

    String getUserResourcePermission(Integer userId);

    void deleteSysPermissionsByRoleId(Integer roleId);
    void deleteSysPermissionsByUserId(Integer userId);

    void deleteSysPermissionsByIds(@Param("ids") List<Integer> ids);

}
