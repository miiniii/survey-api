package com.api.demo.dto;


import lombok.Data;

import java.util.List;

@Data
public class CategoryDto {
    private String name;
    private List<QuestionDto> questions;
}
