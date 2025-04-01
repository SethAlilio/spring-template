package com.sethdev.cbpm.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sethdev.cbpm.exception.BusinessException;
import com.sethdev.cbpm.models.DatabaseConnection;
import com.sethdev.cbpm.models.ResultMsg;
import com.sethdev.cbpm.models.ResultPage;
import com.sethdev.cbpm.models.constants.BackupSchedule;
import com.sethdev.cbpm.models.constants.BackupType;
import com.sethdev.cbpm.models.constants.Crud;
import com.sethdev.cbpm.models.constants.DatabaseType;
import com.sethdev.cbpm.payload.request.DatabaseBackupRequest;
import com.sethdev.cbpm.payload.request.PagingRequest;
import com.sethdev.cbpm.repository.DatabaseConnectionRepository;
import com.sethdev.cbpm.service.ContextService;
import com.sethdev.cbpm.service.DatabaseConnectionService;
import com.sethdev.cbpm.util.LocalDateTimeAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DatabaseConnectionServiceImpl implements DatabaseConnectionService {

    @Autowired
    DatabaseConnectionRepository dbConnRepo;

    @Autowired
    ContextService contextService;

    @Override
    public void createDatabaseConnection(DatabaseConnection connection) throws BusinessException {
        Gson gson = new GsonBuilder()
                         .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                         .create();
        log.info("createDatabaseConnection | connection: " + gson.toJson(connection));
        if (connection.hasMissingInfo(Crud.CREATE)) {
            throw new BusinessException("All required fields needs to be filled");
        }
        connection.setCreateBy(contextService.getCurrentUserId());
        dbConnRepo.insert(connection);
    }

    @Override
    public ResultPage<DatabaseConnection> getDatabaseConnectionList(PagingRequest request) {
        log.info("list | request: " + new Gson().toJson(request));
        List<DatabaseConnection> connectionList = dbConnRepo.list(request);
        int listCount = dbConnRepo.listCount(request);
        int totalCount = request.getStart() == 0 && connectionList.size() < request.getLimit()
                ? connectionList.size() : dbConnRepo.listCount(request);
        //log.info("list | connectionList: " + new Gson().toJson(connectionList));
        log.info("list | listCount: " + listCount);
        connectionList.forEach(x -> x.setTypeName(DatabaseType.getDisplayName(x.getType())));
        return new ResultPage<>(connectionList, totalCount, request.getLimit(), request.getStart());
    }

    @Override
    public List<DatabaseConnection> getOverallDatabaseConnectionList() {
        return dbConnRepo.listAll();
    }

    @Override
    public ResultMsg<DatabaseConnection> getDatabaseConnectionDetails(Integer id) {
        DatabaseConnection connection = dbConnRepo.get(id);
        if (connection == null) {
            return new ResultMsg<DatabaseConnection>().failure("Unknown ID");
        } else {
            return new ResultMsg<DatabaseConnection>().success(connection);
        }
    }

    @Override
    public void updateDatabaseConnection(DatabaseConnection connection) throws BusinessException {
        if (connection.hasMissingInfo(Crud.UPDATE)) {
            throw new BusinessException("All required fields needs to be filled");
        }
        connection.setUpdateBy(contextService.getCurrentUserId());
        dbConnRepo.update(connection);
    }

    @Override
    public void deleteDatabaseConnection(Integer id) {
        dbConnRepo.delete(id);
    }

    //TODO: Different connection for different databases (Currently for MySQL only)
    @Override
    public boolean testDatabaseConnection(DatabaseConnection connection) {
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            //dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(generateDatasourceUrl(connection));
            dataSource.setUsername(connection.getUsername());
            dataSource.setPassword(connection.getPassword());
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String generateDatasourceUrl(DatabaseConnection connection) {
        return String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&useUnicode=true&characterEncoding=" +
                "utf-8&zeroDateTimeBehavior=round&useSSL=true",
                connection.getHost(), connection.getPort(), connection.getSchema());
    }

    @Override
    public List<String> getDatabaseTableNames(Integer id) {
        DatabaseConnection connection = dbConnRepo.get(id);
        if (connection == null) {
            return new ArrayList<>();
        }

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(generateDatasourceUrl(connection));
        dataSource.setUsername(connection.getUsername());
        dataSource.setPassword(connection.getPassword());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query("SHOW TABLES", (rs, rowNum) -> rs.getString(1));
    }


    public ResultMsg<?> validateDatabaseBackupRequest(DatabaseBackupRequest request) {
        List<String> error = new ArrayList<>();
        if (request.getSourceDatabase() == null) {
            return new ResultMsg<>().failure("Source database is required");
        }
        if (request.getDestinationDatabase() == null) {
            return new ResultMsg<>().failure("Destination database is required");
        }
        if (StringUtils.isBlank(request.getType())) {
            return new ResultMsg<>().failure("Export type is required");
        } else {
            BackupType type = BackupType.get(request.getType());
            if (type == null) {
                return new ResultMsg<>().failure("Invalid export type");
            }
        }

        if (CollectionUtils.isEmpty(request.getBackupTableList())) {
            return new ResultMsg<>().failure("No tables to backup are selected");
        }

        if (StringUtils.isBlank(request.getSchedule())) {
            return new ResultMsg<>().failure("Schedule is required");
        } else {
            BackupSchedule schedule = BackupSchedule.get(request.getSchedule());
            if (schedule == null) {
                return new ResultMsg<>().failure("Invalid schedule");
            }
            if (schedule.equals(BackupSchedule.CUSTOM)) {
                if (StringUtils.isBlank(request.getScheduleTime())) {
                    return new ResultMsg<>().failure("Schedule time is required");
                } else {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(request.getScheduleTime());
                    LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    if (localDateTime.isBefore(currentDateTime)) {
                        return new ResultMsg<>().failure("Invalid schedule time, cannot choose a past time");
                    }
                    Duration duration = Duration.between(currentDateTime, localDateTime);
                    if (duration.toMinutes() < 2) {
                        return new ResultMsg<>().failure("Invalid schedule time, Should be at least 2 minutes from now");
                    }
                }
            }
        }

        List<String> destinationTables = getDatabaseTableNames(request.getDestinationDatabase().getId());
        if (CollectionUtils.isNotEmpty(destinationTables)) {
            List<String> existing = request.getBackupTableList().stream()
                    .map(DatabaseBackupRequest.BackupTable::getName)
                    .filter(destinationTables::contains)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(existing)) {
                return new ResultMsg<>().failure("The ");
            }
        }
        return new ResultMsg<>().success();
    }
    public void backupDatabaseTables(DatabaseBackupRequest request) {
        //if ()


    }

}
