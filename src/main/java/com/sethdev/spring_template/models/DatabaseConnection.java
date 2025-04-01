package com.sethdev.spring_template.models;

import com.sethdev.spring_template.models.constants.Crud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConnection {
    private Integer id;
    private String type;
    private String name;
    private String host;
    private String port;
    private String schema;
    private String username;
    private String password;
    private LocalDateTime createDate;
    private Integer createBy;
    private LocalDateTime updateDate;
    private Integer updateBy;

    /** Additional */

    private String typeName;

    public boolean hasMissingInfo(Crud crud) {
        if (Crud.CREATE.equals(crud) || Crud.TEST.equals(crud)) {
            return StringUtils.isBlank(this.type) ||
                    StringUtils.isBlank(this.name) ||
                    StringUtils.isBlank(this.host) ||
                    StringUtils.isBlank(this.port) ||
                    StringUtils.isBlank(this.schema) ||
                    StringUtils.isBlank(this.username) ||
                    StringUtils.isBlank(this.password);
        } else if (Crud.UPDATE.equals(crud)) {
            return this.id == null ||
                    StringUtils.isBlank(this.type) ||
                    StringUtils.isBlank(this.name) ||
                    StringUtils.isBlank(this.host) ||
                    StringUtils.isBlank(this.port) ||
                    StringUtils.isBlank(this.schema) ||
                    StringUtils.isBlank(this.username) ||
                    StringUtils.isBlank(this.password);
        } else {
            return false;
        }
    }
}
