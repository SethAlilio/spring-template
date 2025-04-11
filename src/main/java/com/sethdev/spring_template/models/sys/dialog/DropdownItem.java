package com.sethdev.spring_template.models.sys.dialog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DropdownItem {
    private String label;
    private String value;
}
