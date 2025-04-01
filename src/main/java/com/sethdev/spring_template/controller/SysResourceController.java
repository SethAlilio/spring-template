package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.service.SysResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
