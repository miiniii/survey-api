package com.api.demo.controller;


import com.api.demo.model.result.RestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthCheckApi {

    @GetMapping("/health")
    public RestResult healthCheck() {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("name", "swim-server");
        data.put("status", "UP");
        data.put("message", "OK");
        return new RestResult(data);
    }

}


