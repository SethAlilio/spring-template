package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.*;
import com.sethdev.spring_template.models.constants.Crud;
import com.sethdev.spring_template.models.sys.SysPermission;
import com.sethdev.spring_template.models.sys.SysRelation;
import com.sethdev.spring_template.models.sys.SysResource;
import com.sethdev.spring_template.repository.SysRelationRepository;
import com.sethdev.spring_template.repository.UserRepository;
import com.sethdev.spring_template.service.ContextService;
import com.sethdev.spring_template.service.SysResourceService;
import com.sethdev.spring_template.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: Test if transactions rollback for adding or updating user
 */

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    SysResourceService sysResourceService;

    @Autowired
    ContextService contextService;

    @Autowired
    SysRelationRepository sysRelRepo;

    @Autowired
    PasswordEncoder encoder;

    @Value("000000")
    String defaultPassword;

    @Override
    public ResultPage<User> getUsersFromGroup(PagingRequest<Map<String, Object>> request) {
        List<User> userList = userRepo.getUsersFromGroup(request);
        int totalCount = request.getStart() == 0 && userList.size() < request.getLimit()
                ? userList.size() : userRepo.getUsersByGroupIdCount(request);
        return ResultPage.<User>builder()
                .data(userList)
                .pageStart(request.getStart())
                .pageSize(request.getLimit())
                .totalCount(totalCount)
                .build();
    }

    public ResultMsg<?> isValidUser(User user, Crud action) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isBlank(user.getUsername())) {
            missing.add("Username");
        } else {
            Integer id = userRepo.getIdByUsername(user.getUsername());
            if (id != null && (action == Crud.CREATE || !id.equals(user.getId()))) {
                return new ResultMsg<>().failure("Username is already taken");
            }
        }
        if (StringUtils.isBlank(user.getFullName())) {
            missing.add("Full Name");
        }
        if (StringUtils.isBlank(user.getPermission())) {
            missing.add("Permission");
        }

        if (StringUtils.isNotBlank(user.getEmail())) {
            Integer id = userRepo.getIdByEmail(user.getEmail());
            if (id != null && (action == Crud.CREATE || !id.equals(user.getId()))) {
                return new ResultMsg<>().failure("Email is already taken");
            }
        }

        if (CollectionUtils.isEmpty(user.getRelationList())) {
            missing.add("Positions");
        }
        if (missing.isEmpty()) {
            return new ResultMsg<>().success();
        } else {
            return new ResultMsg<>().failure(
                    String.format("The following fields are required: %s",
                            String.join(",", missing)));
        }
    }

    @Override
    public ResultMsg<?> createUser(User user) {
        ResultMsg<?> validation = isValidUser(user, Crud.CREATE);
        if (!validation.isSuccess()) {
            return validation;
        }
        Integer createBy = contextService.getCurrentUserId();
        //user.setPermission(UserPermissionType.ROLE.name());
        user.setCreateBy(createBy);
        user.setPassword(encoder.encode(defaultPassword));
        userRepo.insert(user);

        sysRelRepo.insertSysRelations(
                user.getRelationList().stream()
                    .peek(x -> {
                        x.setUserId(user.getId());
                        x.setCreateBy(createBy);
                    })
                    .collect(Collectors.toList()));


        //Relation
        SysRelation activePosition = user.getRelationList().stream()
                .filter(x -> x.getIsActive() != null && x.getIsActive())
                .findFirst().orElse(null);

        if (activePosition != null) {
            Integer sysRelId = sysRelRepo.getSysRelationIdByUserRoleGroup(
                    user.getId(), activePosition.getRoleId(), activePosition.getGroupId());
            if (sysRelId != null) {
                userRepo.updateRelationId(user.getId(), sysRelId);
            }
        } else {
            //If user didn't set an active position, will set the first sys_relation record
            userRepo.updateRelationId(user.getId(),
                    sysRelRepo.getFirstSysRelationIdByUser(user.getId()));
        }

        //Permission
        if (MapUtils.isNotEmpty(user.getSelectedPermissions())) {
            List<SysPermission> permissions = user.getSelectedPermissions().keySet()
                    .stream()
                    .map(x -> {
                        SysPermission perm = SysPermission.builder()
                                .userId(user.getId())
                                .resourceId(Integer.valueOf(x))
                                .type(2)
                                .build();
                        perm.setCreateBy(contextService.getCurrentUserId());
                        return perm;
                    })
                    .collect(Collectors.toList());
            sysResourceService.insertSysPermissionsUserBased(permissions);
        }

        return new ResultMsg<>().success("User created");
    }

    @Override
    public ResultPage<User> getUserList(PagingRequest<User> request) {
        List<User> userList = userRepo.getUserList(request);
        int totalCount = request.getStart() == 0 && userList.size() < request.getLimit()
                ? userList.size() : userRepo.getUserListCount(request);
        return ResultPage.<User>builder()
                .data(userList)
                .pageStart(request.getStart())
                .pageSize(request.getLimit())
                .totalCount(totalCount)
                .build();
    }

    @Override
    public ResultMsg<User> getUser(Integer id) {
        User user = userRepo.getUser(id);
        if (user == null) {
            return new ResultMsg<User>().failure("User not found");
        }
        List<SysRelation> relations = sysRelRepo.getSysRelationsByUser(id);

        user.setRelationList(relations.stream()
                .peek(x -> x.setIsActive(user.getRelationId() != null && user.getRelationId().equals(x.getId())))
                .collect(Collectors.toList()));

        List<SysResource> resources = sysResourceService.getAllResources();
        List<SysPermission> permissions = sysResourceService.getSysPermissionsByUserId(id);

        List<ResourceNode<SysResource>> permissionTree = sysResourceService.convertSysResourceListToListResourceNode(
                resources.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), resources
        );
        Map<String, ResourceNodeCheck> selectedPerms = sysResourceService.convertSysPermissionListToPermissionNodeCheckMap(
                permissions, resources
        );
        user.setPermissionTree(permissionTree);
        user.setSelectedPermissions(selectedPerms);

        return new ResultMsg<User>().success(user);
    }

    @Override
    public ResultMsg<?> updateUser(User user) {
        ResultMsg<?> validation = isValidUser(user, Crud.UPDATE);
        if (!validation.isSuccess()) {
            return validation;
        }

        userRepo.updateUser(user);

        //Relation
        if (CollectionUtils.isEmpty(user.getRelationList())) {
            sysRelRepo.deleteSysRelationByUser(user.getId());
            user.setRelationId(null);
        } else {
            List<SysRelation> currentRelations = sysRelRepo.getSysRelationsByUser(user.getId());
            List<String> currentRelationStr = currentRelations.stream()
                    .map(x -> String.format("%s-%s", x.getRoleId(), x.getGroupId()))
                    .collect(Collectors.toList());

            List<String> newRelationStr = user.getRelationList().stream()
                    .map(x -> String.format("%s-%s", x.getRoleId(), x.getGroupId()))
                    .collect(Collectors.toList());

            List<SysRelation> toAdd = user.getRelationList().stream()
                    .filter(x -> !currentRelationStr.contains(String.format("%s-%s", x.getRoleId(), x.getGroupId())))
                    .peek(x -> x.setUserId(user.getId()))
                    .collect(Collectors.toList());

            List<SysRelation> toDelete = currentRelations.stream()
                    .filter(x -> !newRelationStr.contains(String.format("%s-%s", x.getRoleId(), x.getGroupId())))
                    .collect(Collectors.toList());

            log.info("updateUser | toAdd: " + toAdd.size());
            log.info("updateUser | toDelete: " + toDelete.size());

            if (CollectionUtils.isNotEmpty(toAdd)) {
                sysRelRepo.insertSysRelations(toAdd.stream()
                        .peek(x -> x.setCreateBy(contextService.getCurrentUserId()))
                        .collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(toDelete)) {
                sysRelRepo.deleteSysRelationByIds(toDelete.stream()
                        .map(SysRelation::getId).collect(Collectors.toList()));
            }

            SysRelation activePosition = user.getRelationList().stream()
                    .filter(SysRelation::getIsActive)
                    .findFirst().orElse(null);

            user.setRelationId(activePosition != null
                            ? sysRelRepo.getSysRelationIdByUserRoleGroup(
                            user.getId(),
                            activePosition.getRoleId(),
                            activePosition.getGroupId()) : null);
        }

        //Permission
        List<SysPermission> newPermissions = user.getSelectedPermissions().keySet()
                .stream()
                .map(x -> {
                    SysPermission perm = SysPermission.builder()
                            .userId(user.getId())
                            .resourceId(Integer.valueOf(x))
                            .type(2)
                            .build();
                    perm.setCreateBy(contextService.getCurrentUserId());
                    return perm;
                })
                .collect(Collectors.toList());

        List<SysPermission> currentPermissions = sysResourceService.getSysPermissionsByUserId(user.getId());

        List<Integer> newPermsIds = user.getSelectedPermissions().keySet().stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        List<Integer> currentPermsIds = currentPermissions.stream()
                .map(SysPermission::getId)
                .collect(Collectors.toList());

        List<SysPermission> toAdd = CollectionUtils.isEmpty(currentPermissions)
                ? newPermissions
                : CollectionUtils.isNotEmpty(newPermissions)
                    ? newPermissions.stream()
                        .filter(x -> !currentPermsIds.contains(x.getId())).collect(Collectors.toList())
                    : null;

        List<Integer> toDelete = currentPermsIds.stream()
                .filter(x -> !newPermsIds.contains(x))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(toAdd)) {
            sysResourceService.insertSysPermissionsUserBased(toAdd);
        }

        if (CollectionUtils.isNotEmpty(toDelete)) {
            sysResourceService.deletePermissionsByIds(toDelete);
        }

        userRepo.updateUser(user);
        return new ResultMsg<>().success("User updated");
    }

    @Override
    public ResultMsg<?> updateUserDetails(User user) {
        if (!user.isCompleteInput()) {
            return new ResultMsg<>().failure("Required fields must be filled");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("needRelog", false);
        User currentUser = userRepo.getById(user.getId());
        if (!currentUser.getUsername().equals(user.getUsername())) {
            Integer id = userRepo.getIdByUsername(user.getUsername());
            if (id != null && !id.equals(user.getId())) {
                return new ResultMsg<>().failure("Username is already taken");
            }
            result.put("needRelog", true); //Force user to relog when there's a change in username
        }

        if (StringUtils.isNotBlank(user.getEmail())) {
            Integer id = userRepo.getIdByEmail(user.getEmail());
            if (id != null && !id.equals(user.getId())) {
                return new ResultMsg<>().failure("Email is already in use");
            }
        }
        userRepo.updateDetails(user);
        return new ResultMsg<>().success(result, "Details updated");
    }

    @Override
    public ResultMsg<?> updatePassword(Integer userId, String oldPassword, String newPassword) {
        if (StringUtils.isBlank(oldPassword)) {
            return new ResultMsg<>().failure("Input old password");
        }
        if (StringUtils.isBlank(newPassword)) {
            return new ResultMsg<>().failure("New password cannot be empty");
        }

        String currentPassword = userRepo.getPassword(userId);
        if (!encoder.matches(oldPassword, currentPassword)) {
            return new ResultMsg<>().failure("Old password doesn't match");
        }

        userRepo.updatePassword(new User(userId, encoder.encode(newPassword)));
        return new ResultMsg<>().success("Password updated");
    }

    @Override
    public ResultMsg<?> updateUserActivePosition(Integer userId, Integer relationId) {
        //Validation
        if (userId == null) {
            return new ResultMsg<>().failure("User is empty");
        }
        if (relationId == null) {
            return new ResultMsg<>().failure("No active position set");
        }
        userRepo.updateRelationId(userId, relationId);
        return new ResultMsg<>().success("Active position updated");
    }

    @Override
    public void updateUserEnabled(Integer userId, Boolean enabled) {
        userRepo.updateEnabled(userId, enabled);
    }

    @Override
    public ResultMsg<?> deleteUser(Integer id) {
        try {
            sysRelRepo.deleteSysRelationByUser(id);
            sysResourceService.deleteSysPermissionsByUserId(id);
            userRepo.deleteUser(id);
            return new ResultMsg<>().success("User deleted");
        } catch (Exception e) {
            return new ResultMsg<>().failure("Error deleting user");
        }
    }

}
