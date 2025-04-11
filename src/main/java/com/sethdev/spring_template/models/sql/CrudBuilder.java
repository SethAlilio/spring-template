package com.sethdev.spring_template.models.sql;

public interface CrudBuilder {
    /**
     * @return Complete query including the where clause
     */
    String build();

    /**
     * @return Statement excluding the where clause
     */
    String generateStatement();
}
