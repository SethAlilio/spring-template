package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.Group;
import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@CrossOrigin
@Slf4j
public class GroupController {

    @Autowired
    GroupService groupService;

    @PostMapping("/create")
    public ResultMsg<?> createGroup(@RequestBody Group group) {
        try {
            groupService.createGroup(group);
            return new ResultMsg<>().success("Group created");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("/listAll")
    public ResultMsg<List<Group>> getAllGroups() {
        return new ResultMsg<List<Group>>().success(groupService.getAllGroups());
    }

    @PostMapping("/tree")
    public ResultMsg<List<ResourceNode<Group>>> getGroupTree() {
        try {
            return new ResultMsg<List<ResourceNode<Group>>>()
                    .success(groupService.getAllGroupAsTree());
        } catch (Exception e) {
            log.info("getGroupTree.e: " + ExceptionUtils.getStackTrace(e));
            return new ResultMsg<List<ResourceNode<Group>>>().failure("Error getting group tree");
        }
    }

    @PostMapping("/update")
    public ResultMsg<?> updateGroup(@RequestBody Group group) {
        try {
            groupService.updateGroup(group);
            return new ResultMsg<>().success("Group updated");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultMsg<?> deleteGroup(@RequestParam Integer id) {
        try {
            groupService.deleteGroup(id);
            return new ResultMsg<>().success("Group deleted");
        } catch (Exception e) {
            log.info("deleteGroup.e: " + ExceptionUtils.getStackTrace(e));
            return new ResultMsg<>().failure("Failed to delete group");
        }
    }

}
