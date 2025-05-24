package com.api.demo.controller;


import com.api.demo.dto.SurveyResponseMain;
import com.api.demo.dto.SurveySubmitRequest;
import com.api.demo.repository.SurveyResultRepository;
import com.api.demo.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyResultRepository surveyResultRepository;

    // 특정 회사의 설문 항목 조회 API
    @GetMapping("/{companyId}")
    public ResponseEntity<SurveyResponseMain> getSurveyByCompanyId(@PathVariable Long companyId) {
        SurveyResponseMain responseDto = surveyService.getSurveyByCompanyId(companyId);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<String> submitSurvey(@RequestBody SurveySubmitRequest request) {
        surveyService.saveSurveyResult(request);
        return ResponseEntity.ok("설문조사 결과가 저장되었습니다.");
    }

}
