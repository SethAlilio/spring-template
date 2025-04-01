package com.sethdev.cbpm.service;

import com.sethdev.cbpm.exception.BusinessException;
import com.sethdev.cbpm.models.DatabaseConnection;
import com.sethdev.cbpm.models.ResultMsg;
import com.sethdev.cbpm.models.ResultPage;
import com.sethdev.cbpm.payload.request.PagingRequest;

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
