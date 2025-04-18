package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.*;
import com.sethdev.spring_template.models.constants.SysResourceCategory;
import com.sethdev.spring_template.models.constants.UserPermissionType;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import com.sethdev.spring_template.repository.SysResourceRepository;
import com.sethdev.spring_template.service.ContextService;
import com.sethdev.spring_template.service.SysResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysResourceServiceImpl implements SysResourceService {

    @Autowired
    SysResourceRepository sysResourceRepo;

    @Autowired
    ContextService contextService;

    @Override
    public void createSysResource(SysResource sysResource) throws BusinessException {
        ResultMsg<?> validation = validateSysResource(sysResource);
        if (!validation.isSuccess()) {
            throw new BusinessException(validation.getMessage());
        }
        sysResource.setCreateBy(contextService.getCurrentUserId());
        sysResourceRepo.insertSysResource(sysResource);
        if (sysResource.getParentId() != null) {
            String path = sysResourceRepo.getPath(sysResource.getParentId());
            sysResourceRepo.updatePath(sysResource.getId(),
                    String.format("%s.%s", path, sysResource.getId()));
        } else {
            sysResourceRepo.updatePath(sysResource.getId(), String.valueOf(sysResource.getId()));
        }
    }

    public ResultMsg<?> validateSysResource(SysResource sysResource) {
        //Required fields
        if (StringUtils.isBlank(sysResource.getName())) {
            return new ResultMsg<>().failure("Name is required");
        }
        if (StringUtils.isBlank(sysResource.getCategory())) {
            return new ResultMsg<>().failure("Category is required");
        }

        //For buttons, the parent should be a MENU
        if (SysResourceCategory.BUTTON.name().equals(sysResource.getCategory())
            && sysResource.getParentId() != null) {
            String parentCategory = sysResourceRepo.getCategory(sysResource.getParentId());
            if (SysResourceCategory.BUTTON.name().equals(parentCategory)) {
                return new ResultMsg<>().failure("Parent of a button should be a menu");
            }
        }
        return new ResultMsg<>().success();
    }

    @Override
    public List<SysResource> getAllResources() {
        return sysResourceRepo.getAllResources();
    }

    @Override
    public List<ResourceNode<SysResource>> getAllResourcesAsPermissionNodeList() {
        List<SysResource> resources = sysResourceRepo.getAllResources();
        return convertSysResourceListToListResourceNode(
                resources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), resources
        );
    }

    @Override
    public List<ResourceNode<SysResource>> convertSysResourceListToListResourceNode(List<SysResource> currentIteration,
                                                                                  List<SysResource> resources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> ResourceNode.<SysResource>builder()
                            .key(String.valueOf(res.getId()))
                            .label(res.getName())
                            .icon("pi pi-fw " + res.getIcon())
                            .data(res)
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

    /**
     * Get the menu resources available to user
     * @param user
     * @return Resources as AppMenuItem to match frontend
     */
    @Override
    public List<AppMenuItem> getUserAppMenuItems(User user) {
        if (UserPermissionType.ROLE.name().equals(user.getPermission())) {
            List<SysResource> roleResources = sysResourceRepo.getResourcesByRole(
                    SysResourceCategory.MENU.name(), user.getRoleId());
            return convertSysResourceListToAppMenuItemList(
                    roleResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    roleResources);
        } else if (UserPermissionType.USER.name().equals(user.getPermission())) {
            List<SysResource> userResources = sysResourceRepo.getResourcesByUser(
                    SysResourceCategory.MENU.name(), user.getId());
            return convertSysResourceListToAppMenuItemList(
                    userResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    userResources);
        }
        return null;
    }

    public List<AppMenuItem> getUserAppButtonItems(User user) {
        if (UserPermissionType.ROLE.name().equals(user.getPermission())) {
            List<SysResource> roleResources = sysResourceRepo.getResourcesByRole(
                    SysResourceCategory.BUTTON.name(), user.getRoleId());
            return convertSysResourceListToAppMenuItemList(
                    roleResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    roleResources);
        } else if (UserPermissionType.USER.name().equals(user.getPermission())) {
            List<SysResource> userResources = sysResourceRepo.getResourcesByUser(
                    SysResourceCategory.BUTTON.name(), user.getId());
            return convertSysResourceListToAppMenuItemList(
                    userResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    userResources);
        }
        return null;
    }

    /**
     * Same as {@link #getUserAppMenuItems(User)} but different paramenter
     * @param userId
     * @return Resources as AppMenuItem to match frontend
     */
    @Override
    public List<AppMenuItem> getUserAppMenuItems(Integer userId) {
        String permission = sysResourceRepo.getUserResourcePermission(userId);
        if (UserPermissionType.ROLE.name().equals(permission)) {
            List<SysResource> roleResources = sysResourceRepo.getResourcesByUserRole(
                    SysResourceCategory.MENU.name(), userId);
            return convertSysResourceListToAppMenuItemList(
                    roleResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    roleResources);
        } else if (UserPermissionType.USER.name().equals(permission)) {
            List<SysResource> userResources = sysResourceRepo.getResourcesByUser(
                    SysResourceCategory.MENU.name(), userId);
            return convertSysResourceListToAppMenuItemList(
                    userResources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()),
                    userResources);
        }
        return null;
    }

    @Override
    public List<AppMenuItem> convertSysResourceListToAppMenuItemList(List<SysResource> currentIteration,
                                                                     List<SysResource> sysResources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> AppMenuItem.builder()
                            .label(res.getName())
                            .icon(StringUtils.isNotBlank(res.getIcon()) ? "pi pi-fw " + res.getIcon() : "")
                            .to(res.getResourcePath())
                            .items(convertSysResourceListToAppMenuItemList(
                                    sysResources.stream()
                                              .filter(x -> x.getParentId() != null && x.getParentId().equals(res.getId()))
                                              .collect(Collectors.toList()),
                                    sysResources))
                            .build()
                    )
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public void updateSysResource(SysResource sysResource) throws BusinessException {
        ResultMsg<?> validation = validateSysResource(sysResource);
        if (!validation.isSuccess()) {
            throw new BusinessException(validation.getMessage());
        }
        sysResourceRepo.updateSysResource(sysResource);
    }

    @Override
    public void deleteSysResource(Integer id) {
        String path = sysResourceRepo.getPath(id);
        sysResourceRepo.deleteSysPermissionByResource(id, path);
        sysResourceRepo.deleteSysResource(id, path);
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
    public void deleteSysPermissionsByUserId(Integer userId) {
        sysResourceRepo.deleteSysPermissionsByUserId(userId);
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
