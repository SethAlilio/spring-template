package com.sethdev.spring_template.util;

import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class GsonUtils {
    public static String toJson(Object object) {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create().toJson(object);
    }
}
