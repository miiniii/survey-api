package com.api.demo.dto;


import lombok.Data;


@Data
public class SurveyAnswerDto {

    private Long memberId;

    private Long surveyId;

    private String surveyTitle;

    private String answerText;
}