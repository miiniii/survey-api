package com.api.demo.model.result;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class RestResult {
    private LinkedHashMap<String, Object> data;

    public RestResult(LinkedHashMap<String, Object> data) {
        this.data = data;
    }
}
