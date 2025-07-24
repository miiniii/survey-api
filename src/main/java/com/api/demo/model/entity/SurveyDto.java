package com.api.demo.model.entity;

import com.api.demo.model.entity.enums.QuestionType;

import java.time.LocalDateTime;

public interface SurveyDto {

    String getCompanyName();

    String getCategoryName();

    Integer getSortOrder();

    Long getId();

    Long getCompanyId();

    Long getCategoryId();

    QuestionType getType();

    String getTitle();

    String getSubtitle();

    Integer getMax();

    boolean isRequired();

    String getOptions();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
