package com.sethdev.spring_template.models;

import lombok.*;

import java.util.List;

/**
 * Model for PrimeReact's Tree component value
 * @param <T>
 */
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
