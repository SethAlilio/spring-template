package com.sethdev.spring_template.models;

import com.sethdev.spring_template.models.base.BaseModel;
import com.sethdev.spring_template.models.sys.SysResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseModel {
    private String name;
    private String description;
    private Boolean enabled;

    private List<ResourceNode<SysResource>> permissionTree;

    private Map<String, ResourceNodeCheck> selectedPermissions;

}
