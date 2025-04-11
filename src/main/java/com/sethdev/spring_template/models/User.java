package com.sethdev.spring_template.models;

import com.sethdev.spring_template.models.base.BaseModel;
import com.sethdev.spring_template.models.sys.SysRelation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseModel {
    private String username;
    private String fullName;

    private String email;
    private String password;
    //private Set<Role> roles = new HashSet<>();
    private Integer relationId;

    /** {@link com.sethdev.spring_template.models.constants.UserPermissionType} */
    private String permission;

    private Boolean enabled;

    //Current role
    private Integer roleId;
    private String role;

    //Current group
    private Integer groupId;
    private String group;
    private LocalDateTime createDate;

    private List<SysRelation> relationList;


    /** User specific permissions */
    private List<ResourceNode<Integer>> permissionTree;

    private Map<String, ResourceNodeCheck> selectedPermissions;

    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    public boolean isCompleteInput() {
        return StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(fullName);
    }
}
