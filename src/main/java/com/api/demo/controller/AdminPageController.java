package com.api.demo.controller;

import com.api.demo.dto.SurveyResponseMain;
import com.api.demo.dto.SurveyStatisticsResponse;
import com.api.demo.model.entity.Company;
import com.api.demo.model.entity.Member;
import com.api.demo.service.CompanyService;
import com.api.demo.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {

    private final SurveyService surveyService;
    private final CompanyService companyService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        List<Company> companies = companyService.findAll();
        Member member = (Member) authentication.getPrincipal(); // 캐스팅 주의
        model.addAttribute("memberName", member.getName());     // 이름 꺼내기
        model.addAttribute("companies", companies);
        return "admin-dashboard"; // 관리자 메인
    }

    @GetMapping("/survey/manage")
    public String surveyManagePage(@RequestParam Long companyId, Model model) {
        SurveyResponseMain surveyData = surveyService.getSurveyByCompanyId(companyId);
        model.addAttribute("surveyData", surveyData);
        model.addAttribute("companyId", companyId);
        return "admin-survey-manage";
    }

    @GetMapping("/survey/statistics")
    public String surveyStatisticsPage(@RequestParam Long surveyId, Model model) {
        SurveyStatisticsResponse statistics = surveyService.getSurveyStatistics(surveyId);
        model.addAttribute("statistics", statistics);
        model.addAttribute("surveyType", statistics.getType()); // "RATING", "RADIO", etc
        return "admin-survey-statistics";
    }
}

