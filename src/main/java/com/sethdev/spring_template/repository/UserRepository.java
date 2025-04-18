package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
@Repository
public interface UserRepository {
    void insert(User user);

    User getById(Integer id);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Integer getIdByUsername(String username);

    Integer getIdByEmail(String email);

    List<User> getUserList(PagingRequest<User> params);
    int getUserListCount(PagingRequest<User> params);

    User getUser(Integer id);

    List<User> getUsersFromGroup(PagingRequest<Map<String, Object>> params);
    int getUsersByGroupIdCount(PagingRequest<Map<String, Object>> params);

    String getPassword(Integer id);

    void updateUser(User user);

    void updateDetails(User user);

    void updatePassword(User user);

    void updateRelationId(@Param("id") Integer id,
                          @Param("relationId") Integer relationId);
    void updateEnabled(@Param("id") Integer id,
                       @Param("enabled") Boolean enabled);

    void deleteUser(Integer id);
}
