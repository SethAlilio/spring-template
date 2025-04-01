package com.sethdev.spring_template.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultPage<T> {
    private List<T> data;
    private int totalCount;
    private int pageSize;
    private int pageStart;
}
