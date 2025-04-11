package com.sethdev.spring_template.models.constants;

/**
 * Value for user.permission column
 */
public enum UserPermissionType {
    ROLE, //Resources are based on the permission assigned to role
    USER  //Resources are based on the permission assigned to specific user
}
