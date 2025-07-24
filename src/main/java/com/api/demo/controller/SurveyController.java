package com.api.demo.controller;


import com.api.demo.dto.SurveyResponseMain;
import com.api.demo.dto.SurveyStatisticsResponse;
import com.api.demo.dto.SurveySubmitRequest;
import com.api.demo.model.entity.Company;
import com.api.demo.model.entity.SurveyResult;
import com.api.demo.model.result.RestResult;
import com.api.demo.service.CompanyService;
import com.api.demo.service.SurveyService;
import com.api.demo.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final CompanyService companyService;

    @Operation(summary = "설문 목록 조회")
    @GetMapping("/{companyId}")
    public RestResult getSurveyByCompanyId(@PathVariable Long companyId) {
        log.info("[GET] 설문 목록 조회 요청 - companyId: {}", companyId);
        SurveyResponseMain surveyResponseMain = surveyService.getSurveyByCompanyId(companyId);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("surveys", surveyResponseMain);
        return new RestResult(data);
    }

    @Operation(summary = "설문 목록 제출")
    @PostMapping()
    public RestResult submitSurvey(@RequestBody SurveySubmitRequest request) {

        log.info("[POST] 설문저장 요청 /api/survey {}", Utils.toJson(request));
        final List<SurveyResult> results = surveyService.saveSurveyResult(request);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("result", "설문조사 결과 " + results.size() + "건이 저장되었습니다.");
        return new RestResult(data);
    }

    @Operation(summary = "설문 통계 조회")
    @GetMapping("/statistics/{surveyId}")
    public RestResult getSurveyStatistics(@PathVariable Long surveyId) {
        log.info("[GET] 설문 통계 조회 요청 - surveyId: {}", surveyId);
        SurveyStatisticsResponse surveyStatisticsResponse = surveyService.getSurveyStatistics(surveyId);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("statistics", surveyStatisticsResponse);
        return new RestResult(data);
    }

    @GetMapping("/select-company")
    public String selectCompanyPage(Model model) {
        List<Company> companies = companyService.findAll();
        model.addAttribute("companies", companies);
        return "select-company"; // 회사 목록 보여주는 템플릿
    }


}


