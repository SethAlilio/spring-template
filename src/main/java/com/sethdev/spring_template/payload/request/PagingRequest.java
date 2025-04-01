package com.sethdev.spring_template.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest {
    private Map<String, Object> params;
    private int start;
    private int limit;
}
