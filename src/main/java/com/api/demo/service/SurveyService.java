package com.api.demo.service;

import com.api.demo.dto.*;
import com.api.demo.exception.BadRequestRuntimeException;
import com.api.demo.model.entity.*;
import com.api.demo.repository.CompanyRepository;
import com.api.demo.repository.MemberRepository;
import com.api.demo.repository.SurveyRepository;
import com.api.demo.repository.SurveyResultRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final SurveyResultRepository surveyResultRepository;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    public SurveyResponseMain getSurveyByCompanyId(Long companyId) {

        String companyName = companyRepository.findById(companyId)
                .map(Company::getName)
                .orElse("알 수 없는 회사");

        final List<SurveyDto> surveyList = surveyRepository.findSurveyWith(companyId);
        SurveyResponseMain data = new SurveyResponseMain();
        data.setCompany(companyName);

        if (surveyList.isEmpty()) {
            data.setCategories(Collections.emptyList());
            return data;
            //throw new NoSuchElementException("해당 회사 ID(" + companyId + ")에 대한 설문이 존재하지 않습니다.");
        }

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
            if (survey.getOptions() != null && !survey.getOptions().isBlank()) {
                try {
                    // JSON 배열 형태일 경우 처리: ["A", "B", "C"]
                    if (survey.getOptions().trim().startsWith("[")) {
                        List<String> parsed = objectMapper.readValue(survey.getOptions(), new TypeReference<List<String>>() {});
                        question.setOptions(parsed);
                    } else {
                        // 그냥 콤마 구분된 일반 텍스트일 경우
                        question.setOptions(Arrays.stream(survey.getOptions().split(","))
                                .map(String::trim)
                                .collect(Collectors.toList()));
                    }
                } catch (Exception e) {
                    log.warn("옵션 파싱 오류, 원본 옵션: {}", survey.getOptions(), e);
                    question.setOptions(List.of(survey.getOptions()));
                }
            }

            category.getQuestions().add(question);
        }

        data.setCategories(new ArrayList<>(categoryMap.values()));
        return data;

    }

    @Transactional
    public List<SurveyResult> saveSurveyResult(SurveySubmitRequest request) {

//        // 파트1: 유저 생성부
//        Optional<Member> memberOptional = memberRepository.findByEmail(request.getMemberDto().getEmail());
//        if (surveyResultRepository.existsByMemberEmail(request.getMemberDto().getEmail())) {
//            log.warn("이미 설문 참여한 사용자입니다: {}", request.getMemberDto().getEmail());
//            throw new BadRequestRuntimeException("이미 설문을 완료하여 중복 설문이 불가합니다.");
//        }
//        final Member newMember = Member.convert(request.getMemberDto());
//        final Member savedMember = memberService.save(newMember);
//        log.info("{} 유저 저장 완료", savedMember.getEmail());

        // 로그인한 사용자 정보 사용
        Member member = memberRepository.findByEmail(request.getMemberDto().getEmail())
                .orElseThrow(() -> new BadRequestRuntimeException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 중복 설문 여부 확인
        if (surveyResultRepository.existsByMemberEmail(member.getEmail())) {
            log.warn("이미 설문 참여한 사용자입니다: {}", member.getEmail());
            throw new BadRequestRuntimeException("이미 설문을 완료하여 중복 설문이 불가합니다.");
        }

        log.info("{} 유저 확인 완료, 설문 결과 저장 시작", member.getEmail());



        // 파트2: 설문결과 저장부
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
                log.error("{} 의 해당 surveyId: {} 설문이 존재하지 않습니다.",
                        member.getEmail(), answer.getSurveyId());
                throw new BadRequestRuntimeException("해당 설문이 존재하지 않습니다.");
            }
            SurveyResult result = new SurveyResult();
            result.setMember(member);
            result.setSurvey(survey);
            result.setAnswerText(answer.getAnswerText());
            results.add(result);
        }

        log.info("{} 유저 저장 완료, {} 개 설문 저장 완료", member.getEmail(), surveys.size());

        surveyResultRepository.saveAll(results);

        return results;
    }

    public SurveyStatisticsResponse getSurveyStatistics(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BadRequestRuntimeException("해당 설문이 존재하지 않습니다. id = " + surveyId));

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
                optionCounts.isEmpty() ? null : optionCounts,
                averageRating
        );
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 없습니다."));
    }

}
