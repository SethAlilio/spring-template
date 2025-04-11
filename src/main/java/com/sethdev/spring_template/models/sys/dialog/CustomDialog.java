package com.sethdev.spring_template.models.sys.dialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomDialog {

    private Integer id;
    private String name;
    private String key;

    /** {@link DialogType} */
    private String type;
    private String queryTable;//Table name where the data on the dialog is retrieved
    private String columns;//Serialized columnList
    private String layout;//Serialized layoutSettings

    private String pkColumn;//Primary key of the table
    private List<Column> columnList;//Deserialized columns
    private TreeSettings treeSettings;//Deserialized columns

    private String filter;
    private DialogFilter dialogFilter;
    private LayoutSettings layoutSettings;

    /** Optional custom params */
    private Map<String, Object> filterParams;

    /** Data from the search fields */
    private Map<String, Object> searchParams;

    /**
     * Columns are saved as string in database, this will parse it to object based on type
     */
    public void parseColumns() {
        if (this.type.equals("LIST")) {
            Type listType = new TypeToken<List<Column>>(){}.getType();
            this.columnList = new Gson().fromJson(this.columns, listType);

            if (this.treeSettings == null) {
                this.treeSettings = new TreeSettings();
            }
            this.treeSettings.setReturnDataColumns(new ArrayList<>());
        }
        if (this.type.equals("TREE")) {
            this.treeSettings = new Gson().fromJson(this.columns, TreeSettings.class);
            this.columnList = this.treeSettings.getReturnDataColumns();
        }
        //this.columnList = (List<Column>) new Gson().fromJson(this.columns, List.class);
    }

    /**
     * Filter settings are saved as string in database, this will parse it to DialogFilter object
     */
    public void parseFilter() {
        if (StringUtils.isNotBlank(this.filter)) {
            this.dialogFilter = new Gson().fromJson(this.filter, DialogFilter.class);
        }
    }
    public void parseLayout() {
        if (StringUtils.isNotBlank(this.layout)) {
            this.layoutSettings = new Gson().fromJson(this.layout, LayoutSettings.class);
        }
    }

    public void parseSettings() {
        this.parseColumns();
        this.parseFilter();
        this.parseLayout();
    }
    public void parseSettingsThenClear() {
        this.parseColumns();
        this.parseFilter();
        this.parseLayout();
        this.columns = null;
        this.filter = null;
        this.layout = null;
    }

    /** Fill the searchSettings for each column, the default will be of type TEXT */
    public void setupSearchSettings() {
        if (CollectionUtils.isNotEmpty(this.columnList)) {
            this.columnList.forEach(x -> {
                if (x.getSearchSettings() == null) {
                    x.setSearchSettings(new SearchSettings(SearchFieldType.TEXT.name()));
                } else if (!x.getSearchable()) {
                    x.setSearchSettings(null);
                }
            });
        }
    }

    /** Fill the {@link Column#onChangeEnabled} */
    public void setupColumnsOnChangeEnabled() {
        if (CollectionUtils.isNotEmpty(this.columnList)) {
            this.columnList.forEach(x -> {
                x.setOnChangeEnabled(columnList.stream()
                        .anyMatch(col -> {
                            if (col.getSearchSettings() != null
                                    && CollectionUtils.isNotEmpty(col.getSearchSettings().getOnChangeColumnSubscribeList())) {
                                return col.getSearchSettings().getOnChangeColumnSubscribeList().contains(x.columnName);
                            }
                            return false;
                        }));
            });
        }
    }

    /** Fill the {@link SearchSettings#onChangeSubscribers} */
    public void setupOnChangeSubscribers() {
        if (CollectionUtils.isNotEmpty(this.columnList)) {
            this.columnList.forEach(x -> {
                if (x.getSearchSettings() != null) {
                    x.getSearchSettings().setOnChangeSubscribers(columnList.stream()
                            .filter(y -> {
                                if (y.getSearchSettings() != null &&
                                        CollectionUtils.isNotEmpty(y.getSearchSettings().getOnChangeColumnSubscribeList())) {
                                    return y.getSearchSettings()
                                            .getOnChangeColumnSubscribeList()
                                            .contains(x.getColumnName());
                                }
                                return false;
                            })
                            .map(Column::getColumnName)
                            .collect(Collectors.toList())
                    );
                }
            });
        }
    }

    public List<String> getColumnsForSql() {
        List<String> sqlCols = new ArrayList<>();
        if ("LIST".equals(this.type) && CollectionUtils.isNotEmpty(this.columnList)) {
            for (Column col : this.columnList) {
                if (StringUtils.isBlank(col.getType()) || ColumnType.DATABASE.name().equals(col.getType())) {
                    sqlCols.add(col.getColumnName() + " AS `" + col.getReturnName() + "`");
                } else if (ColumnType.FORMULA.name().equals(col.getType())) {
                    sqlCols.add("(" + col.getCustomValue() + ") AS `" + col.getReturnName() + "`");
                }
            }
        }
        if ("TREE".equals(this.type) && this.treeSettings != null
                && CollectionUtils.isNotEmpty(this.treeSettings.getReturnDataColumns())) {
            for (Column col : this.treeSettings.getReturnDataColumns()) {
                if (StringUtils.isBlank(col.getType()) || ColumnType.DATABASE.name().equals(col.getType())) {
                    sqlCols.add(col.getColumnName() + " AS `" + col.getReturnName() + "`");
                } else if (ColumnType.FORMULA.name().equals(col.getType())) {
                    sqlCols.add("(" + col.getCustomValue() + ") AS `" + col.getReturnName() + "`");
                }
            }
        }
        return sqlCols;
    }

    public FilterType getFilterType() {
        this.parseFilter();
        if (dialogFilter != null) {
            return FilterType.eq(dialogFilter.getType());
        } else {
            return FilterType.NONE;
        }
    }

    public List<String> listDuplicateColumnNames() {
        if (!org.springframework.util.CollectionUtils.isEmpty(getColumnList())) {
            return getColumnList().stream()
                    .collect(Collectors.groupingBy(Column::getColumnName))
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<String> listDuplicateReturnNames() {
        if (!org.springframework.util.CollectionUtils.isEmpty(getColumnList())) {
            return getColumnList().stream()
                    .collect(Collectors.groupingBy(Column::getReturnName))
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<Column> listShownColumns() {
        return getType().equals("LIST")
                ? getColumnList().stream()
                .filter(CustomDialog.Column::getShow)
                .collect(Collectors.toList())
                : getTreeSettings().getReturnDataColumns().stream()
                .filter(CustomDialog.Column::getShow)
                .collect(Collectors.toList());
    }

    public String generateWhereCondition(String searchData) {
        StringBuilder whereSql = new StringBuilder();

        List<String> searchColumns = this.getColumnList().stream()
                .filter(CustomDialog.Column::getSearchable)
                .map(CustomDialog.Column::getColumnName)
                .collect(Collectors.toList());

        if (StringUtils.isNotBlank(searchData) && CollectionUtils.isNotEmpty(searchColumns)) {
            for (String col : searchColumns) {
                whereSql.append("LOWER(").append(col).append(") LIKE '%")
                        .append(searchData.toLowerCase()).append("%' OR ");
            }
        }

        String where = whereSql.toString();
        if (StringUtils.isNotBlank(where)) {
            return where.substring(0, where.length()-4);
        } else {
            return "";
        }
    }

    public String applyParamToSql(String sql) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            variables.add(matcher.group());
        }
        if (CollectionUtils.isNotEmpty(variables)) {
            for (String s : variables) {
                String var = s.replace("{", "").replace("}", "");
                String value = MapUtils.getString(this.filterParams, var, null);
                sql = sql.replace(s, value != null ? value : "NULL");
            }
        }
        return sql;
    }

    //TODO: TEST
    public String applyParamToSearchSettingsSql(String sql) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            variables.add(matcher.group());
        }
        if (CollectionUtils.isNotEmpty(variables)) {
            for (String s : variables) {
                String var = s.replace("{", "").replace("}", "");
                String[] varSplit = var.split("\\.");
                if ("param".equals(varSplit[0])) {
                    String value = MapUtils.getString(this.filterParams, varSplit[1], null);
                    sql = sql.replace(s, value != null ? value : "NULL");
                } if ("search".equals(varSplit[0])) {
                    String value = MapUtils.getString(this.searchParams, varSplit[1], null);
                    sql = sql.replace(s, value != null ? value : "NULL");
                }
            }
        }
        return sql;
    }

    public Pair<String, String> applyParamToSql(String sql, String sqlCount) {
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            variables.add(matcher.group());
        }
        if (CollectionUtils.isNotEmpty(variables)) {
            for (String s : variables) {
                String var = s.replace("{", "").replace("}", "");
                String value = MapUtils.getString(this.filterParams, var, null);
                sql = sql.replace(s, value != null ? value : "NULL");
                sqlCount = sqlCount.replace(s, value != null ? value : "NULL");
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

    public enum DialogType {
        LIST, TREE
    }

    @Getter
    @Setter
    public static class Column {
        private String columnName;
        private String displayName;
        private String returnName;
        private Boolean show;
        private Boolean searchable;

        /** {@link ColumnType} */
        private String type;

        /** Has value only if `type` is FORMULA or SCRIPT */
        private String customValue;

        private SearchSettings searchSettings;

        /** If other columns subscribe to this field's onchange {@link SearchSettings#onChangeColumnSubscribeList} */
        private boolean onChangeEnabled;

        public boolean hasEmptyFields() {
            return (ColumnType.DATABASE.name().equals(this.type) && StringUtils.isBlank(this.columnName))
                    || StringUtils.isBlank(this.displayName)
                    || StringUtils.isBlank(this.returnName);
        }
    }

    public enum ColumnType {
        DATABASE, FORMULA, SCRIPT
    }

    /**
     * Describes the appearance of a searchable field on the search panel
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SearchSettings {
        /** {@link SearchFieldType} */
        private String type;

        /** {@link SearchFieldDataSource} */
        private String dataSrcType;

        /** SQL query or groovy script (Based on {@link #dataSrcType}) */
        private String dataSrcValue;

        /** The  result of {@link #dataSrcValue} if the value of {@link #type} is `DROPDOWN` */
        private List<DropdownItem> selection;
        //private List<Map<String, String>> selection;

        /** The result of {@link #dataSrcValue} if the value of {@link #type} is `TREE` */
        private TreeData treeSelection;

        /** Columns where if the value of those column changes, the current search field will re-execute the {@link #dataSrcValue} to refresh the selection values */
        private List<String> onChangeColumnSubscribeList;

        /** Columns that subscribe to the onChange of this field */
        private List<String> onChangeSubscribers;

        public SearchSettings(String type) {
            this.type = type;
        }

    }

    public enum SearchFieldType {
        TEXT, DROPDOWN, MULTISELECT, TREE;

        public static SearchFieldType get(String type) {
            for (SearchFieldType s : values()) {
                if (s.name().equals(type)) return s;
            }
            return null;
        }
    }

    public enum SearchFieldDataSource {
        LIST, SQL, SCRIPT;

        public static SearchFieldDataSource get(String src) {
            for (SearchFieldDataSource s : values()) {
                if (s.name().equals(src)) return s;
            }
            return null;
        }
    }

    /** Summary of the search fields and it's subscribers (Subscriber fields are columns that will refresh its selection if the current field changes in value */
    @Getter
    @Setter
    public static class SearchFieldOnChangeSubscription {
        private String columnName;
        private List<String> subscribers;
    }

    @Getter
    @Setter
    public static class TreeSettings {
        private String idColumn;
        private String parentIdColumn;
        private List<Column> returnDataColumns;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DialogFilter {
        /**
         * {@link FilterType}
         */
        private String type;

        /**
         * Only required if type is 'CUSTOM_QUERY'
         */
        private String sqlQuery;

        /**
         * Only required if type is 'CUSTOM_QUERY'
         */
        private String sqlCountQuery;

        private List<FilterClause> filterClauses;

        private DisableRule disableRule;

        public DialogFilter(String type) {
            this.type = type;
        }

    }

    @Getter
    @Setter
    public static class FilterClause {
        private int id;

        /** AND, OR */
        private String operator1;
        private String column;

        /**
         * =, !=, IN, NOT IN, LIKE, >, <, >=, <=, IS NULL, IS NOT NULL
         */
        private String operator2;
        private Object value;

        public String format(boolean includeOperator1) {
            if (operator2.equals("IS NULL") || operator2.equals("IS NOT NULL")) {
                return String.format("%s%s %s", includeOperator1 ? operator1 + " " : "", column, operator2);
            } else {
                return String.format("%s%s %s %s", includeOperator1 ? operator1 + " " : "", column, operator2,
                        value == null ? "NULL"
                                : operator2.equals("IN") || operator2.equals("NOT IN")
                                ? "(" + value + ")"
                                : "'" + value + "'");
            }
        }
    }

    public enum FilterType {
        NONE,
        CUSTOM_QUERY,
        FIELD_FILTER;

        public static FilterType eq(String type) {
            if (StringUtils.isNotBlank(type)) {
                for (FilterType filter : values()) {
                    if (filter.name().equals(type)) {
                        return filter;
                    }
                }
            }
            return NONE;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutSettings {
        /** {@link LayoutType} */
        private String type;
        private int colCount;
        private boolean toggleable;
        private boolean collapsed;
    }

    public enum LayoutType {
        SINGLE, MULTI
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DisableRule {
        private String script;
    }
}
