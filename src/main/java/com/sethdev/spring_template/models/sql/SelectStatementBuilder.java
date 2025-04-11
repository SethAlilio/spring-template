package com.sethdev.spring_template.models.sql;

import com.sethdev.spring_template.models.constants.Sort;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SelectStatementBuilder implements CrudBuilder {

    private String table;
    private WhereStatementBuilder<SelectStatementBuilder> where;
    private Map<String, Object> selectColumns;
    private String orderBy;
    private Sort sort;
    private Integer limit;
    private Integer offset;

    public SelectStatementBuilder table(String tableName) {
        this.table = tableName;
        return this;
    }

    public SelectStatementBuilder count(String value) {
        String count = String.format("COUNT(%s)", value);
        this.selectColumn(count, null);
        return this;
    }

    public SelectStatementBuilder where(WhereStatementBuilder<SelectStatementBuilder> where) {
        this.where = where;
        return this;
    }

    public WhereStatementBuilder<SelectStatementBuilder> where() {
        if (this.where == null) {
            this.where = new WhereStatementBuilder<>(this);
        }
        return this.where;
    }

    public SelectStatementBuilder selectColumn(String column) {
        if (this.selectColumns == null) {
            this.selectColumns = new HashMap<>();
        }
        this.selectColumns.put(column, null);
        return this;
    }

    public SelectStatementBuilder selectColumn(String column, String alias) {
        if (this.selectColumns == null) {
            this.selectColumns = new HashMap<>();
        }
        this.selectColumns.put(column, alias);
        return this;
    }

    public SelectStatementBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public SelectStatementBuilder orderBy(String orderBy, Sort sort) {
        this.orderBy = orderBy;
        this.sort = sort;
        return this;
    }

    public SelectStatementBuilder limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public SelectStatementBuilder limit(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    /**
     * @return Complete query including the where clause
     */
    @Override
    public String build() {
        if (this.where != null) {
            return this.where.build();
        } else {
            String stmt = this.generateStatement();
            if (StringUtils.isNotBlank(stmt)) {
                String orderBySql = this.generateOrderBy();
                String limitSql = this.generateLimit();
                StringBuilder sb = new StringBuilder();
                sb.append(stmt);
                if (StringUtils.isNotBlank(orderBySql)) {
                    sb.append(" ").append(orderBySql);
                }
                if (StringUtils.isNotBlank(limitSql)) {
                    sb.append(" ").append(limitSql);
                }
                return sb.toString();
            } else {
                return "";
            }
        }
    }

    /**
     * @return Statement excluding the where clause
     */
    @Override
    public String generateStatement() {
        if (MapUtils.isNotEmpty(selectColumns) && StringUtils.isNotEmpty(this.table)) {
            List<String> selectCols = new ArrayList<>();
            for (Map.Entry<String, Object> col : selectColumns.entrySet()) {
                selectCols.add(col.getKey() + (col.getValue() != null ? " AS " + col.getValue() : ""));
            }
            String selColStr = String.join(", ", selectCols);
            return String.format("SELECT %s FROM %s", selColStr, this.table);
        } else {
            return "";
        }
    }

    public String generateStatementTemplateForWhereClause() {
        String stmt = this.generateStatement();
        if (StringUtils.isNotBlank(stmt)) {
            String orderBySql = this.generateOrderBy();
            String limitSql = this.generateLimit();
            StringBuilder sb = new StringBuilder();
            sb.append(stmt).append(" %s");
            if (StringUtils.isNotBlank(orderBySql)) {
                sb.append(" ").append(orderBySql);
            }
            if (StringUtils.isNotBlank(limitSql)) {
                sb.append(" ").append(limitSql);
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public String generateOrderBy() {
        if (this.orderBy != null) {
            return String.format(" ORDER BY %s%s", this.orderBy,
                    this.sort != null ? " " + this.sort.name() : "");
        } else {
            return "";
        }
    }

    public String generateLimit() {
        if (this.limit != null) {
            if (this.offset != null) {
                return String.format(" LIMIT %s,%s", this.offset, this.limit);
            } else {
                return String.format(" LIMIT %s", this.limit);
            }
        } else {
            return "";
        }
    }
}
