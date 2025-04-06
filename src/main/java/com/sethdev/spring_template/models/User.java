package com.sethdev.spring_template.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String fullName;

    private String email;
    private String password;
    //private Set<Role> roles = new HashSet<>();
    private String relationId;
    private String role;
    private LocalDateTime createDate;

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
