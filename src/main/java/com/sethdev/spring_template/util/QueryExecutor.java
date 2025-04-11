package com.sethdev.spring_template.util;

import com.sethdev.spring_template.models.sql.DeleteStatementBuilder;
import com.sethdev.spring_template.models.sql.InsertStatementBuilder;
import com.sethdev.spring_template.models.sql.SelectStatementBuilder;
import com.sethdev.spring_template.models.sql.UpdateStatementBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Getter
@Slf4j
public class QueryExecutor {

    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    String url;
    @Value("${spring.datasource.username}")
    String username;
    @Value("${spring.datasource.password}")
    String password;

    @PostConstruct
    public void init() {
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            //dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception ignored) { }
    }

    /** SELECT **/

    public List<Map<String, Object>> select(String sql) {
        log.info("QueryExecutor | select: " + sql);
        return StringUtils.isNotBlank(sql) ? jdbcTemplate.queryForList(sql) : null;
    }
    public List<Map<String, Object>> select(SelectStatementBuilder builder) {
        return this.select(builder != null ? builder.build() : null);
    }

    //@Deprecated //Use selectOne(String query)
    public <T>T selectOne(String query, Class<T> clazz) {
        log.info("QueryExecutor | selectOne: " + query);
        return jdbcTemplate.queryForObject(query, clazz);
    }

    /**
     * Use for count or queries that has always return
     */
    public <T>T selectOne(SelectStatementBuilder builder, Class<T> clazz) {
        String query = builder.build();
        log.info("QueryExecutor | selectOne: " + query);
        return jdbcTemplate.queryForObject(query, clazz);
    }

    public Map<String, Object> selectOne(String query) {
        log.info("QueryExecutor | selectOne: " + query);
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(query);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Deprecated //Throws exception if no rows returned
    public Map<String, Object> selectOneMap(String query) {
        log.info("QueryExecutor | selectOneMap: " + query);
        return jdbcTemplate.queryForMap(query);
    }

    /** INSERT **/

    public int insert(String sql) {
        log.info("QueryExecutor | insert: " + sql);
        if (StringUtils.isNotBlank(sql)) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS), keyHolder);
            return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : -1;
        } else {
            return 0;
        }
    }

    public int insert(InsertStatementBuilder builder) {
        return builder != null ? this.insert(builder.build()) : 0;
    }

    //TODO: CONTINUE THIS
    /*public List<Long> insertMultiple(InsertStatementBuilder insertBuilder) {
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", insertBuilder.getTable(),
                insertBuilder.colsAsString(), insertBuilder.getValuePlaceholder());

        log.info("insertMultiple.sql: " + sql);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        log.info("insertMultiple.keyHolder.KeyList: " + keyHolder.getKeyList().size());
        List<Long> generatedIds = keyHolder.getKeyList().stream()
                .map(x -> {
                    log.info("insertMultiple.x: " + new Gson().toJson(x));
                    return (Long) x.get(insertBuilder.getPk());
                })
                .collect(Collectors.toList());
        log.info("insertMultiple.generatedIds: " + new Gson().toJson(generatedIds));

        return generatedIds;

    }*/

    /** UPDATE **/

    public int update(String sql) {
        log.info("QueryExecutor | update: " + sql);

        return StringUtils.isNotBlank(sql) ? jdbcTemplate.update(sql) : 0;
    }

    public int update(UpdateStatementBuilder updateBuilder) {
        return this.update(updateBuilder != null ? updateBuilder.build() : null);
    }

    /** DELETE **/

    public int delete(String sql) {
        log.info("QueryExecutor | delete: " + sql);
        return StringUtils.isNotBlank(sql) ? jdbcTemplate.update(sql) : 0;
    }

    public int delete(DeleteStatementBuilder deleteBuilder) {
        return this.delete(deleteBuilder != null ? deleteBuilder.build() : null);
    }

    /** MISC/UTILITIES */

    public String getWhereClause(String query) {
        // Define the pattern to match the WHERE clause
        Pattern pattern = Pattern.compile("WHERE\\s+(.+)");

        // Match the pattern against the query string
        Matcher matcher = pattern.matcher(query);

        // Check if the WHERE clause is found and extract it
        if (matcher.find()) {
            return "WHERE " + matcher.group(1).trim();
        } else {
            return "";
        }
    }
}
