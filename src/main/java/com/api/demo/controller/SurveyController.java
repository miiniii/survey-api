package com.api.demo.controller;


import com.api.demo.dto.SurveyResponseMain;
import com.api.demo.dto.SurveyStatisticsResponse;
import com.api.demo.dto.SurveySubmitRequest;
import com.api.demo.entity.Member;
import com.api.demo.repository.MemberRepository;
import com.api.demo.repository.SurveyResultRepository;
import com.api.demo.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyResultRepository surveyResultRepository;

    @Operation(summary = "설문 목록 조회")
    @GetMapping("/{companyId}")
    public ResponseEntity<SurveyResponseMain> getSurveyByCompanyId(@PathVariable Long companyId) {
        log.info("[GET] 설문 목록 조회 요청 - companyId: {}", companyId);
        SurveyResponseMain responseDto = surveyService.getSurveyByCompanyId(companyId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "설문 목록 제출")
    @PostMapping
    public ResponseEntity<String> submitSurvey(@RequestBody SurveySubmitRequest request) {

        surveyService.saveSurveyResult(request);

        // 저장 이후 DB에서 id가 생성된 member 조회
        Member member = surveyService.findMemberByEmail(request.getMember().getEmail());

        log.info("[POST] 설문 제출 요청 - memberId: {}, name: {}, email: {}",
                member.getId(), member.getName(), member.getEmail());

        return ResponseEntity.ok("설문조사 결과가 저장되었습니다.");
    }

    @Operation(summary = "설문 통계 조회")
    @GetMapping("/statistics/{surveyId}")
    public ResponseEntity<SurveyStatisticsResponse> getSurveyStatistics(@PathVariable Long surveyId) {
        log.info("[GET] 설문 통계 조회 요청 - surveyId: {}", surveyId);
        SurveyStatisticsResponse statistics = surveyService.getSurveyStatistics(surveyId);
        return ResponseEntity.ok(statistics);
    }


}


