package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.ERole;
import com.sethdev.spring_template.models.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface RoleRepository {
    Optional<Role> findByName(ERole name);
}
