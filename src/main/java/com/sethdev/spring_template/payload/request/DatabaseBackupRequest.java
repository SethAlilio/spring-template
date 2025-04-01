package com.sethdev.spring_template.payload.request;

import com.sethdev.spring_template.models.DatabaseConnection;
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

    /** {@link com.sethdev.spring_template.models.constants.BackupType} */
    private String type;

    private List<BackupTable> backupTableList;

    /** {@link com.sethdev.spring_template.models.constants.BackupSchedule} */
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
