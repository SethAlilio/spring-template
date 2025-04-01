package com.sethdev.cbpm.models.constants;

import java.util.Arrays;

public enum BackupSchedule {
    NOW, CUSTOM;

    public static BackupSchedule get(String schedule) {
        return Arrays.stream(values()).filter(x -> x.name().equals(schedule)).findFirst().orElse(null);
    }
}
