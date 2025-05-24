package com.api.demo.dto;


import lombok.Data;

import java.util.List;

@Data
public class SurveySubmitRequest {

    private MemberDto member;
    private List<SurveyAnswerDto> responses;
}
