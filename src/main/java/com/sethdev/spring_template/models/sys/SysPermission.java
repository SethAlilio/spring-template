package com.sethdev.spring_template.models.sys;

import com.sethdev.spring_template.models.base.BaseModel;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysPermission extends BaseModel {

    private Integer userId;
    private Integer roleId;
    private Integer resourceId;
    private Integer type;

}
