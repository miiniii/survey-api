package com.api.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class SurveyResponseMain {
    private String company;
    private List<CategoryDto> categories;

}
