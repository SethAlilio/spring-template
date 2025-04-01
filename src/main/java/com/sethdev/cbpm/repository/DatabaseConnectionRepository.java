package com.sethdev.cbpm.repository;

import com.sethdev.cbpm.models.DatabaseConnection;
import com.sethdev.cbpm.payload.request.PagingRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DatabaseConnectionRepository {

    void insert(DatabaseConnection connection);

    DatabaseConnection get(Integer id);

    List<DatabaseConnection> list(PagingRequest request);

    int listCount(PagingRequest request);

    List<DatabaseConnection> listAll();

    void update(DatabaseConnection connection);

    void delete(Integer id);

}
