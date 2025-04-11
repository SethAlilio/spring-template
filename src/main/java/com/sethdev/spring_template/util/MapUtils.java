package com.sethdev.spring_template.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtils {

    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, Map.class);
    }

    public static List<Map<String, Object>> objectToListMap(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(mapper.writeValueAsString(object), List.class);
    }

    public static List<Map<String, String>> changeLisMapValueTypeToStr(List<Map<String, Object>> list) {
        return list.stream()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            Object value = entry.getValue();
                            if (value != null) {
                                return value instanceof String ? (String) value : String.valueOf(value);
                            }
                            return null; // Handle null values as needed
                        })))
                .collect(Collectors.toList());
    }

    public static Object getBusEntityPropData(Map<String, Object> data, String entityKey, String propName) {
        Map<String, Object> entityData = objectToMap(data.get(entityKey));
        return entityData.get(propName);
    }
}
