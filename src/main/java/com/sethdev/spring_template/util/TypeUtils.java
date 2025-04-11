package com.sethdev.spring_template.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TypeUtils {
    public static <T> List<T> objectToList(Object obj, Class<T> clazz) {
        if (obj == null) return null;

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(obj);

        if (jsonElement.isJsonArray()) {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(jsonElement, type);
        } else {
            return null;
        }
    }

    public static <T> List<T> stringToList(String json, Class<T> clazz) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<T>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static <T> List<T> parameterizeList(List list, Class<T> clazz) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        } else {
            List<T> newList = new ArrayList<>();
            Gson gson = new Gson();
            for (Object obj : list) {
                String json = gson.toJson(obj);
                newList.add(gson.fromJson(json, clazz));
                /*if (clazz.isInstance(obj)) {
                    newList.add(clazz.cast(obj));
                }*/
            }
            return newList;
        }
    }
}
