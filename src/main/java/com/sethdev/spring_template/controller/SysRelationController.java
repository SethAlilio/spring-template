package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.SysRelation;
import com.sethdev.spring_template.service.SysRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/relation")
@CrossOrigin
public class SysRelationController {

    @Autowired
    SysRelationService sysRelationService;

    @PostMapping("/list")
    public ResultPage<SysRelation> getSysRelationList(@RequestBody PagingRequest<SysRelation> request) {
        return sysRelationService.getSysRelationList(request);
    }

}
