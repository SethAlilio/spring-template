package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;

import java.util.List;

public interface SysResourceService {
    List<SysResource> getAllResources();

    List<ResourceNode<Integer>> getAllResourcesAsPermissionNodeList();

    List<ResourceNode<Integer>> convertSysResourceListToListPermissionNode(List<SysResource> currentIteration,
                                                                           List<SysResource> resources);

    void insertSysPermissions(List<SysPermission> permissions);

    List<SysPermission> getSysPermissionsByRoleId(Integer roleId);

    void deletePermissionsByRoleId(Integer roleId);

    void deletePermissionsByIds(List<Integer> ids);
}
