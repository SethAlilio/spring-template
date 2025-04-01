package com.sethdev.spring_template.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceNode<T> {

    private String key;
    private String label;
    private T data;
    private String icon;
    private List<ResourceNode<T>> children;

}
