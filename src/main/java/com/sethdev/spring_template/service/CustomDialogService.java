package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.PagingRequest;
import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.dialog.CustomDialog;
import com.sethdev.spring_template.models.sys.dialog.TreeData;

import java.util.List;
import java.util.Map;

public interface CustomDialogService {

    void validateCustomDialog(CustomDialog dialog) throws Exception;

    CustomDialog save(CustomDialog dialog) throws Exception;

    void create(CustomDialog dialog) throws Exception;

    void update(CustomDialog dialog) throws Exception;

    CustomDialog getByKey(String dialogKey, boolean forEdit);

    List<CustomDialog.Column> getColumns(Integer id);

    ResultPage<Map<String, Object>> getDialogDataListV2(Map<String, Object> params);

    ResultPage<Map<String, Object>> getDialogDataListV3(PagingRequest<CustomDialog> request);

    List<Map<String, Object>> handleSearchFieldOnChange(Integer id, String columnName,
                                                        Map<String, Object> filterParams,
                                                        Map<String, Object> searchParams);

    ResultMsg<TreeData> getDialogDataTreeV2(Map<String, Object> params);

    List<CustomDialog> getCustomDialogList(Map<String, Object> param);

    ResultPage<CustomDialog> getCustomDialogList(PagingRequest<CustomDialog> request);

    int getCustomDialogListCount(Map<String, Object> param);

    List<Map<String, String>> getTableOptions();
    List<Map<String, String>> getTableColumns(String table);

    void deleteDialog(Integer id);
}
