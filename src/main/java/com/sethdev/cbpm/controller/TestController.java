package com.sethdev.cbpm.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sethdev.cbpm.models.test.FormComponentValue;
import com.sethdev.cbpm.util.MapBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {


    @PostMapping("/dingtalk/access")
    public Map<String, String> getDingTalkAccess(@RequestBody Map<String, String> params) {

        String url = "https://api.dingtalk.com/v1.0/oauth2/accessToken"; // Replace with your API endpoint

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HttpEntity with headers and body
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {}
        );

        // Get response body as Map
        return responseEntity.getBody();
    }

    @PostMapping("/dingtalk/instanceIds/query")
    public Map<String, Object> getProcessInstIds(@RequestBody Map<String, String> params) {

        String url = "https://api.dingtalk.com/v1.0/workflow/processes/instanceIds/query"; // Replace with your API endpoint

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String accessToken = MapUtils.getString(params, "accessToken");
        headers.set("x-acs-dingtalk-access-token", accessToken);

        Map<String, Object> request = new MapBuilder<String, Object>()
                .put("startTime", MapUtils.getLong(params, "startTime"))
                .put("endTime", MapUtils.getLong(params, "endTime"))
                .put("processCode", MapUtils.getString(params, "processCode"))
                .put("nextToken", MapUtils.getInteger(params, "nextToken"))
                .put("maxResults", MapUtils.getInteger(params, "maxResults"))
                .build();

        // Create HttpEntity with headers and body
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        // Get response body as Map
        Map<String, Object> returnObj = responseEntity.getBody();
        Map<String, Object> result = (Map<String, Object>) returnObj.get("result");
        List<String> ids = ((List<String>) result.get("list"));
        if (CollectionUtils.isNotEmpty(ids)) {
            List<Map<String, Object>> instanceList = new ArrayList<>();
            ids.forEach(x -> {
                instanceList.add(getProcessInstanceDetails(x, accessToken));
            });
            result.put("instanceList", instanceList);
        }

        return result;
    }

    @PostMapping("/dingtalk/instance")
    public Map<String, Object> getProcessInstanceDetails(@RequestParam String instId, @RequestParam String accessToken) {
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-acs-dingtalk-access-token", accessToken);

        String url = UriComponentsBuilder.fromHttpUrl("https://api.dingtalk.com/v1.0/workflow/processInstances")
                .queryParam("processInstanceId", instId)
                .toUriString();

        // Create HttpEntity with headers and body
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        // Get response body as Map
        return parseFormData(responseEntity.getBody());
    }

    public Map<String, Object> parseFormData(Map<String, Object> data) {
        Gson gson = new Gson();
        String resultJson = gson.toJson(data.get("result"));
        Map<String, Object> resultMap = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>(){}.getType());
        String formComponentValuesJson = gson.toJson(resultMap.get("formComponentValues"));

        Type listType = new TypeToken<List<FormComponentValue>>(){}.getType();

        // Extract formComponentValues from the JSON
        List<FormComponentValue> formComponentValues = new Gson().fromJson(formComponentValuesJson, listType);
        Map<String, Object> finalData = new HashMap<>();
        finalData.put("plateNumber", formComponentValues.stream()
                .filter(x -> x.getId().equals("TextField_OD41LPEWDHC0"))
                .findFirst().orElse(null).getValue());
        finalData.put("driverName", formComponentValues.stream()
                .filter(x -> x.getId().equals("TextField_1033NTSLBO340"))
                .findFirst().orElse(null).getValue());
        finalData.put("driverContact", formComponentValues.stream()
                .filter(x -> x.getId().equals("TextField_11THX5VSHMM80"))
                .findFirst().orElse(null).getValue());
        finalData.put("region", formComponentValues.stream()
                .filter(x -> x.getId().equals("DDSelectField_LRWHMTY9AS00"))
                .findFirst().orElse(null).getValue());
        finalData.put("area", formComponentValues.stream()
                .filter(x -> x.getId().equals("DDSelectField_VNXQV2FVC340"))
                .findFirst().orElse(null).getValue());
        finalData.put("projectDepartment", formComponentValues.stream()
                .filter(x -> x.getId().equals("TextField_1TSF1TL7LKQO0"))
                .findFirst().orElse(null).getValue());

        return finalData;
    }
}
