package com.api.demo.dto;


import lombok.Data;

import java.util.List;

@Data
public class SurveyStatisticsResponse {

    private Long surveyId;
    private String title;
    private int totalRespondents;
    private List<QuestionStatisticsDto> questions;
}
