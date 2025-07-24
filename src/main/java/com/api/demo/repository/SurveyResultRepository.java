package com.api.demo.repository;

import com.api.demo.model.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
    List<SurveyResult> findBySurveyId(Long surveyId);
    // SurveyResultRepository.java
    boolean existsByMemberEmail(String email);

}
