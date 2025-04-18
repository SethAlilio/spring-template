package com.sethdev.spring_template.service;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.AppMenuItem;
import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.ResourceNodeCheck;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;

import java.util.List;
import java.util.Map;

public interface SysResourceService {
    void createSysResource(SysResource sysResource) throws BusinessException;

    List<SysResource> getAllResources();

    List<ResourceNode<SysResource>> getAllResourcesAsPermissionNodeList();

    List<ResourceNode<SysResource>> convertSysResourceListToListResourceNode(List<SysResource> currentIteration,
                                                                           List<SysResource> resources);

    List<AppMenuItem> getUserAppMenuItems(User user);

    List<AppMenuItem> getUserAppMenuItems(Integer userId);

    List<AppMenuItem> convertSysResourceListToAppMenuItemList(List<SysResource> currentIteration,
                                                              List<SysResource> sysResources);

    void updateSysResource(SysResource sysResource) throws BusinessException;

    void deleteSysResource(Integer id);

    void insertSysPermissionsRoleBased(List<SysPermission> permissions);
    void insertSysPermissionsUserBased(List<SysPermission> permissions);

    List<SysPermission> getSysPermissionsByRoleId(Integer roleId);

    List<SysPermission> getSysPermissionsByUserId(Integer userId);

    void deletePermissionsByRoleId(Integer roleId);
    void deleteSysPermissionsByUserId(Integer userId);

    void deletePermissionsByIds(List<Integer> ids);

    //Format the resources for the `value` property of PrimeReact's Tree component
    Map<String, ResourceNodeCheck> convertSysPermissionListToPermissionNodeCheckMap(
            List<SysPermission> permissions, List<SysResource> resources);

    //Format the permission for the `selectedKey` property of PrimeReact's Tree component
    ResourceNodeCheck getAsPermissionNodeCheck(Integer resourceId, List<Integer> permissions,
                                               List<SysResource> resources);
}
