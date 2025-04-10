package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.ResourceNodeCheck;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import com.sethdev.spring_template.repository.SysResourceRepository;
import com.sethdev.spring_template.service.SysResourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysResourceServiceImpl implements SysResourceService {

    @Autowired
    SysResourceRepository sysResourceRepo;

    @Override
    public List<SysResource> getAllResources() {
        return sysResourceRepo.getAllResources();
    }

    @Override
    public List<ResourceNode<Integer>> getAllResourcesAsPermissionNodeList() {
        List<SysResource> resources = sysResourceRepo.getAllResources();
        return convertSysResourceListToListResourceNode(
                resources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), resources
        );
    }

    @Override
    public List<ResourceNode<Integer>> convertSysResourceListToListResourceNode(List<SysResource> currentIteration,
                                                                                  List<SysResource> resources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> ResourceNode.<Integer>builder()
                            .key(String.valueOf(res.getId()))
                            .label(res.getName())
                            .icon("pi pi-fw " + res.getIcon())
                            .data(res.getId())
                            .children(convertSysResourceListToListResourceNode(
                                    resources.stream()
                                            .filter(x -> x.getParentId() != null && x.getParentId().equals(res.getId()))
                                            .collect(Collectors.toList()), resources))
                            .build()
                    )
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
    /** Sys Permission **/

    @Override
    public void insertSysPermissionsRoleBased(List<SysPermission> permissions) {
        sysResourceRepo.insertSysPermissionsRoleBased(permissions);
    }

    @Override
    public void insertSysPermissionsUserBased(List<SysPermission> permissions) {
        sysResourceRepo.insertSysPermissionsUserBased(permissions);
    }

    @Override
    public List<SysPermission> getSysPermissionsByRoleId(Integer roleId) {
        return sysResourceRepo.getSysPermissionsByRoleId(roleId);
    }

    @Override
    public List<SysPermission> getSysPermissionsByUserId(Integer userId) {
        return sysResourceRepo.getSysPermissionsByUserId(userId);
    }

    @Override
    public void deletePermissionsByRoleId(Integer roleId) {
        sysResourceRepo.deleteSysPermissionsByRoleId(roleId);
    }

    @Override
    public void deletePermissionsByIds(List<Integer> ids) {
        sysResourceRepo.deleteSysPermissionsByIds(ids);
    }

    //Format the resources for the `value` property of PrimeReact's Tree component
    @Override
    public Map<String, ResourceNodeCheck> convertSysPermissionListToPermissionNodeCheckMap(
            List<SysPermission> permissions, List<SysResource> resources) {
        List<Integer> permittedResourceIds = permissions.stream()
                .map(SysPermission::getResourceId)
                .collect(Collectors.toList());
        Map<String, ResourceNodeCheck> selectedPermissions = new HashMap<>();
        permissions.forEach(x -> selectedPermissions.put(String.valueOf(x.getResourceId()),
                getAsPermissionNodeCheck(x.getResourceId(), permittedResourceIds, resources)));
        return selectedPermissions;
    }

    //Format the permission for the `selectedKey` property of PrimeReact's Tree component
    @Override
    public ResourceNodeCheck getAsPermissionNodeCheck(Integer resourceId, List<Integer> permissions,
                                                      List<SysResource> resources) {
        SysResource resource = resources.stream().filter(x -> x.getId().equals(resourceId))
                .findFirst().orElse(null);
        if (resource != null && permissions.contains(resourceId)) {
            List<Integer> children = resources.stream()
                    .filter(x -> x.getParentId() != null && x.getParentId().equals(resourceId))
                    .map(SysResource::getId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(children)) {
                if (new HashSet<>(permissions).containsAll(children)) {
                    return new ResourceNodeCheck(true, false);
                } else {
                    return new ResourceNodeCheck(false, true);
                }
            } else {
                return new ResourceNodeCheck(true, false);
            }
        }
        return new ResourceNodeCheck(false, false);
    }


}
