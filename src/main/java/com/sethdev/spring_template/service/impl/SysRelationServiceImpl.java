package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.SysRelation;
import com.sethdev.spring_template.repository.SysRelationRepository;
import com.sethdev.spring_template.service.SysRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRelationServiceImpl implements SysRelationService {

    @Autowired
    SysRelationRepository sysRelRepo;

    @Override
    public ResultPage<SysRelation> getSysRelationList(PagingRequest<SysRelation> request) {
        List<SysRelation> relations = sysRelRepo.getSysRelationList(request);
        int totalCount = relations.size() < request.getLimit()
                ? relations.size() + (request.getLimit() * (request.getStart() - 1))
                : sysRelRepo.getSysRelationListCount(request);
        return ResultPage.<SysRelation>builder()
                .data(relations)
                .pageStart(request.getStart())
                .pageSize(request.getLimit())
                .totalCount(totalCount)
                .build();
    }

}