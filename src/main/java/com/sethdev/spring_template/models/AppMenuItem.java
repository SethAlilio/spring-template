package com.sethdev.spring_template.models;

import lombok.*;

import java.util.List;

/**
 * This is the model for front-end's CustomAppMenu menu items
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppMenuItem {
    private String label;
    private String icon;
    private String to;
    private List<AppMenuItem> items;
}
