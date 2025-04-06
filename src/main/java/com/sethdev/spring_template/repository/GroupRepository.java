package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.Group;
import com.sethdev.spring_template.models.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GroupRepository {

    void createGroup(Group group);
    List<Group> getAllGroups();
    String getPath(Integer id);
    void updateGroup(Group group);
    void updatePath(@Param("id") Integer id,
                    @Param("path") String path);
    void deleteGroup(@Param("id") Integer id,
                     @Param("path") String path);
    void deleteSysRelationByGroup(@Param("id") Integer id,
                                  @Param("path") String path);

}
