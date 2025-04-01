package com.sethdev.spring_template.service;

import com.sethdev.spring_template.exception.BusinessException;
import com.sethdev.spring_template.models.DatabaseConnection;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.payload.request.PagingRequest;

import java.util.List;

public interface DatabaseConnectionService {
    void createDatabaseConnection(DatabaseConnection connection) throws BusinessException;

    ResultPage<DatabaseConnection> getDatabaseConnectionList(PagingRequest request);

    List<DatabaseConnection> getOverallDatabaseConnectionList();

    ResultMsg<DatabaseConnection> getDatabaseConnectionDetails(Integer id);

    void updateDatabaseConnection(DatabaseConnection connection) throws BusinessException;

    void deleteDatabaseConnection(Integer id);

    //TODO: Different connection for different databases (Currently for MySQL only)
    boolean testDatabaseConnection(DatabaseConnection connection);

    List<String> getDatabaseTableNames(Integer id);
}
