package com.sethdev.spring_template.models.sys;

import com.sethdev.spring_template.models.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SysRelation extends BaseModel {
    private Integer userId;
    private Integer roleId;
    private Integer groupId;
    private Boolean enabled;

    private String userName;
    private String userUserName;
    private String roleName;
    private String groupName;

    private Integer relationId;

    private Boolean isActive;
}
