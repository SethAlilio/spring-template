package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import com.sethdev.spring_template.repository.SysResourceRepository;
import com.sethdev.spring_template.service.SysResourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        return convertSysResourceListToListPermissionNode(
                resources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), resources
        );
    }

    @Override
    public List<ResourceNode<Integer>> convertSysResourceListToListPermissionNode(List<SysResource> currentIteration,
                                                                                  List<SysResource> resources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> ResourceNode.<Integer>builder()
                            .key(String.valueOf(res.getId()))
                            .label(res.getName())
                            .icon("pi pi-fw " + res.getIcon())
                            .data(res.getId())
                            .children(convertSysResourceListToListPermissionNode(
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
    public void insertSysPermissions(List<SysPermission> permissions) {
        sysResourceRepo.insertSysPermissions(permissions);
    }

    @Override
    public List<SysPermission> getSysPermissionsByRoleId(Integer roleId) {
        return sysResourceRepo.getSysPermissionsByRoleId(roleId);
    }

    @Override
    public void deletePermissionsByRoleId(Integer roleId) {
        sysResourceRepo.deleteSysPermissionsByRoleId(roleId);
    }

    @Override
    public void deletePermissionsByIds(List<Integer> ids) {
        sysResourceRepo.deleteSysPermissionsByIds(ids);
    }

}
