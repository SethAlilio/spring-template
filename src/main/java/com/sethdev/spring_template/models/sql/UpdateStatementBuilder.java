package com.sethdev.spring_template.models.sql;

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
public class UpdateStatementBuilder implements CrudBuilder {
    private String table;
    private WhereStatementBuilder<UpdateStatementBuilder> where;
    private Map<String, Object> updateColumns;

    public UpdateStatementBuilder table(String tableName) {
        this.table = tableName;
        return this;
    }

    public UpdateStatementBuilder where(WhereStatementBuilder<UpdateStatementBuilder> where) {
        this.where = where;
        return this;
    }

    public WhereStatementBuilder<UpdateStatementBuilder> where() {
        if (this.where == null) {
            this.where = new WhereStatementBuilder<>(this);
        }
        return this.where;
    }

    public UpdateStatementBuilder updateColumn(String column, Object value) {
        if (this.updateColumns == null) {
            this.updateColumns = new HashMap<>();
        }
        this.updateColumns.put(column, value);
        return this;
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
        if (MapUtils.isNotEmpty(updateColumns) && StringUtils.isNotEmpty(this.table)) {
            List<String> updates = new ArrayList<>();
            for (Map.Entry<String, Object> col : updateColumns.entrySet()) {
                String value = String.valueOf(col.getValue());
                updates.add(col.getKey() + " = '" + value.replace("'", "\\'") + "'");
            }
            String updColStr = String.join(", ", updates);
            return String.format("UPDATE %s SET %s", this.table, updColStr);
        } else {
            return "";
        }
    }
}
