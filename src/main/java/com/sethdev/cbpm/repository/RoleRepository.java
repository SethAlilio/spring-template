package com.sethdev.cbpm.repository;

import com.sethdev.cbpm.models.ERole;
import com.sethdev.cbpm.models.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface RoleRepository {
    Optional<Role> findByName(ERole name);
}
