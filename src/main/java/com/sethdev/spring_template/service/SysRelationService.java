package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.SysRelation;

public interface SysRelationService {
    ResultPage<SysRelation> getSysRelationList(PagingRequest<SysRelation> request);
}
