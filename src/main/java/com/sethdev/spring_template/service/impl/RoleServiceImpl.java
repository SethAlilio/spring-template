package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.*;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysResource;
import com.sethdev.spring_template.repository.RoleRepository;
import com.sethdev.spring_template.service.ContextService;
import com.sethdev.spring_template.service.RoleService;
import com.sethdev.spring_template.service.SysResourceService;
import com.sethdev.spring_template.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleRepository roleRepo;

    @Autowired
    ContextService contextService;

    @Autowired
    SysResourceService sysResourceService;

    @Override
    public void createRole(Role role) throws BusinessException {
        if (StringUtils.isBlank(role.getName())) {
            throw new BusinessException("Name is required");
        }
        role.setCreateBy(contextService.getCurrentUserId());
        roleRepo.createRole(role);

        List<SysPermission> permissions = role.getSelectedPermissions().keySet()
                .stream()
                .map(x -> {
                    SysPermission perm = SysPermission.builder()
                            .roleId(role.getId())
                            .resourceId(Integer.valueOf(x))
                            .type(1)
                            .build();
                    perm.setCreateBy(contextService.getCurrentUserId());
                    return perm;
                })
                .collect(Collectors.toList());
        sysResourceService.insertSysPermissions(permissions);
    }

    @Override
    public Role getRole(Integer id) throws BusinessException {
        Role role = roleRepo.getRole(id);
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        List<SysResource> resources = sysResourceService.getAllResources();
        log.info("getRole | resources: " + GsonUtil.toJson(resources));
        List<SysPermission> permissions = roleRepo.getRolePermissions(id);

        List<ResourceNode<Integer>> resourceNodes = sysResourceService.convertSysResourceListToListResourceNode(
          resources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), resources
        );
        role.setPermissionTree(resourceNodes);
        role.setSelectedPermissions(convertSysPermissionListToPermissionNodeCheckMap(permissions, resources));
        return role;
    }

    /*public List<PermissionNode<Integer>> convertSysResourceListToListPermissionNode(List<SysResource> currentIteration,
                                                                                    List<SysResource> resources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> PermissionNode.<Integer>builder()
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
    }*/

    //Format the resources for the `value` property of PrimeReact's Tree component
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

    @Override
    public ResultPage<Role> getRoleList(PagingRequest<Role> request) {
        List<Role> roleList = roleRepo.getRoleList(request);
        int totalCount = request.getStart() == 0 && roleList.size() < request.getLimit()
                ? roleList.size() : roleRepo.getRoleListTotalCount(request);
        return ResultPage.<Role>builder()
                .data(roleList)
                .pageStart(request.getStart())
                .pageSize(request.getLimit())
                .totalCount(totalCount)
                .build();
    }

    @Override
    public void updateRole(Role role) throws BusinessException {
        if (StringUtils.isBlank(role.getName())) {
            throw new BusinessException("Name is required");
        }
        roleRepo.updateRole(role);

        //Adjust sys permission
        if (MapUtils.isEmpty(role.getSelectedPermissions())) {
            //Delete all permission
            sysResourceService.deletePermissionsByRoleId(role.getId());
        } else {
            //Get permissions to delete or to add based on the current perms
            List<SysPermission> currentPerms = sysResourceService.getSysPermissionsByRoleId(role.getId());
            List<Integer> currentPermsIds = currentPerms.stream()
                    .map(SysPermission::getId)
                    .collect(Collectors.toList());
            List<Integer> newPermsIds = role.getSelectedPermissions().keySet().stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            List<SysPermission> toAdd = newPermsIds.stream()
                    .filter(x -> !currentPermsIds.contains(x))
                    .map(x -> {
                        SysPermission perm = SysPermission.builder()
                                .roleId(role.getId())
                                .resourceId(Integer.valueOf(x))
                                .type(1)
                                .build();
                        perm.setCreateBy(contextService.getCurrentUserId());
                        return perm;
                    })
                    .collect(Collectors.toList());

            List<Integer> toDelete = currentPermsIds.stream()
                    .filter(x -> !newPermsIds.contains(x))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(toAdd)) {
                sysResourceService.insertSysPermissions(toAdd);
            }

            if (CollectionUtils.isNotEmpty(toDelete)) {
                sysResourceService.deletePermissionsByIds(toDelete);
            }
        }
    }

    @Override
    public void deleteRole(Integer id) {
        roleRepo.deleteRole(id);
        sysResourceService.deletePermissionsByRoleId(id);
    }

}
