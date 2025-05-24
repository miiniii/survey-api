package com.api.demo.service;

import com.api.demo.dto.*;
import com.api.demo.entity.*;
import com.api.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final SurveyResultRepository surveyResultRepository;

    public SurveyResponseMain getSurveyByCompanyId(Long companyId) {

        final List<SurveyDto> surveyList = surveyRepository.findSurveyWith(companyId);
        // todo: SurveyResponseMain 로 만들어주기.
        SurveyResponseMain data = new SurveyResponseMain();
        data.setCompany(surveyList.get(0).getCompanyName());    // todo: surveyList가 비어있으면, NPE 가 나서 개선이 필요합니다.

        Map<String, CategoryDto> categoryMap = new LinkedHashMap<>();

        for (SurveyDto survey : surveyList) {
            String categoryName = survey.getCategoryName();

            CategoryDto category = categoryMap.computeIfAbsent(categoryName, name -> {
                CategoryDto c = new CategoryDto();
                c.setName(name);
                c.setQuestions(new ArrayList<>());
                return c;
            });

            QuestionDto question = new QuestionDto();
            question.setId("q" + survey.getId());
            question.setType(survey.getType().name().toLowerCase());
            question.setTitle(survey.getTitle());
            question.setSubtitle(survey.getSubtitle());
            question.setMax(survey.getMax());
            question.setRequired(survey.isRequired());
            question.setOptions(Collections.emptyList());   // default 로 빈 리스트 할당.

            // 옵션은 콤마(,)로 구분된 문자열이라 가정
            if (survey.getOptions() != null && !survey.getOptions().isEmpty()) {
                question.setOptions(List.of(survey.getOptions().split(",")));
            }

            category.getQuestions().add(question);
        }

        data.setCategories(new ArrayList<>(categoryMap.values()));
        return data;

    }

    public void saveSurveyResult(SurveySubmitRequest request) {
        MemberDto memberDto = request.getMember();

        // todo 회원의 중복가입을 피하기 위한 방어로직이 필요합니다.
        Member member = new Member();
        member.setName(memberDto.getName());
        member.setEmail(memberDto.getEmail());
        member.setGender(memberDto.getGender());
        member.setAgeRange(memberDto.getAgeRange());

        memberRepository.save(member);

        List<Long> surveyIds = request.getResponses().stream()
                .map(SurveyAnswerDto::getSurveyId)
                .collect(Collectors.toList());

        List<Survey> surveys = surveyRepository.findAllById(surveyIds);

        Map<Long, Survey> surveyMap = surveys.stream()
                .collect(Collectors.toMap(Survey::getId, Function.identity()));

        List<SurveyResult> results = new ArrayList<>();
        for (SurveyAnswerDto answer : request.getResponses()) {
            Survey survey = surveyMap.get(answer.getSurveyId());
            if (survey == null) {
                throw new IllegalArgumentException("해당 설문이 존재하지 않습니다.");
            }
            SurveyResult result = new SurveyResult();
            result.setMember(member);
            result.setSurvey(survey);
            result.setAnswerText(answer.getAnswerText());
            results.add(result);
        }

        surveyResultRepository.saveAll(results);
    }


}
