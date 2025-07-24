package com.api.demo.controller;

import com.api.demo.dto.SurveyResponseMain;
import com.api.demo.dto.SurveySubmitRequest;
import com.api.demo.exception.BadRequestRuntimeException;
import com.api.demo.model.entity.Company;
import com.api.demo.model.entity.Member;
import com.api.demo.model.entity.SurveyResult;
import com.api.demo.service.CompanyService;
import com.api.demo.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {

    private final SurveyService surveyService;
    private final CompanyService companyService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/main")
    public String mainPage(Model model, Authentication authentication) {
        Member member = (Member) authentication.getPrincipal(); // 캐스팅 주의
        List<Company> companies = companyService.findAll(); // 👈 추가
        model.addAttribute("memberName", member.getName());     // 이름 꺼내기
        model.addAttribute("companies", companies);
        return "main";
    }

    @GetMapping("/survey/participate")
    public String surveyParticipatePage(@RequestParam Long companyId, Model model, Authentication authentication) {
        SurveyResponseMain surveyData = surveyService.getSurveyByCompanyId(companyId);
        Member member = (Member) authentication.getPrincipal();

        model.addAttribute("surveyData", surveyData);
        model.addAttribute("companyId", companyId); // 제출할 때 필요
        model.addAttribute("memberName", member.getName());
        model.addAttribute("memberEmail", member.getEmail());
        return "user-survey"; // 설문 작성 화면
    }

    @PostMapping("/survey/submit")
    public String submitSurveyHtml
            (@ModelAttribute SurveySubmitRequest request, Authentication authentication,
             Model model, RedirectAttributes redirectAttributes
            ){
        try {
            Member member = (Member) authentication.getPrincipal();
            request.getMemberDto().setId(member.getId());

            List<SurveyResult> results = surveyService.saveSurveyResult(request);
            model.addAttribute("message", results.size() + "건 설문이 저장되었습니다.");
            return "survey-success";
        } catch (BadRequestRuntimeException e) {
            // 예외 발생 시 리다이렉트 + 알림 메시지 전달
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/survey/error";
        }
    }

    @GetMapping("/survey/submit")
    public String surveySubmitNotAllowedPage() {
        // 사용자가 직접 GET으로 접근하려 할 때 보여줄 안내 페이지
        return "redirect:/";  // 홈으로 보내거나 에러 안내 페이지로
    }


    @GetMapping("/survey/select")
    public String selectCompanyPage(Model model) {
        List<Company> companies = companyService.findAll();
        model.addAttribute("companies", companies);
        return "user-survey-select";
    }

    @GetMapping("/survey/error")
    public String showErrorPage(Model model) {
        model.addAttribute("message", "중복 설문으로 참여가 제한되었습니다.");
        return "survey-error";
    }


    @GetMapping("/surveys/new")
    public String newSurveyPage(Model model) {
        model.addAttribute("title", "새 설문 만들기");
        return "surveys-new";
    }

}
