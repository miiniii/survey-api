package com.api.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyStatisticsResponse {

    private Long surveyId;

    private String title;

    private String type; // radio, checkbox, etc

    private int totalResponses;

    //옵션별 응답 수
    private Map<String, Integer> optionCounts;

    //rating일 경우 평균 점수
    private Double averageRating;

}
