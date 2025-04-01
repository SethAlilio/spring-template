package com.sethdev.spring_template.models.sys;

import com.sethdev.spring_template.models.base.BaseModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SysResource extends BaseModel {

    private Integer parentId;

    private Integer type;

    private String name;

    private Integer enabled;

    private String icon;

    private String path;

    private Integer sort;

}
