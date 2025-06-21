package com.api.demo.service;

import com.api.demo.dto.*;
import com.api.demo.entity.*;
import com.api.demo.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        //이메일로 중복확인
        Optional<Member> optionalMember = memberRepository.findByEmail(memberDto.getEmail());

        Member member = optionalMember.orElseGet(() -> {
            Member newMember = new Member();
            newMember.setName(memberDto.getName());
            newMember.setEmail(memberDto.getEmail());
            newMember.setGender(memberDto.getGender());
            newMember.setAgeRange(memberDto.getAgeRange());
            newMember.setCreatedAt(LocalDateTime.now());
            newMember.setUpdatedAt(LocalDateTime.now());
            return memberRepository.save(newMember); // 없을 경우만 저장
        });

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

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 없습니다."));
    }

}
