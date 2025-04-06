package com.sethdev.spring_template.models;

import com.sethdev.spring_template.models.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Group extends BaseModel {
    private String name;
    private Integer parentId;
    private Integer type;
    private Boolean enabled;
    private String path;
}
