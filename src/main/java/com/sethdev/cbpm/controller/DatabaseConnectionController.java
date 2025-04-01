package com.sethdev.cbpm.controller;

import com.sethdev.cbpm.exception.BusinessException;
import com.sethdev.cbpm.models.DatabaseConnection;
import com.sethdev.cbpm.models.ResultMsg;
import com.sethdev.cbpm.models.ResultPage;
import com.sethdev.cbpm.models.constants.BackupAdvanceSetting;
import com.sethdev.cbpm.models.constants.Crud;
import com.sethdev.cbpm.payload.request.PagingRequest;
import com.sethdev.cbpm.service.DatabaseConnectionService;
import com.sethdev.cbpm.util.MapBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/api/dbconn")
@CrossOrigin
@Slf4j
public class DatabaseConnectionController {

    @Autowired
    DatabaseConnectionService dbConnService;

    @PostMapping("create")
    public ResultMsg<?> createDatabaseConnection(@RequestBody DatabaseConnection connection) {
        try {
            dbConnService.createDatabaseConnection(connection);
            return new ResultMsg<>().success("Database connection saved");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("list")
    public ResultPage<DatabaseConnection> getDatabaseConnectionList(@RequestBody PagingRequest request) {
        return dbConnService.getDatabaseConnectionList(request);
    }

    @PostMapping("details")
    public ResultMsg<DatabaseConnection> getDatabaseConnectionDetails(Integer id) {
        return dbConnService.getDatabaseConnectionDetails(id);
    }

    @PostMapping("listAll")
    public ResultMsg<List<DatabaseConnection>> getOverallDatabaseConnectionList() {
        try {
            List<DatabaseConnection> list = dbConnService.getOverallDatabaseConnectionList();
            return new ResultMsg<List<DatabaseConnection>>().success(list);
        } catch (Exception e) {
            return new ResultMsg<List<DatabaseConnection>>().failure("Error getting database connections");
        }
    }

    @PostMapping("update")
    public ResultMsg<?> updateDatabaseConnection(@RequestBody DatabaseConnection connection) {
        try {
            dbConnService.updateDatabaseConnection(connection);
            return new ResultMsg<>().success("Database connection saved");
        } catch (BusinessException e) {
            return new ResultMsg<>().failure(e.getMessage());
        }
    }

    @PostMapping("delete")
    public ResultMsg<?> deleteDatabaseConnection(Integer id) {
        try {
            dbConnService.deleteDatabaseConnection(id);
            return new ResultMsg<>().success("Database connection deleted");
        } catch (Exception e) {
            return new ResultMsg<>().failure("Error deleting connection, please try again");
        }
    }

    @PostMapping("test")
    public ResultMsg<?> testDatabaseConnection(@RequestBody DatabaseConnection connection) {
        if (connection.hasMissingInfo(Crud.TEST)) {
            return new ResultMsg<>().failure("Required fields needs to be filled");
        }
        boolean success = dbConnService.testDatabaseConnection(connection);
        return new ResultMsg<>(success, success ? "Connection success" : "Connection failed");
    }

    @PostMapping("testId")
    public ResultMsg<?> testDatabaseConnectionById(@RequestParam Integer id) {
        ResultMsg<DatabaseConnection> connectionResult = dbConnService.getDatabaseConnectionDetails(id);
        if (connectionResult.isSuccess()) {
            if (connectionResult.getData().hasMissingInfo(Crud.TEST)) {
                return new ResultMsg<>().failure("Required fields needs to be filled");
            }
            boolean success = dbConnService.testDatabaseConnection(connectionResult.getData());
            return new ResultMsg<>(success, success ? "Connection success" : "Connection failed");
        } else {
            return new ResultMsg<>().failure(connectionResult.getMessage());
        }
    }

    @PostMapping("tables")
    public ResultMsg<List<String>> getSchemaTables(@RequestParam Integer id) {
        return new ResultMsg<List<String>>().success(dbConnService.getDatabaseTableNames(id));
    }

    @PostMapping("advanceSettings")
    public ResultMsg<List<Map<String, String>>> getAdvanceSettings() {
        return new ResultMsg<List<Map<String, String>>>().success(
                Arrays.stream(BackupAdvanceSetting.values())
                        .map(x -> {
                            return new MapBuilder<String, String>()
                                    .put("key", x.name())
                                    .put("type", x.getType().name())
                                    .put("desc", x.getDescription())
                                    .build();
                        })
                        .collect(Collectors.toList()));
    }


}
