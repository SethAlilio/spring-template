package com.sethdev.spring_template.models.sql;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class WhereStatementBuilder<T extends CrudBuilder> {

    private T crudBuilder;
    private StringBuilder whereSb;

    public WhereStatementBuilder(T crudBuilder) {
        this.crudBuilder = crudBuilder;
        this.whereSb = new StringBuilder();
    }

    public WhereStatementBuilder<T> and() {
        this.whereSb.append(" AND ");
        return this;
    }

    public WhereStatementBuilder<T> or() {
        this.whereSb.append(" OR ");
        return this;
    }

    public WhereStatementBuilder<T> equal(String column, Object value) {
        this.whereSb.append(column).append(" = '").append(value).append("'");
        return this;
    }

    public WhereStatementBuilder<T> in(String column, Object... values) {
        if (StringUtils.isNotBlank(column) && values.length > 0) {
            String valuesString = Arrays.stream(values)
                    .map(Object::toString)
                    .map(value -> "'" + value + "'")
                    .collect(Collectors.joining(","));
            this.whereSb.append(column).append(" IN (").append(valuesString).append(")");
        }
        return this;
    }

    public WhereStatementBuilder<T> in(String column, List<String> values) {
        if (StringUtils.isNotBlank(column) && CollectionUtils.isNotEmpty(values)) {
            String valuesString = values.stream()
                    .map(Object::toString)
                    .map(value -> "'" + value + "'")
                    .collect(Collectors.joining(","));
            this.whereSb.append(column).append(" IN (").append(valuesString).append(")");
        }
        return this;
    }

    public WhereStatementBuilder<T> like(String column, Object value) {
        this.whereSb.append(column).append(" LIKE '%").append(value).append("%'");
        return this;
    }

    public WhereStatementBuilder<T> likeStart(String column, Object value) {
        this.whereSb.append(column).append(" LIKE '").append(value).append("%'");
        return this;
    }

    public WhereStatementBuilder<T> likeEnd(String column, Object value) {
        this.whereSb.append(column).append(" LIKE '%").append(value).append("'");
        return this;
    }

    public WhereStatementBuilder<T> between(String column, Object from, Object to) {
        this.whereSb.append(column).append(" BETWEEN '")
                .append(from).append("' AND '")
                .append(to).append("'");
        return this;
    }

    public WhereStatementBuilder<T> gt(String column, Object value) {
        this.whereSb.append(column).append(" > '").append(value).append("'");
        return this;
    }
    public WhereStatementBuilder<T> gtEqual(String column, Object value) {
        this.whereSb.append(column).append(" >= '").append(value).append("'");
        return this;
    }
    public WhereStatementBuilder<T> lt(String column, Object value) {
        this.whereSb.append(column).append(" < '").append(value).append("'");
        return this;
    }
    public WhereStatementBuilder<T> ltEqual(String column, Object value) {
        this.whereSb.append(column).append(" <= '").append(value).append("'");
        return this;
    }

    /**
     * @return The WHERE clause only
     */
    public String generate() {
        return "WHERE " + this.whereSb.toString();
    }

    /**
     * @return The main statement including this where clause
     */
    public String build() {
        String where = this.whereSb.toString();
        if (StringUtils.isNotEmpty(where)) {
            if (crudBuilder instanceof SelectStatementBuilder) {
                return String.format(((SelectStatementBuilder)this.crudBuilder)
                        .generateStatementTemplateForWhereClause(), this.generate());
            } else {
                return String.format("%s %s", this.crudBuilder.generateStatement(), this.generate());
            }
        } else {
            return this.crudBuilder.build();
        }
    }

    public T crudBuilder() {
        return this.crudBuilder;
    }
}
