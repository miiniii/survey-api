package com.api.demo.dto;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuestionStatisticsDto {

    private Long questionId;
    private String type;
    private String title;

    //객관식
    private Map<String, Integer> statistics;

    //주관식
    private List<String> responses;

}
