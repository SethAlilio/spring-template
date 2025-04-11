package com.sethdev.spring_template.service;

import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.sys.dialog.DropdownItem;

import java.util.List;
import java.util.Map;

public interface GroovyScriptService {
    /*void run(String script, ContextDto<TaskData> contextData);

    ResultMsg<?> runSafe(String script, ContextDto<TaskData> contextDto);

    Boolean runForBool(String script, ContextDto<TaskData> contextData) throws Exception;

    <T> ResultMsg<T> runForResultMsg(String script, ContextDto<TaskData> contextData, Class<T> clazz) throws Exception;

    <T> ResultMsg<T> runForResultMsgSafe(String script, ContextDto<TaskData> contextDto, Class<T> clazz);

    List<User> runForUserList(String script, ContextDto<TaskData> contextData) throws Exception;

    TaskData runForTaskData(String script, ContextDto<TaskData> contextData);*/

    Boolean runForCustomDialogDisabledScript(String script, Map<String, Object> data) throws Exception;

    Object runForCustomDialogScriptColumn(String script, Map<String, Object> data);

    List<DropdownItem> runForCustomDialogSearchFieldScriptColumn(String script,
                                                                 Map<String, Object> filterParams,
                                                                 Map<String, Object> searchParams);
}
