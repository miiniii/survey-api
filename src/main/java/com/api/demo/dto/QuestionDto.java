package com.api.demo.dto;


import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {

    private String id;

    private String type;

    private String title;

    private String subtitle;

    private List<String> options;

    private Integer max;

    private boolean required;

}
