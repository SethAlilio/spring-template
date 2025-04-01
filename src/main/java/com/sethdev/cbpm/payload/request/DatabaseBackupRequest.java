package com.sethdev.cbpm.payload.request;

import com.sethdev.cbpm.models.DatabaseConnection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseBackupRequest {

    private DatabaseConnection sourceDatabase;
    private DatabaseConnection destinationDatabase;

    /** {@link com.sethdev.cbpm.models.constants.BackupType} */
    private String type;

    private List<BackupTable> backupTableList;

    /** {@link com.sethdev.cbpm.models.constants.BackupSchedule} */
    private String schedule;

    private String scheduleTime;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class BackupTable {
        private Integer id;
        private String name;
    }


}
