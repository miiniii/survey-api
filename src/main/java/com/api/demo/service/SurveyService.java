package com.api.demo.service;

import com.api.demo.dto.*;
import com.api.demo.entity.*;
import com.api.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final SurveyResultRepository surveyResultRepository;

    public SurveyResponseMain getSurveyByCompanyId(Long companyId) {

        final List<SurveyDto> surveyWith = surveyRepository.findSurveyWith(companyId);
        // todo: SurveyResponseMain 로 만들어주기.
        SurveyResponseMain data = new SurveyResponseMain();
        data.setCompany(surveyWith.get(0).getCompanyName());

        Map<String, CategoryDto> categoryMap = new LinkedHashMap<>();

        for (SurveyDto dto : surveyWith) {
            String categoryName = dto.getCategoryName();

            CategoryDto category = categoryMap.computeIfAbsent(categoryName, name -> {
                CategoryDto c = new CategoryDto();
                c.setName(name);
                c.setQuestions(new ArrayList<>());
                return c;
            });

            QuestionDto question = new QuestionDto();
            question.setId("q" + dto.getId());
            question.setType(dto.getType().name().toLowerCase());
            question.setTitle(dto.getTitle());
            question.setSubtitle(dto.getSubtitle());
            question.setMax(dto.getMax());
            question.setRequired(dto.isRequired());

            // 옵션은 콤마(,)로 구분된 문자열이라 가정
            if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                question.setOptions(List.of(dto.getOptions().split(",")));
            } else {
                question.setOptions(Collections.emptyList());
            }

            category.getQuestions().add(question);
        }

        data.setCategories(new ArrayList<>(categoryMap.values()));
        return data;

    }

    public void saveSurveyResult(SurveySubmitRequest request) {
        MemberDto memberDto = request.getMember();

        Member member = new Member();
        member.setName(memberDto.getName());
        member.setEmail(memberDto.getEmail());
        member.setGender(memberDto.getGender());
        member.setAgeRange(memberDto.getAgeRange());

        memberRepository.save(member);

        for (SurveyAnswerDto answer : request.getResponses()) {
            Survey survey = surveyRepository.findById(answer.getSurveyId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 설문이 존재하지 않습니다."));
            SurveyResult result = new SurveyResult();
            result.setMember(member);
            result.setSurvey(survey);
            result.setAnswerText(answer.getAnswerText());

            surveyResultRepository.save(result);
        }
    }


}
