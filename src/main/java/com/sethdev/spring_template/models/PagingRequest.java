package com.sethdev.spring_template.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest<T> {

    private Integer start;
    private Integer limit;
    private T query;

}
