package com.sethdev.spring_template.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String username;

    private String fullName;

    private String email;

    private String role;

    private String password;

    public boolean isCompleteInput() {
        return StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(fullName)
                //&& StringUtils.isNotBlank(email)
                && StringUtils.isNotBlank(password);
    }
}
