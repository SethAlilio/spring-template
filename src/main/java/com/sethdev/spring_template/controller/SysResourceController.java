package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.AppMenuItem;
import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.service.SysResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/resource")
@CrossOrigin
@Slf4j
public class SysResourceController {

    @Autowired
    SysResourceService sysResourceService;

    @PostMapping("/resourceTree")
    public ResultMsg<List<ResourceNode<Integer>>> getResourceNodeTree() {
        try {
            return new ResultMsg<List<ResourceNode<Integer>>>()
                    .success(sysResourceService.getAllResourcesAsPermissionNodeList());
        } catch (Exception e) {
            return new ResultMsg<List<ResourceNode<Integer>>>().failure("Error getting resources tree");
        }
    }

    @PostMapping("/appMenu")
    public ResultMsg<List<AppMenuItem>> getUserAppMenuItems(@RequestParam("userId") Integer userId) {
        try {
            return new ResultMsg<List<AppMenuItem>>()
                    .success(sysResourceService.getUserAppMenuItems(userId));
        } catch (Exception e) {
            return new ResultMsg<List<AppMenuItem>>().failure("Error getting app menus");
        }
    }


}
