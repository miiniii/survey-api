package com.api.demo.repository;

import com.api.demo.entity.Member;
import com.api.demo.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
    List<SurveyResult> findByMember(Member member);
}
