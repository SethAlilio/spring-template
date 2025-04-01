package com.sethdev.cbpm.models.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.sethdev.cbpm.models.constants.BackupType.*;

@Getter
@AllArgsConstructor
public enum BackupAdvanceSetting {

    EX_1_1(STRUCTURE_ONLY, "If table exists skip it"),
    EX_1_2(STRUCTURE_ONLY, "If table exists truncate data"),
    EX_1_3(STRUCTURE_ONLY, "If table exists update the structure"),
    EX_1_4(STRUCTURE_ONLY, "If table exists truncate data and update the structure"),

    EX_2_1(DATA_ONLY, "If error occurs skip the current data then proceed to the next record"),
    EX_2_2(DATA_ONLY, "If error occurs skip the current data then proceed to the next record. Stop the backup after the current table is finished"),
    EX_2_3(DATA_ONLY, "If error occurs stop importing data on the current table then proceed to the next table"),


    /*EX_3_1(STRUCTURE_AND_DATA, "If table exists skip it"),

    EX_3_2(STRUCTURE_AND_DATA, "If table exists import new data"),
    EX_3_3(STRUCTURE_AND_DATA, "If table exists update the structure then import new data"),

    EX_3_4(STRUCTURE_AND_DATA, "If table exists truncate data then update the structure then import new data"),*/

    ;

    private BackupType type;
    private String description;
}
