package com.sethdev.spring_template.models.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormComponentValue {
    private String componentType;
    private String name;
    private String bizAlias;
    private String id;
    private String value;
    private String extValue; // Optional, as not all objects have this field
}
