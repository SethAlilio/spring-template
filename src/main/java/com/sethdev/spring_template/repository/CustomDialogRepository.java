package com.sethdev.spring_template.repository;

import com.sethdev.spring_template.models.sys.dialog.CustomDialog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Mapper
@Repository
public interface CustomDialogRepository {

    void create(CustomDialog dialog);

    void update(CustomDialog dialog);

    CustomDialog getByKey(String key);
    Integer getIdByKey(String key);

    String getPrimaryKeyOfTable(String tableName);

    List<CustomDialog> getCustomDialogList(Map<String, Object> param);
    int getCustomDialogListCount(Map<String, Object> param);

    String getDialogColumns(Integer id);
    CustomDialog getDialogForSearchFieldOnChange(Integer id);

    List<String> getTableNames(String databaseName);
    List<Map<String, String>> getTableColumns(String table);

    boolean isExistingKey(String key);

    void delete(Integer id);
}
