package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.Group;
import com.sethdev.spring_template.models.ResourceNode;
import com.sethdev.spring_template.repository.GroupRepository;
import com.sethdev.spring_template.service.ContextService;
import com.sethdev.spring_template.service.GroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    GroupRepository groupRepo;

    @Autowired
    ContextService contextService;

    @Override
    public void createGroup(Group group) throws BusinessException {
        if (StringUtils.isBlank(group.getName())) {
            throw new BusinessException("Name is required");
        }
        group.setCreateBy(contextService.getCurrentUserId());
        groupRepo.createGroup(group);
        if (group.getParentId() != null) {
            String path = groupRepo.getPath(group.getParentId());
            groupRepo.updatePath(group.getId(), String.format("%s.%s", path, group.getId()));
        } else {
            groupRepo.updatePath(group.getId(), String.valueOf(group.getId()));
        }
    }

    @Override
    public List<Group> getAllGroups() {
        return groupRepo.getAllGroups();
    }

    @Override
    public List<ResourceNode<Group>> getAllGroupAsTree() {
        List<Group> groups = groupRepo.getAllGroups();
        return convertGroupListToListResourceNode(
                groups.stream().filter(x -> x.getType().equals(1)).collect(Collectors.toList()), groups
        );
    }

    @Override
    public List<ResourceNode<Group>> convertGroupListToListResourceNode(List<Group> currentIteration,
                                                                        List<Group> resources) {
        if (CollectionUtils.isNotEmpty(currentIteration)) {
            return currentIteration.stream()
                    .map(res -> ResourceNode.<Group>builder()
                            .key(String.valueOf(res.getId()))
                            .label(res.getName())
                            .data(res)
                            .children(convertGroupListToListResourceNode(
                                    resources.stream()
                                            .filter(x -> x.getParentId() != null && x.getParentId().equals(res.getId()))
                                            .collect(Collectors.toList()), resources))
                            .build()
                    )
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateGroup(Group group) throws BusinessException {
        if (StringUtils.isBlank(group.getName())) {
            throw new BusinessException("Name is required");
        }
        groupRepo.updateGroup(group);
    }

    @Override
    public void deleteGroup(Integer id) {
        String path = groupRepo.getPath(id);
        groupRepo.deleteGroup(id, path);
        groupRepo.deleteSysRelationByGroup(id, path);
    }
}
