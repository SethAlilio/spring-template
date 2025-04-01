package com.sethdev.cbpm.models.constants;

import java.util.Arrays;

public enum BackupType {
    STRUCTURE_ONLY, DATA_ONLY, STRUCTURE_AND_DATA;

    public static BackupType get(String backupType) {
        return Arrays.stream(values()).filter(x -> x.name().equals(backupType)).findFirst().orElse(null);
    }
}
