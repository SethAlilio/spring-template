package com.sethdev.spring_template.service;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.Group;
import com.sethdev.spring_template.models.ResourceNode;

import java.util.List;

public interface GroupService {
    void createGroup(Group group) throws BusinessException;
    List<Group> getAllGroups();

    List<ResourceNode<Group>> getAllGroupAsTree();

    List<ResourceNode<Group>> convertGroupListToListResourceNode(List<Group> currentIteration,
                                                                   List<Group> resources);

    void updateGroup(Group group) throws BusinessException;
    void deleteGroup(Integer id);
}
