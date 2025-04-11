package com.sethdev.spring_template.controller;

import com.sethdev.spring_template.models.ResultMsg;
import com.sethdev.spring_template.models.ResultPage;
import com.sethdev.spring_template.models.sys.dialog.CustomDialog;
import com.sethdev.spring_template.models.sys.dialog.TreeData;
import com.sethdev.spring_template.service.CustomDialogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/custom/dialog")
@CrossOrigin
@Slf4j
public class CustomDialogController {
    @Autowired
    CustomDialogService customDialogService;

    @PostMapping("/save")
    public ResultMsg<CustomDialog> save(@RequestBody CustomDialog dialog) {
        ResultMsg<CustomDialog> result = new ResultMsg<>();
        try {
            String action = dialog.getId() == null ? "created" : "updated";
            CustomDialog savedDialog = customDialogService.save(dialog);
            result.setData(savedDialog);
            result.setMessage("Dialog " + action);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @PostMapping("/create")
    public ResultMsg<CustomDialog> create(@RequestBody CustomDialog dialog) {
        ResultMsg<CustomDialog> result = new ResultMsg<>();
        try {
            customDialogService.create(dialog);
            result.setData(dialog);
            result.setMessage("Dialog created");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    public ResultMsg<CustomDialog> update(@RequestBody CustomDialog dialog) {
        ResultMsg<CustomDialog> result = new ResultMsg<>();
        try {
            customDialogService.update(dialog);
            result.setData(dialog);
            result.setMessage("Dialog updated");
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @PostMapping("/getByKey")
    public ResultMsg<CustomDialog> getByKey(@RequestParam String dialogKey,
                                            @RequestParam (required = false) boolean forEdit) {
        try {
            CustomDialog dialog = customDialogService.getByKey(dialogKey, forEdit);
            if (dialog == null) {
                return new ResultMsg<CustomDialog>().failure("Dialog key not found");
            }
            return new ResultMsg<CustomDialog>().success(dialog);
        } catch (Exception e) {
            return new ResultMsg<CustomDialog>().failure(e.getMessage());
        }
    }

    @PostMapping("/queryDataList")
    public ResultPage<Map<String, Object>> getDialogDataList(@RequestBody Map<String, Object> params) {
        return customDialogService.getDialogDataListV2(params);
        //return customDialogService.getDialogDataList(params);
    }

    @PostMapping("/queryDataTree")
    public ResultMsg<TreeData> getDialogDataTree(@RequestBody Map<String, Object> params) {
        return customDialogService.getDialogDataTreeV2(params);
        //return customDialogService.getDialogDataTree(dialogKey);
    }

    @PostMapping("/generateSelectSql")
    public ResultMsg<Map<String, String>> generateSelectSql(@RequestBody CustomDialog dialog) {
        Map<String, String> queryMap = new HashMap<>();
        List<String> selectColumns = dialog.getColumnsForSql();
        queryMap.put("list", "SELECT " + String.join(", ", selectColumns) + " FROM " + dialog.getQueryTable());
        if (CustomDialog.DialogType.LIST.name().equals(dialog.getType())) {
            queryMap.put("count", "SELECT COUNT(0) FROM " + dialog.getQueryTable());
        }
        return new ResultMsg<>(true, "", queryMap);
    }

    @PostMapping("/query")
    public ResultPage<CustomDialog> getCustomDialogList(@RequestBody Map<String, Object> params) {
        try {
            return ResultPage.<CustomDialog>builder()
                    .data(customDialogService.getCustomDialogList(params))
                    .totalCount(customDialogService.getCustomDialogListCount(params))
                    .pageSize((int) params.get("limit"))
                    .pageStart((int) params.get("start"))
                    .build();
        } catch (Exception e) {
            log.info("getCustomDialogList.e: " + ExceptionUtils.getStackTrace(e));
            return new ResultPage<>();
        }
    }

    @PostMapping("/handleSearchFieldOnChange")
    public ResultMsg<List<Map<String, Object>>> handleSearchFieldOnChange(@RequestBody Map<String, Object> params) {
        ResultMsg<List<Map<String, Object>>> result = new ResultMsg<>();
        try {
            Integer id = MapUtils.getInteger(params, "id");
            String columnName = MapUtils.getString(params, "columnName");
            Map<String, Object> filterParams = com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("filterParams"));
            Map<String, Object> searchParams = com.sethdev.spring_template.util.MapUtils.objectToMap(params.get("searchParams"));
            result.setData(customDialogService.handleSearchFieldOnChange(id, columnName, filterParams, searchParams));
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error handling on change");
        }
        return result;
    }

    @PostMapping("/getTableOptions")
    public ResultMsg<List<Map<String, String>>> getTableOptions() {
        return new ResultMsg<List<Map<String, String>>>().success(customDialogService.getTableOptions());
    }

    @PostMapping("/getTableColumns")
    public ResultMsg<List<Map<String, String>>> getTableColumns(@RequestParam String table) {
        return new ResultMsg<List<Map<String, String>>>().success(customDialogService.getTableColumns(table));
    }
    @PostMapping("/delete")
    public ResultMsg<?> deleteDialog(@RequestParam Integer id) {
        ResultMsg<?> resultMsg = new ResultMsg<>();
        try {
            customDialogService.deleteDialog(id);
            resultMsg.setMessage("Dialog deleted");
        } catch (Exception e) {
            resultMsg.setSuccess(false);
            resultMsg.setMessage("Delete dialog failed");
        }
        return resultMsg;
    }
}
