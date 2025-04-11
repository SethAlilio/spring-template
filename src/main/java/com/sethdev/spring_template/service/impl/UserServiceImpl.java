package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.User;
import com.sethdev.spring_template.models.constants.Crud;
import com.sethdev.spring_template.models.constants.UserPermissionType;
import com.sethdev.spring_template.models.sys.SysRelation;
import com.sethdev.spring_template.repository.SysRelationRepository;
import com.sethdev.spring_template.repository.UserRepository;
import com.sethdev.spring_template.service.ContextService;
import com.sethdev.spring_template.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    ContextService contextService;

    @Autowired
    SysRelationRepository sysRelRepo;

    @Autowired
    PasswordEncoder encoder;

    @Value("000000")
    String defaultPassword;

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
        if (StringUtils.isNotBlank(user.getEmail()) && !StringUtils.equals(currentUser.getEmail(), user.getUsername())) {
            Integer id = userRepo.getIdByEmail(user.getEmail());
            if (id != null && !id.equals(user.getId())) {
                return new ResultMsg<>().failure("Email is already in use");
            }
        }
        userRepo.updateDetails(user);
        return new ResultMsg<>().success(result, "Details updated");
    }

    @Override
    public ResultMsg<?> updatePassword(User user) {
        if (StringUtils.isBlank(user.getPassword())) {
            return new ResultMsg<>().failure("Password cannot be empty");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.updatePassword(user);
        return new ResultMsg<>().success("Password updated");
    }

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
        if (StringUtils.isBlank(user.getFullName())) {
            missing.add("Full Name");
        }

        if (StringUtils.isNotBlank(user.getEmail())) {
            Integer id = userRepo.getIdByEmail(user.getEmail());
            if (id != null && (action == Crud.CREATE || !id.equals(user.getId()))) {
                return new ResultMsg<>().failure("Email is already taken");
            }
        }

        if (CollectionUtils.isEmpty(user.getRelationList())) {
            missing.add("Role-Group Relation");
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
        user.setPermission(UserPermissionType.ROLE.name());
        user.setCreateBy(createBy);
        user.setPassword(encoder.encode(defaultPassword));
        userRepo.insert(user);

        sysRelRepo.insertSysRelations(
                user.getRelationList().stream()
                    .map(x -> {
                        x.setUserId(user.getId());
                        x.setCreateBy(createBy);
                        return x;
                    })
                    .collect(Collectors.toList()));


        SysRelation activePosition = user.getRelationList().stream()
                .filter(x -> x.getIsActive() != null && x.getIsActive())
                .findFirst().orElse(null);

        if (activePosition != null) {
            Integer sysRelId = sysRelRepo.getSysRelationIdByUserRoleGroup(
                    user.getId(), activePosition.getRoleId(), activePosition.getGroupId());
            if (sysRelId != null) {
                userRepo.updateRelationId(sysRelId, user.getId());
            }
        } else {
            //If user didn't set an active position, will set the first sys_relation record
            userRepo.updateRelationId(
                    sysRelRepo.getFirstSysRelationIdByUser(user.getId()),
                    user.getId());
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
        return new ResultMsg<User>().success(user);
    }

    @Override
    public ResultMsg<?> updateUser(User user) {
        ResultMsg<?> validation = isValidUser(user, Crud.UPDATE);
        if (!validation.isSuccess()) {
            return validation;
        }

        userRepo.updateUser(user);

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
            /*userRepo.updateRelationId(activePosition != null
                   ? sysRelRepo.getSysRelationIdByUserRoleGroup(
                       user.getId(),
                       activePosition.getRoleId(),
                       activePosition.getGroupId()) : null,
                   user.getId());*/


            user.setRelationId(activePosition != null
                            ? sysRelRepo.getSysRelationIdByUserRoleGroup(
                            user.getId(),
                            activePosition.getRoleId(),
                            activePosition.getGroupId()) : null);
        }
        userRepo.updateUser(user);
        return new ResultMsg<>().success("User updated");
    }

    @Override
    public ResultMsg<?> deleteUser(Integer id) {
        try {
            userRepo.deleteUser(id);
            sysRelRepo.deleteSysRelationByUser(id);
            return new ResultMsg<>().success("User deleted");
        } catch (Exception e) {
            return new ResultMsg<>().failure("Error deleting user");
        }
    }

}
