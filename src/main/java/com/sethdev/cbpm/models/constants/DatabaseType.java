package com.sethdev.cbpm.models.constants;

import com.sethdev.cbpm.util.MapBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum DatabaseType {

    MY_SQL("MySQL"),
    ORACLE("Oracle"),
    MONGO_DB("MongoDB"),
    ;

    private final String displayName;

    public static List<Map<String, String>> getAsSelection() {
        return Arrays.stream(values())
                .map(x -> new MapBuilder<String, String>()
                        .put("name", x.getDisplayName())
                        .put("value", x.name())
                        .build())
                .collect(Collectors.toList());
    }

    public static String getDisplayName(String type) {
        for (DatabaseType dt : values()) {
            if (dt.name().equals(type)) {
                return dt.getDisplayName();
            }
        }
        return "";
    }
}
