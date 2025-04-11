package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.sys.SysRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface SysRelationRepository {

    void insertSysRelations(@Param("relations") List<SysRelation> relations);

    List<SysRelation> getSysRelationsByUser(Integer userId);

    Integer getFirstSysRelationIdByUser(Integer userId);

    Integer getSysRelationIdByUserRoleGroup(@Param("userId") Integer userId,
                                            @Param("roleId") Integer roleId,
                                            @Param("groupId") Integer groupId);

    void deleteSysRelationByIds(@Param("ids") List<Integer> ids);

    void deleteSysRelationByUser(Integer userId);
    void deleteSysRelationByRole(Integer roleId);
    void deleteSysRelationByGroup(Integer groupId);
}
