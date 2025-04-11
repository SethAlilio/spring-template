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
public class InsertStatementBuilder implements CrudBuilder {

    private String table;
    private Map<String, Object> colVal;

    public InsertStatementBuilder table(String tableName) {
        this.table = tableName;
        return this;
    }

    public InsertStatementBuilder colVal(String column, Object value) {
        if (this.colVal == null) {
            this.colVal = new HashMap<>();
        }
        this.colVal.put(column, value);
        return this;
    }

    @Override
    public String build() {
        return this.generateStatement();
    }

    @Override
    public String generateStatement() {
        if (StringUtils.isNotBlank(this.table) && MapUtils.isNotEmpty(this.colVal)) {
            String format = "INSERT INTO %s (%s) VALUES (%s)";
            List<Map.Entry<String, Object>> entrySet = new ArrayList<>(this.colVal.entrySet());
            StringBuilder sbCols = new StringBuilder();
            StringBuilder sbVals = new StringBuilder();
            for (int i = 0; i < entrySet.size(); i++) {
                sbCols.append(entrySet.get(i).getKey());
                if (this.shouldWrapValue(entrySet.get(i).getValue())) {
                    String value = String.valueOf(entrySet.get(i).getValue());
                    sbVals.append("'").append(value.replace("'", "\\'")).append("'");
                } else {
                    sbVals.append(entrySet.get(i).getValue());
                }
                if (i < entrySet.size()-1) {
                    sbCols.append(", ");
                    sbVals.append(", ");
                }
            }
            return String.format(format, this.table, sbCols.toString(), sbVals.toString());
        } else {
            return "";
        }
    }

    public boolean shouldWrapValue(Object value) {
        if (value != null) {
            String str = value.toString().toLowerCase().trim();
            return !str.equals("now()") && !str.equals("null");
        } else {
            return false;
        }
    }
}
