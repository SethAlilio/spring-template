package com.sethdev.spring_template.service.impl;

import com.google.gson.Gson;
import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.dialog.CustomDialog;
import com.sethdev.spring_template.models.sys.dialog.CustomDialog.*;
import com.sethdev.spring_template.models.sys.dialog.DropdownItem;
import com.sethdev.spring_template.models.sys.dialog.TreeData;
import com.sethdev.spring_template.repository.CustomDialogRepository;
import com.sethdev.spring_template.service.CustomDialogService;
import com.sethdev.spring_template.service.GroovyScriptService;
import com.sethdev.spring_template.util.QueryExecutor;
import com.sethdev.spring_template.util.TypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomDialogServiceImpl implements CustomDialogService {
    @Autowired
    CustomDialogRepository customDialogRepo;

    @Autowired
    QueryExecutor queryExecutor;

    @Autowired
    GroovyScriptService groovyScriptService;

    @Value("${template.app.databaseName}")
    String databaseName;

    //TODO: Make the dialogFilter.filterClauses required if the dialogFilter.type is 'FIELD_FILTER'
    @Override
    public void validateCustomDialog(CustomDialog dialog) throws Exception {
        //Validate for empty basic info
        if (StringUtils.isBlank(dialog.getName())) {
            throw new NullPointerException("Name is empty");
        }
        if (StringUtils.isBlank(dialog.getKey())) {
            throw new NullPointerException("Key is empty");
        }
        if (StringUtils.isBlank(dialog.getType())) {
            throw new NullPointerException("Type is empty");
        }
        if (StringUtils.isBlank(dialog.getQueryTable())) {
            throw new NullPointerException("Query table is empty");
        }

        //There should be at least 1 column
        if ("LIST".equals(dialog.getType())) {
            if (CollectionUtils.isEmpty(dialog.getColumnList())) {
                throw new NullPointerException("Empty columns to display on dialog");
            }
        } else if ("TREE".equals(dialog.getType())) {
            if (CollectionUtils.isEmpty(dialog.getTreeSettings().getReturnDataColumns())) {
                throw new NullPointerException("Empty returned columns on dialog");
            }
            CustomDialog.Column idCol = null;
            CustomDialog.Column parentIdCol = null;

            //ID column and parent ID column should be set
            if (StringUtils.isBlank(dialog.getTreeSettings().getIdColumn())) {
                throw new Exception("No ID column selected");
            } else {
                //Check if id column is still valid (For cases that the parent id column set is removed)
                idCol = dialog.getTreeSettings().getReturnDataColumns().stream()
                        .filter(x -> x.getReturnName().equals(dialog.getTreeSettings().getIdColumn()))
                        .findFirst()
                        .orElse(null);
                if (idCol == null) {
                    throw new Exception("Invalid ID column");
                }
            }

            if (StringUtils.isBlank(dialog.getTreeSettings().getParentIdColumn())) {
                throw new Exception("No Parent ID column selected");
            } else {
                //Check if parent id column is still valid (For cases that the parent id column set is removed)
                parentIdCol = dialog.getTreeSettings().getReturnDataColumns().stream()
                        .filter(x -> x.getReturnName().equals(dialog.getTreeSettings().getParentIdColumn()))
                        .findFirst()
                        .orElse(null);
                if (parentIdCol == null) {
                    throw new Exception("Invalid Parent ID column");
                }
            }

            if (StringUtils.equals(idCol.getReturnName(), parentIdCol.getReturnName())) {
                throw new Exception("ID column and parent ID column must not be the same");
            }
        }

        //Column name, display name, and return name are all required
        List<CustomDialog.Column> emptyFields = dialog.getColumnList().stream()
                .filter(CustomDialog.Column::hasEmptyFields)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(emptyFields)) {
            throw new NullPointerException("Empty field properties: column name, display name, and return name cannot be empty");
        }

        //Check for duplicate column name or return name
        List<String> duplicateColumns = dialog.listDuplicateColumnNames();
        if (CollectionUtils.isNotEmpty(duplicateColumns)) {
            throw new Exception("Duplicate columns: " + Strings.join(duplicateColumns, ','));
        }
        List<String> duplicateReturnNames = dialog.listDuplicateReturnNames();
        if (CollectionUtils.isNotEmpty(duplicateReturnNames)) {
            throw new Exception("Duplicate return names: " + Strings.join(duplicateReturnNames, ','));
        }

        //For LIST, at least 1 column must be shown. For TREE, only 1 column can be shown
        List<CustomDialog.Column> shownColumns = dialog.listShownColumns();
        if (CollectionUtils.isEmpty(shownColumns)) {
            throw new Exception("At least 1 column should be shown");
        } else if (dialog.getType().equals("TREE") && CollectionUtils.size(shownColumns) > 1) {
            throw new Exception("There should be only 1 shown column");
        }

        //Check if key is used
        Integer id = customDialogRepo.getIdByKey(dialog.getKey());
        if ((dialog.getId() == null && id != null)
                || (dialog.getId() != null && !dialog.getId().equals(id))) {
            throw new Exception("Key already taken");
        }
    }

    @Override
    public CustomDialog save(CustomDialog dialog) throws Exception {
        if ("LIST".equals(dialog.getType())) {
            dialog.setupColumnsOnChangeEnabled();
            dialog.setupSearchSettings();
            dialog.setColumns(new Gson().toJson(dialog.getColumnList()));
        } else if ("TREE".equals(dialog.getType())) {
            dialog.getTreeSettings().setReturnDataColumns(dialog.getColumnList());
            dialog.setColumns(new Gson().toJson(dialog.getTreeSettings()));
        }
        dialog.setFilter(new Gson().toJson(dialog.getDialogFilter()));
        dialog.setLayout(new Gson().toJson(dialog.getLayoutSettings()));
        this.validateCustomDialog(dialog);
        if (dialog.getId() == null) {
            customDialogRepo.create(dialog);
        } else {
            customDialogRepo.update(dialog);
        }
        return dialog;
    }

    @Override
    public void create(CustomDialog dialog) throws Exception {
        if ("LIST".equals(dialog.getType())) {
            dialog.setColumns(new Gson().toJson(dialog.getColumnList()));
        } else if ("TREE".equals(dialog.getType())) {
            dialog.getTreeSettings().setReturnDataColumns(dialog.getColumnList());
            dialog.setColumns(new Gson().toJson(dialog.getTreeSettings()));
        }
        dialog.setFilter(new Gson().toJson(dialog.getDialogFilter()));
        dialog.setLayout(new Gson().toJson(dialog.getLayoutSettings()));
        this.validateCustomDialog(dialog);
        customDialogRepo.create(dialog);
    }

    @Override
    public void update(CustomDialog dialog) throws Exception {
        if ("LIST".equals(dialog.getType())) {
            dialog.setColumns(new Gson().toJson(dialog.getColumnList()));
        } else if ("TREE".equals(dialog.getType())) {
            dialog.getTreeSettings().setReturnDataColumns(dialog.getColumnList());
            dialog.setColumns(new Gson().toJson(dialog.getTreeSettings()));
        }
        dialog.setFilter(new Gson().toJson(dialog.getDialogFilter()));
        dialog.setLayout(new Gson().toJson(dialog.getLayoutSettings()));
        this.validateCustomDialog(dialog);
        customDialogRepo.update(dialog);
    }

    @Override
    public CustomDialog getByKey(String dialogKey, boolean forEdit) {
        CustomDialog dialog = customDialogRepo.getByKey(dialogKey);
        if (dialog != null) {
            dialog.parseSettingsThenClear();
            String pkCol = customDialogRepo.getPrimaryKeyOfTable(dialog.getQueryTable());

            //Get the values for search fields with type `DROPDOWN`
            if (!forEdit) {
                dialog.setupSearchSettings();
                dialog.setupOnChangeSubscribers();
                dialog.getColumnList().forEach(x -> {
                    if (x.getSearchSettings() == null) {
                        x.setSearchSettings(new CustomDialog.SearchSettings("TEXT"));
                    }
                    else {
                        SearchFieldType fieldType = SearchFieldType
                                .get(x.getSearchSettings().getType());
                        SearchFieldDataSource dataSrc = SearchFieldDataSource
                                .get(x.getSearchSettings().getDataSrcType());
                        if (SearchFieldType.DROPDOWN.equals(fieldType) || SearchFieldType.MULTISELECT.equals(fieldType)) {
                            List<DropdownItem> options = new ArrayList<>();
                            if (SearchFieldDataSource.LIST.equals(dataSrc)) {
                                List<DropdownItem> items = TypeUtils.stringToList(
                                        x.getSearchSettings().getDataSrcValue(), DropdownItem.class);
                                if (CollectionUtils.isNotEmpty(items)) options.addAll(items);
                            }
                            else if (SearchFieldDataSource.SQL.equals(dataSrc)) {
                                String sql = dialog.applyParamToSearchSettingsSql(x.getSearchSettings().getDataSrcValue());
                                List<Map<String, Object>> items = queryExecutor.select(sql);
                                if (CollectionUtils.isNotEmpty(items))
                                    options.addAll(TypeUtils.parameterizeList(items, DropdownItem.class));
                            }
                            else if (SearchFieldDataSource.SCRIPT.equals(dataSrc)) {
                                List<DropdownItem> items = groovyScriptService.runForCustomDialogSearchFieldScriptColumn(
                                        x.getSearchSettings().getDataSrcValue(), dialog.getFilterParams(),
                                        dialog.getSearchParams());
                                if (CollectionUtils.isNotEmpty(items)) options.addAll(items);
                            }
                            x.getSearchSettings().setSelection(options);
                        } else if (SearchFieldType.TREE.equals(fieldType)) {
                            if (SearchFieldDataSource.LIST.equals(dataSrc)) {
                                //TODO
                            }
                            else if (SearchFieldDataSource.SQL.equals(dataSrc)) {
                                //TODO
                            }
                            else if (SearchFieldDataSource.SCRIPT.equals(dataSrc)) {
                                //TODO
                            }
                        }
                    }
                });
            }

            dialog.setPkColumn(dialog.getColumnList().stream()
                    .filter(x -> x.getColumnName().equals(pkCol))
                    .map(CustomDialog.Column::getReturnName)
                    .findFirst().orElse(null));

            //Set default filter
            if (dialog.getDialogFilter() == null) {
                dialog.setDialogFilter(new CustomDialog.DialogFilter(FilterType.NONE.name()));
            }

            //Set default layout settings
            if (dialog.getLayoutSettings() == null) {
                dialog.setLayoutSettings(new CustomDialog.LayoutSettings(
                        CustomDialog.LayoutType.SINGLE.name(), 1, false, false
                ));
            }

        }
        return dialog;
    }

    @Override
    public List<CustomDialog.Column> getColumns(Integer id) {
        CustomDialog dialog = CustomDialog.builder()
                .id(id)
                .columns(customDialogRepo.getDialogColumns(id))
                .build();
        dialog.parseColumns();
        return dialog.getColumnList();
    }

    /** Query data for `LIST` type dialogs */
    @Override
    public ResultPage<Map<String, Object>> getDialogDataListV2(Map<String, Object> params) {
        try {
            CustomDialog dialog = this.getByKey(params.get("dialogKey").toString(), false);

            List<CustomDialog.Column> searchColumns = dialog.getColumnList().stream()
                    .filter(x -> x.getSearchable() != null && x.getSearchable())
                    .collect(Collectors.toList());

            String searchData = MapUtils.getString(params, "search");
            Map<String, Object> searchMap = com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("searchData"));
            FilterType filterType = dialog.getFilterType();

            if (FilterType.NONE.equals(filterType)) {
                List<String> selectColumns = dialog.getColumnsForSql();
                StringBuilder whereSql = new StringBuilder();

                //Merge the search condition
                this.mergeSearchQuery(dialog, whereSql, searchColumns, searchData, searchMap);

                String where = whereSql.toString();

                //Form the final query for list and count
                String sql = "SELECT " + String.join(", ", selectColumns)
                        + " FROM " + dialog.getQueryTable()
                        + (StringUtils.isNotBlank(where) ? " WHERE" + where : "")
                        + " LIMIT " + params.get("start") + ", " + params.get("limit");

                String sqlCount = "SELECT COUNT(0) FROM " + dialog.getQueryTable()
                        + (StringUtils.isNotBlank(where) ? " WHERE " + where : "");

                //Execute the select queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                List<CustomDialog.Column> scriptCols = dialog.getColumnList().stream()
                        .filter(x -> CustomDialog.ColumnType.SCRIPT.name().equals(x.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptCols)) {
                    dataList = this.applyScriptColumnToQueryResult(scriptCols, dataList);
                }
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);

                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize((int) params.get("limit"))
                        .pageStart((int) params.get("start"))
                        .build();
            }
            else if (FilterType.CUSTOM_QUERY.equals(filterType)) {
                //Apply filter params & merge the where clause for search fields
                Map<String, Object> dataParam = com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("param"));
                dialog.setFilterParams(dataParam);
                Pair<String, String> sqlPair = this.mergeSearchQuery(dialog, searchColumns, searchData, searchMap);
                String sql = sqlPair.getLeft();
                String sqlCount = sqlPair.getRight();

                //Execute queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);

                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize((int) params.get("limit"))
                        .pageStart((int) params.get("start"))
                        .build();
            }
            else if (FilterType.FIELD_FILTER.equals(filterType)) {
                List<String> selectColumns = dialog.getColumnsForSql();

                //Merge the field filter condition
                StringBuilder whereSql = new StringBuilder();
                whereSql.append("(");
                List<FilterClause> filterClauses = dialog.getDialogFilter().getFilterClauses();
                AtomicInteger index = new AtomicInteger(0);
                filterClauses.forEach(x -> {
                    int i = index.getAndIncrement();
                    String sql = x.format(i != 0);
                    whereSql.append(sql).append(i + 1 < filterClauses.size() ? " " : "");
                });
                whereSql.append(")");

                //Merge the search condition
                this.mergeSearchQuery(dialog, whereSql, searchColumns, searchData, searchMap);

                //Form the final query for list and count
                String sql = "SELECT " + String.join(", ", selectColumns) + " FROM " + dialog.getQueryTable()
                        + " WHERE " + whereSql + " LIMIT " + params.get("start") + ", " + params.get("limit");

                String sqlCount = "SELECT COUNT(0) FROM " + dialog.getQueryTable() + " WHERE " + whereSql;

                //Apply custom params if available (Custom params are enclosed with curly brackets {})
                Map<String, Object> dataParam = com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("param"));
                dialog.setFilterParams(dataParam);
                Pair<String, String> sqlPair = dialog.applyParamToSql(sql, sqlCount);
                sql = sqlPair.getLeft();
                sqlCount = sqlPair.getRight();

                //Execute the select queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                List<CustomDialog.Column> scriptCols = dialog.getColumnList().stream()
                        .filter(x -> CustomDialog.ColumnType.SCRIPT.name().equals(x.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptCols)) {
                    dataList = this.applyScriptColumnToQueryResult(scriptCols, dataList);
                }
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);


                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize((int) params.get("limit"))
                        .pageStart((int) params.get("start"))
                        .build();
            }return ResultPage.<Map<String, Object>>builder()
                    .data(null)
                    .totalCount(0)
                    .pageSize((int) params.get("limit"))
                    .pageStart((int) params.get("start"))
                    .build();
        } catch (Exception e) {
            log.info("[CustomDialog] e: " + ExceptionUtils.getStackTrace(e));
            return new ResultPage<>();
        }
    }

    @Override
    public ResultPage<Map<String, Object>> getDialogDataListV3(PagingRequest<CustomDialog> request) {
        try {
            CustomDialog dialog = this.getByKey(request.getQuery().getKey(), false);
            log.info("getDialogDataListV3 | dialog: " + new Gson().toJson(dialog));

            List<CustomDialog.Column> searchColumns = dialog.getColumnList().stream()
                    .filter(x -> x.getSearchable() != null && x.getSearchable())
                    .collect(Collectors.toList());
            log.info("getDialogDataListV3 | searchColumns: " + new Gson().toJson(searchColumns));
            log.info("getDialogDataListV3 | searchParams: " + new Gson().toJson(request.getQuery().getSearchParams()));

            String searchData = MapUtils.getString(request.getQuery().getSearchParams(), "search");
            log.info("getDialogDataListV3 | searchData: " + searchData);
            Map<String, Object> searchMap = com.sethdev.spring_template.util.MapUtils.objectToMap(
                    request.getQuery().getSearchParams().get("searchData"));
            log.info("getDialogDataListV3 | searchMap: " + new Gson().toJson(searchMap));
            FilterType filterType = dialog.getFilterType();
            log.info("getDialogDataListV3 | filterType: " + filterType);

            if (FilterType.NONE.equals(filterType)) {
                log.info("getDialogDataListV3 | [NONE]");
                List<String> selectColumns = dialog.getColumnsForSql();
                StringBuilder whereSql = new StringBuilder();

                //Merge the search condition
                this.mergeSearchQuery(dialog, whereSql, searchColumns, searchData, searchMap);

                String where = whereSql.toString();

                //Form the final query for list and count
                String sql = "SELECT " + String.join(", ", selectColumns)
                        + " FROM " + dialog.getQueryTable()
                        + (StringUtils.isNotBlank(where) ? " WHERE" + where : "")
                        + " LIMIT " + request.getStart() + ", " + request.getLimit();

                log.info("getDialogDataListV3 | [NONE] sql: " + sql);

                String sqlCount = "SELECT COUNT(0) FROM " + dialog.getQueryTable()
                        + (StringUtils.isNotBlank(where) ? " WHERE " + where : "");
                log.info("getDialogDataListV3 | [NONE] sqlCount: " + sqlCount);

                //Execute the select queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                List<CustomDialog.Column> scriptCols = dialog.getColumnList().stream()
                        .filter(x -> CustomDialog.ColumnType.SCRIPT.name().equals(x.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptCols)) {
                    dataList = this.applyScriptColumnToQueryResult(scriptCols, dataList);
                }
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);

                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize(request.getLimit())
                        .pageStart(request.getStart())
                        .build();
            }
            else if (FilterType.CUSTOM_QUERY.equals(filterType)) {
                log.info("getDialogDataListV3 | [CUSTOM_QUERY]");
                //Apply filter params & merge the where clause for search fields
                Map<String, Object> dataParam = com.sethdev.spring_template.util.MapUtils.objectToMap(
                        request.getQuery().getSearchParams().get("param"));
                dialog.setFilterParams(dataParam);
                Pair<String, String> sqlPair = this.mergeSearchQuery(dialog, searchColumns, searchData, searchMap);
                String sql = sqlPair.getLeft();
                String sqlCount = sqlPair.getRight();

                //Execute queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);

                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize(request.getLimit())
                        .pageStart(request.getStart())
                        .build();
            }
            else if (FilterType.FIELD_FILTER.equals(filterType)) {
                log.info("getDialogDataListV3 | [FIELD_FILTER]");
                List<String> selectColumns = dialog.getColumnsForSql();

                //Merge the field filter condition
                StringBuilder whereSql = new StringBuilder();
                whereSql.append("(");
                List<FilterClause> filterClauses = dialog.getDialogFilter().getFilterClauses();
                AtomicInteger index = new AtomicInteger(0);
                filterClauses.forEach(x -> {
                    int i = index.getAndIncrement();
                    String sql = x.format(i != 0);
                    whereSql.append(sql).append(i + 1 < filterClauses.size() ? " " : "");
                });
                whereSql.append(")");

                //Merge the search condition
                this.mergeSearchQuery(dialog, whereSql, searchColumns, searchData, searchMap);

                //Form the final query for list and count
                String sql = "SELECT " + String.join(", ", selectColumns) + " FROM " + dialog.getQueryTable()
                        + " WHERE " + whereSql + " LIMIT " + request.getStart() + ", " + request.getLimit();

                String sqlCount = "SELECT COUNT(0) FROM " + dialog.getQueryTable() + " WHERE " + whereSql;

                //Apply custom params if available (Custom params are enclosed with curly brackets {})
                Map<String, Object> dataParam = com.sethdev.spring_template.util.MapUtils.objectToMap(
                        request.getQuery().getSearchParams().get("param"));
                dialog.setFilterParams(dataParam);
                Pair<String, String> sqlPair = dialog.applyParamToSql(sql, sqlCount);
                sql = sqlPair.getLeft();
                sqlCount = sqlPair.getRight();

                //Execute the select queries
                List<Map<String, Object>> dataList = queryExecutor.select(sql);
                List<CustomDialog.Column> scriptCols = dialog.getColumnList().stream()
                        .filter(x -> CustomDialog.ColumnType.SCRIPT.name().equals(x.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(scriptCols)) {
                    dataList = this.applyScriptColumnToQueryResult(scriptCols, dataList);
                }
                Integer count = queryExecutor.selectOne(sqlCount, Integer.class);


                return ResultPage.<Map<String, Object>>builder()
                        .data(applyDisableScriptToQueryResult(dialog, dataList))
                        .totalCount(count)
                        .pageSize(request.getLimit())
                        .pageStart(request.getStart())
                        .build();
            }
            return ResultPage.<Map<String, Object>>builder()
                    .data(null)
                    .totalCount(0)
                    .pageSize(request.getLimit())
                    .pageStart(request.getStart())
                    .build();
        } catch (Exception e) {
            log.info("[CustomDialog] e: " + ExceptionUtils.getStackTrace(e));
            return new ResultPage<>();
        }
    }

    /**
     *  Merge the search filter to the select query
     *  (For FilterType.NONE and FilterType.FIELD_FILTER)
     * @param dialog
     * @param whereSql
     * @param searchColumns
     * @param searchData - For dialogs with {@link LayoutType#SINGLE} layoutSettings.type
     *                   (No need to put value for MULTI layout type dialogs
     * @param searchMap - For dialogs with {@link LayoutType#MULTI} layoutSettings.type
     *                   (No need to put value for SINGLE layout type dialogs
     */
    public void mergeSearchQuery(CustomDialog dialog, StringBuilder whereSql, List<CustomDialog.Column> searchColumns,
                                 String searchData, Map<String, Object> searchMap) {
        log.info("mergeSearchQuery | searchColumns: " + new Gson().toJson(searchColumns));
        boolean hasFilter = !whereSql.toString().isBlank();
        //Single field layout
        if (CustomDialog.LayoutType.SINGLE.name().equals(dialog.getLayoutSettings().getType())
                && StringUtils.isNotBlank(searchData) && CollectionUtils.isNotEmpty(searchColumns)) {
            log.info("mergeSearchQuery | [SINGLE]");
            List<String> conditions = new ArrayList<>();
            for (CustomDialog.Column col : searchColumns) {
                String columnName =
                        CustomDialog.ColumnType.DATABASE.name().equals(col.getType()) ? col.getColumnName()
                                : CustomDialog.ColumnType.FORMULA.name().equals(col.getType())
                                ? col.getCustomValue() : "";
                log.info("mergeSearchQuery | [SINGLE] columnName: " + columnName);
                if (StringUtils.isNotBlank(columnName)) {
                    conditions.add(String.format("LOWER(%s) LIKE '%%%s%%'",
                            columnName, searchData.toLowerCase()));
                }
            }
            if (hasFilter) whereSql.append(" AND (");
            whereSql.append(" ").append(StringUtils.join(conditions, " OR "));
            if (hasFilter) whereSql.append(")");
            log.info("mergeSearchQuery | [SINGLE] whereSql: " + whereSql.toString());
        }
        //Multi field layout
        else if (CustomDialog.LayoutType.MULTI.name().equals(dialog.getLayoutSettings().getType())
                && MapUtils.isNotEmpty(searchMap) && CollectionUtils.isNotEmpty(searchColumns)) {
            List<String> conditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
                CustomDialog.Column column = searchColumns.stream()
                        .filter(x -> x.getColumnName().equals(entry.getKey()))
                        .findFirst().orElse(null);
                if (entry.getValue() != null) {
                    String columnName = CustomDialog.ColumnType.DATABASE.name().equals(column.getType())
                            ? entry.getKey() : CustomDialog.ColumnType.FORMULA.name().equals(column.getType())
                            ? column.getCustomValue() : "";
                    //For MULTISELECT, the value is an array
                    if (SearchFieldType.MULTISELECT.name().equals(column.getSearchSettings().getType())) {
                        List<String> values = TypeUtils.objectToList(entry.getValue(), String.class);
                        if (CollectionUtils.isNotEmpty(values)) {
                            conditions.add(String.format("%s IN (%s)", columnName,
                                    values.stream()
                                            .map(x -> String.format("'%s'", x))
                                            .collect(Collectors.joining(","))));
                        }
                    }
                    //For other types, the value is a string
                    else {
                        String value = entry.getValue() != null ? (String) entry.getValue() : null;
                        if (StringUtils.isNotBlank(value)) {
                            if (StringUtils.isNotBlank(columnName)) {
                                //TEXT search fields are case-insensitive
                                if (SearchFieldType.TEXT.name().equals(column.getSearchSettings().getType())) {
                                    conditions.add(String.format("LOWER(%s) LIKE '%%%s%%'",
                                            columnName, value.toLowerCase()));
                                } else {
                                    //DROPDOWN and TREE search fields are exact
                                    conditions.add(String.format("%s = '%s'", columnName, value));
                                }
                            }
                        }
                    }
                }
            }
            if (!conditions.isEmpty()) {
                if (hasFilter) whereSql.append(" AND (");
                whereSql.append(" ").append(StringUtils.join(conditions, " AND "));
                if (hasFilter) whereSql.append(")");
            }
        }
    }

    /** Merge the search filter to the select query
     * (For FilterType.CUSTOM_QUERY) */
    public Pair<String, String> mergeSearchQuery(CustomDialog dialog, List<CustomDialog.Column> searchColumns,
                                                 String searchData, Map<String, Object> searchMap) {
        String sql = dialog.getDialogFilter().getSqlQuery();
        String sqlCount = dialog.getDialogFilter().getSqlCountQuery();
        Pair<String, String> sqlPair = dialog.applyParamToSql(sql, sqlCount);

        if (StringUtils.isNotBlank(sql) && StringUtils.isNotBlank(sqlCount)) {
            boolean hasFilter = sql.toLowerCase().contains("where");
            if (CustomDialog.LayoutType.SINGLE.name().equals(dialog.getLayoutSettings().getType())
                    && StringUtils.isNotBlank(searchData) && CollectionUtils.isNotEmpty(searchColumns)) {
                List<String> conditions = new ArrayList<>();
                for (CustomDialog.Column col : searchColumns) {
                    String columnName = CustomDialog.ColumnType.DATABASE.name().equals(col.getType())
                            ? col.getColumnName() : "";
                    if (StringUtils.isNotBlank(columnName)) {
                        conditions.add(String.format("LOWER(%s) LIKE '%%%s%%'",
                                columnName, searchData.toLowerCase()));
                    }
                }
                String conditionsStr = StringUtils.join(conditions, " OR ");

                String additionalWhere = (StringUtils.isNotBlank(conditionsStr)
                        ? hasFilter
                        ? " AND (" + conditionsStr + ")"
                        : " WHERE " + conditionsStr
                        : "");

                sql = sqlPair.getLeft().replace("<searchWhere>", additionalWhere);
                sqlCount = sqlPair.getRight().replace("<searchWhere>", additionalWhere);
            }
            //Multi field layout
            else if (CustomDialog.LayoutType.MULTI.name().equals(dialog.getLayoutSettings().getType())
                    && MapUtils.isNotEmpty(searchMap) && CollectionUtils.isNotEmpty(searchColumns)) {
                List<String> conditions = new ArrayList<>();
                for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
                    CustomDialog.Column column = searchColumns.stream()
                            .filter(x -> x.getColumnName().equals(entry.getKey()))
                            .findFirst().orElse(null);

                    if (entry.getValue() != null) {
                        String columnName = CustomDialog.ColumnType.DATABASE.name().equals(column.getType())
                                ? entry.getKey() : CustomDialog.ColumnType.FORMULA.name().equals(column.getType())
                                ? column.getCustomValue() : "";

                        //For MULTISELECT, the value is an array
                        if (SearchFieldType.MULTISELECT.name().equals(column.getSearchSettings().getType())) {
                            List<String> values = TypeUtils.objectToList(entry.getValue(), String.class);
                            if (CollectionUtils.isNotEmpty(values)) {
                                conditions.add(String.format("%s IN (%s)", columnName,
                                        values.stream()
                                                .map(x -> String.format("'%s'", x))
                                                .collect(Collectors.joining(","))));
                            }
                        }
                        //For other types, the value is a string
                        else {
                            String value = entry.getValue() != null ? (String) entry.getValue() : null;
                            if (StringUtils.isNotBlank(value)) {
                                if (StringUtils.isNotBlank(columnName)) {
                                    //TEXT search fields are case-insensitive
                                    if (SearchFieldType.TEXT.name().equals(column.getSearchSettings().getType())) {
                                        conditions.add(String.format("LOWER(%s) LIKE '%%%s%%'",
                                                columnName, value.toLowerCase()));
                                    } else {
                                        //DROPDOWN and TREE search fields are exact
                                        conditions.add(String.format("%s = '%s'", columnName, value));
                                    }
                                }
                            }
                        }
                    }
                }
                String conditionsStr = StringUtils.join(conditions, " OR ");
                String additionalWhere = (StringUtils.isNotBlank(conditionsStr)
                        ? hasFilter
                        ? " AND (" + conditionsStr + ")"
                        : " WHERE " + conditionsStr
                        : "");
                sql = sqlPair.getLeft().replace("<searchWhere>", additionalWhere);
                sqlCount = sqlPair.getRight().replace("<searchWhere>", additionalWhere);
            } else {
                sql = sqlPair.getLeft().replace("<searchWhere>", "");
                sqlCount = sqlPair.getRight().replace("<searchWhere>", "");
            }
        }

        String finalSql = sql;
        String finalSqlCount = sqlCount;

        return new Pair<>() {
            @Override
            public String setValue(String value) {
                return null;
            }

            @Override
            public String getLeft() {
                return finalSql;
            }

            @Override
            public String getRight() {
                return finalSqlCount;
            }
        };
    }
    /** Extract data from script and apply to data list */
    public List<Map<String, Object>> applyScriptColumnToQueryResult(List<CustomDialog.Column> scriptCols,
                                                                    List<Map<String, Object>> dataList) {
        return dataList.stream()
                .map(x -> {
                    for (CustomDialog.Column col : scriptCols) {
                        x.put(col.getReturnName(),
                                groovyScriptService.runForCustomDialogScriptColumn(
                                        col.getCustomValue(), x));
                    }
                    return x;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> applyDisableScriptToQueryResult(CustomDialog dialog,
                                                                     List<Map<String, Object>> dataList) {
        if (dialog.getDialogFilter().getDisableRule() == null
                || StringUtils.isBlank(dialog.getDialogFilter().getDisableRule().getScript())) return dataList;
        return dataList.stream()
                .map(x -> {
                    try {
                        x.put("cdcDisable", groovyScriptService.runForCustomDialogDisabledScript(
                                dialog.getDialogFilter().getDisableRule().getScript(), x));
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info("applyDisableScriptToQueryResult.e: " + ExceptionUtils.getStackTrace(e));
                    }
                    return x;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get the dropdown options of the search fields subscribed to columnName's onChange
     */
    @Override
    public List<Map<String, Object>> handleSearchFieldOnChange(Integer id, String columnName,
                                                               Map<String, Object> filterParams,
                                                               Map<String, Object> searchParams) {
        CustomDialog dialog = customDialogRepo.getDialogForSearchFieldOnChange(id);
        dialog.setFilterParams(filterParams);
        dialog.setSearchParams(searchParams);
        dialog.parseColumns();

        //Get the columns subscribed to `columnName`'s onChange
        List<CustomDialog.Column> subscribers = dialog.getColumnList().stream()
                .filter(x -> {
                    if (x.getSearchSettings() != null && x.getSearchSettings()
                            .getOnChangeColumnSubscribeList() != null) {
                        return x.getSearchSettings().getOnChangeColumnSubscribeList().contains(columnName);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        //Execute the sql query/script to get new options
        subscribers.forEach(x -> {
            if (x.getSearchSettings() != null
                    && !SearchFieldType.TEXT.name().equals(x.getSearchSettings().getType())) {
                SearchFieldType fieldType = CustomDialog.SearchFieldType
                        .get(x.getSearchSettings().getType());
                SearchFieldDataSource dataSrc = SearchFieldDataSource
                        .get(x.getSearchSettings().getDataSrcType());
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("columnName", x.getColumnName());
                if (SearchFieldType.DROPDOWN.equals(fieldType) || SearchFieldType.MULTISELECT.equals(fieldType)) {
                    List<DropdownItem> options = new ArrayList<>();
                    if (SearchFieldDataSource.LIST.equals(dataSrc)) {
                        List<DropdownItem> items = TypeUtils.stringToList(
                                x.getSearchSettings().getDataSrcValue(), DropdownItem.class);
                        if (CollectionUtils.isNotEmpty(items)) options.addAll(items);
                    }
                    else if (SearchFieldDataSource.SQL.equals(dataSrc)) {
                        String sql = dialog.applyParamToSearchSettingsSql(x.getSearchSettings().getDataSrcValue());
                        List<Map<String, Object>> items = queryExecutor.select(sql);
                        if (CollectionUtils.isNotEmpty(items))
                            options.addAll(TypeUtils.parameterizeList(items, DropdownItem.class));
                    }
                    else if (SearchFieldDataSource.SCRIPT.equals(dataSrc)) {
                        List<DropdownItem> items = groovyScriptService.runForCustomDialogSearchFieldScriptColumn(
                                x.getSearchSettings().getDataSrcValue(), dialog.getFilterParams(),
                                dialog.getSearchParams());
                        if (CollectionUtils.isNotEmpty(items)) options.addAll(items);
                    }
                    optionMap.put("options", options);
                    result.add(optionMap);
                    x.getSearchSettings().setSelection(options);
                } else if (SearchFieldType.TREE.equals(fieldType)) {
                    if (SearchFieldDataSource.LIST.equals(dataSrc)) {
                        //TODO
                    }
                    else if (SearchFieldDataSource.SQL.equals(dataSrc)) {
                        //TODO
                    }
                    else if (SearchFieldDataSource.SCRIPT.equals(dataSrc)) {
                        //TODO
                    }
                }
            }
        });
        return result;
    }

    /** Query data for `TREE` type dialogs */
    @Override
    public ResultMsg<TreeData> getDialogDataTreeV2(Map<String, Object> params) {
        try {
            String dialogKey = MapUtils.getString(params, "dialogKey");
            //Query data list
            CustomDialog dialog = this.getByKey(dialogKey, false);
            String displayColumn = dialog.getTreeSettings().getReturnDataColumns().stream()
                    .filter(CustomDialog.Column::getShow)
                    .map(CustomDialog.Column::getReturnName)
                    .findFirst()
                    .orElse(null);

            List<String> selectColumns = dialog.getColumnsForSql();
            List<Map<String, Object>> dataList = new ArrayList<>();
            FilterType filterType = dialog.getFilterType();

            if (FilterType.NONE.equals(filterType)) {
                String sql = "SELECT " + String.join(", ", selectColumns)
                        + " FROM `" + dialog.getQueryTable() + "`";
                dataList = queryExecutor.select(sql);
            } else if (FilterType.CUSTOM_QUERY.equals(filterType)) {
                String sql = dialog.getDialogFilter().getSqlQuery();
                //Apply custom params if available (Custom params are enclosed with curly brackets {})
                dialog.setFilterParams(com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("param")));
                sql = dialog.applyParamToSql(sql);
                dataList = queryExecutor.select(sql);
            } else if (FilterType.FIELD_FILTER.equals(filterType)) {
                List<FilterClause> filterClauses = dialog.getDialogFilter().getFilterClauses();
                AtomicInteger index = new AtomicInteger(0);
                StringBuilder whereSql = new StringBuilder();
                filterClauses.forEach(x -> {
                    int i = index.getAndIncrement();
                    String sql = x.format(i != 0);
                    whereSql.append(sql).append(i + 1 < filterClauses.size() ? " " : "");
                });
                String sql = "SELECT " + String.join(", ", selectColumns)
                        + " FROM `" + dialog.getQueryTable() + "` WHERE " + whereSql;
                dialog.setFilterParams(com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("param")));
                sql = dialog.applyParamToSql(sql);
                dataList = queryExecutor.select(sql);
            }

            //Format data into tree
            List<TreeData.TreeNode> dataListNodes = new ArrayList<>();

            for (Map<String, Object> data : dataList) {
                dataListNodes.add(convertMapToTreeNode(dialog, data, displayColumn, dialog.getTreeSettings().getIdColumn(),
                        dialog.getTreeSettings().getParentIdColumn()));
            }

            //Get the top elements first
            List<TreeData.TreeNode> finalDataListNodes = dataListNodes.stream()
                    .filter(x -> StringUtils.isBlank(x.getParentId())
                            || x.getParentId().equals("0"))
                    .collect(Collectors.toList());

            Map<String, String> keyMap = new HashMap<>(); //Contains the keys to be used to update data list later

            //Set keys of top level elements
            for (int i = 0; i < finalDataListNodes.size(); i++) {
                finalDataListNodes.get(i).setKey(String.valueOf(i));
                keyMap.put(finalDataListNodes.get(i).getId(), finalDataListNodes.get(i).getKey());
            }

            //Set the children and fill the keys of the tree nodes
            for (TreeData.TreeNode node : finalDataListNodes) {
                fillNodeChildren(node, dataListNodes, keyMap);
            }

            //Fill the keys of the data list
            for (int i = 0; i < dataList.size(); i++) {
                Integer id = MapUtils.getInteger(dataList.get(i), "id");
                dataList.get(i).put("key", id != null ? keyMap.get(id.toString()) : "NONE");
            }

            return new ResultMsg<TreeData>().success(new TreeData(finalDataListNodes, dataList));
        } catch (Exception e) {
            return new ResultMsg<>(false, ExceptionUtils.getStackTrace(e));
        }
    }

    public TreeData.TreeNode convertMapToTreeNode(CustomDialog dialog, Map<String, Object> map, String displayColumn, String idColumn,
                                                  String parentIdColumn) {
        log.info("convertMapToTreeNode | map: " + new Gson().toJson(map));
        TreeData.TreeNode node = new TreeData.TreeNode();
        node.setId(MapUtils.getString(map, idColumn, ""));
        node.setParentId(MapUtils.getString(map, parentIdColumn, ""));
        String value = MapUtils.getString(map, displayColumn, "");
        node.setLabel(value);
        node.setData(value);
        try {
            node.setSelectable(dialog.getDialogFilter().getDisableRule() == null
                    || !groovyScriptService.runForCustomDialogDisabledScript(
                    dialog.getDialogFilter().getDisableRule().getScript(), map));
        } catch (Exception e) {
            log.info("convertMapToTreeNode | error: " + e.getMessage());
            node.setSelectable(true);
        }
        return node;
    }

    public void fillNodeChildren(TreeData.TreeNode node, List<TreeData.TreeNode> dataListNodes,
                                 Map<String, String> keyMap) {
        node.setChildren(dataListNodes.stream().filter(x -> x.getParentId().equals(node.getId()))
                .collect(Collectors.toList()));

        for (int i = 0; i < node.getChildren().size(); i++) {
            node.getChildren().get(i).setKey(node.getKey() + "-" + i);
            keyMap.put(node.getChildren().get(i).getId(), node.getKey() + "-" + i);
        }
        if (CollectionUtils.isNotEmpty(node.getChildren())) {
            for (TreeData.TreeNode tn : node.getChildren()) {
                fillNodeChildren(tn, dataListNodes, keyMap);
            }
        }
    }

    @Override
    public List<CustomDialog> getCustomDialogList(Map<String, Object> param) {
        return customDialogRepo.getCustomDialogList(param);
    }

    @Override
    public ResultPage<CustomDialog> getCustomDialogList(PagingRequest<CustomDialog> request) {
        List<CustomDialog> dialogs = customDialogRepo.getCustomDialogList(request);
        int totalCount = request.getStart() == 1 && dialogs.size() < request.getLimit()
                ? dialogs.size() : customDialogRepo.getCustomDialogListCount(request);
        return ResultPage.<CustomDialog>builder()
                .data(dialogs)
                .pageStart(request.getStart())
                .pageSize(request.getLimit())
                .totalCount(totalCount)
                .build();
    }

    @Override
    public int getCustomDialogListCount(Map<String, Object> param) {
        return customDialogRepo.getCustomDialogListCount(param);
    }

    @Override
    public List<Map<String, String>> getTableOptions() {
        List<String> tables = customDialogRepo.getTableNames(databaseName);
        List<Map<String, String>> options = new ArrayList<>();
        for (String t : tables) {
            Map<String, String> map = new HashMap<>();
            map.put("name", t);
            map.put("code", t);
            options.add(map);
        }
        return options;
    }

    @Override
    public List<Map<String, String>> getTableColumns(String table) {
        return customDialogRepo.getTableColumns(String.format("`%s`", table));
    }

    @Override
    public void deleteDialog(Integer id) {
        customDialogRepo.delete(id);
    }
}
