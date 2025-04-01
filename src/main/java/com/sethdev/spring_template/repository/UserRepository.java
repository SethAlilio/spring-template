package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface UserRepository {
    void save(User user);

    User getById(Integer id);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Integer getIdByUsername(String username);

    Integer getIdByEmail(String email);

    void updateDetails(User user);

    void updatePassword(User user);
}
