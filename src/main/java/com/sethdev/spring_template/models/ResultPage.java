package com.sethdev.spring_template.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultPage<T> {
    private List<T> data;
    private int totalCount;
    private int pageSize;
    private int pageStart;
}
