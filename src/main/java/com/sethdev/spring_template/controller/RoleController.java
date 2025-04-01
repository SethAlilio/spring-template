package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.Role;
import com.sethdev.spring_template.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/role")
@CrossOrigin
@Slf4j
public class RoleController {

    @Autowired
    RoleService roleService;

    @PostMapping("/create")
    public ResultMsg<?> createRole(@RequestBody Role role) {
        try {
            roleService.createRole(role);
            return new ResultMsg<>().success("Role created");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("/get")
    public ResultMsg<Role> getRole(@RequestParam Integer id) {
        try {
            Role role = roleService.getRole(id);
            if (role != null) {
                return new ResultMsg<Role>().success(role);
            } else {
                return new ResultMsg<Role>().failure("Role not found");
            }
        } catch (Exception e) {
            log.info("getRole.e: " + ExceptionUtils.getStackTrace(e));
            return new ResultMsg<Role>().failure("Getting role failed");
        }
    }

    @PostMapping("/list")
    public ResultPage<Role> getRoleList(@RequestBody PagingRequest<Role> request) {
        return roleService.getRoleList(request);
    }

    @PostMapping("/update")
    public ResultMsg<?> updateRole(@RequestBody Role role) {
        try {
            roleService.updateRole(role);
            return new ResultMsg<>().success("Role updated");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultMsg<?> deleteRole(@RequestParam Integer id) {
        try {
            roleService.deleteRole(id);
            return new ResultMsg<>().success("Role deleted");
        } catch (Exception e) {
            return new ResultMsg<>().failure("Failed to delete role");
        }
    }
}
