package com.api.demo.service;

import com.api.demo.dto.*;
import com.api.demo.entity.*;
import com.api.demo.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.api.demo.entity.enums.QuestionType.TEXTAREA;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final SurveyResultRepository surveyResultRepository;

    public SurveyResponseMain getSurveyByCompanyId(Long companyId) {

        final List<SurveyDto> surveyList = surveyRepository.findSurveyWith(companyId);

        if (surveyList.isEmpty()) {
            throw new NoSuchElementException("해당 회사 ID(" + companyId + ")에 대한 설문이 존재하지 않습니다.");
        }

        SurveyResponseMain data = new SurveyResponseMain();
        data.setCompany(surveyList.get(0).getCompanyName());

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

        Member member;
        if (memberDto.getId() != null) {
            member = memberRepository.findById(memberDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보가 존재하지 않습니다. id = " + memberDto.getId()));
        }

        // todo 회원의 중복가입을 피하기 위한 방어로직이 필요합니다.
        member = new Member();
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

    public SurveyStatisticsResponse getSurveyStatistics(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 설문이 존재하지 않습니다. id = " + surveyId));

        List<SurveyResult> results = surveyResultRepository.findBySurveyId(surveyId);
        int totalResponses = results.size();

        Map<String, Integer> optionCounts = new HashMap<>();
        Double averageRating = null;

        switch (survey.getType()) {
            case RADIO:
            case CHECKBOX:
                for (SurveyResult result : results) {
                    try {
                        //checkbox는 Json array일 수 있음
                        ObjectMapper mapper = new ObjectMapper();
                        List<String> answers = result.getAnswerText().startsWith("[") ? mapper.readValue(result.getAnswerText(), new TypeReference<List<String>>() {})
                                                                                        : List.of(result.getAnswerText());
                        for (String answer : answers) {
                            optionCounts.put(answer, optionCounts.getOrDefault(answer, 0) + 1);
                        }
                    } catch (Exception e) {
                        //로깅 처리
                    }
                }
                break;
            case RATING:
                double sum = 0;
                for (SurveyResult result : results) {
                    try {
                        sum += Double.parseDouble(result.getAnswerText());
                    } catch (NumberFormatException e) {
                        // skip
                    }
                }
                averageRating = results.isEmpty() ? null : sum / totalResponses;
                break;
            case TEXTAREA:
                //통계없음
                break;
        }
        return new SurveyStatisticsResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getType().name(),
                totalResponses,
                optionCounts.isEmpty() ? null : optionCounts, averageRating
        );
    }
}
