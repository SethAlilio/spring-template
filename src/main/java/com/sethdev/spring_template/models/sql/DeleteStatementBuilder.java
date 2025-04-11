package com.sethdev.spring_template.models.sql;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class DeleteStatementBuilder implements CrudBuilder {

    private String table;
    private WhereStatementBuilder<DeleteStatementBuilder> where;

    public DeleteStatementBuilder table(String tableName) {
        this.table = tableName;
        return this;
    }

    public DeleteStatementBuilder where(WhereStatementBuilder<DeleteStatementBuilder> where) {
        this.where = where;
        return this;
    }

    public WhereStatementBuilder<DeleteStatementBuilder> where() {
        if (this.where == null) {
            this.where = new WhereStatementBuilder<>(this);
        }
        return this.where;
    }

    /**
     * @return Complete query including the where clause
     */
    @Override
    public String build() {
        return this.where != null ? this.where.build() : this.generateStatement();
    }

    /**
     * @return Statement excluding the where clause
     */
    @Override
    public String generateStatement() {
        if (StringUtils.isNotEmpty(this.table)) {
            return String.format("DELETE FROM %s", this.table);
        } else {
            return "";
        }
    }
}
