package com.api.demo.aop;


import com.api.demo.dto.SurveySubmitRequest;
import com.api.demo.model.entity.Member;
import com.api.demo.model.entity.UserActivityLog;
import com.api.demo.repository.MemberRepository;
import com.api.demo.repository.UserActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.time.LocalDateTime;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {

    private final UserActivityLogRepository activityLogRepository;
    private final MemberRepository memberRepository;

    @Around("execution(* com.api.demo.controller..*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();
        String url = request.getRequestURI();

        Object[] args = joinPoint.getArgs();

        Object result;
        int statusCode = 200;
        String responseBody = "";
        String requestBody = "";
        Long memberId = null;

        try {
            result = joinPoint.proceed(); // 먼저 서비스 로직 실행 (DB 저장)

            // DB 저장 이후 memberId 추출 및 requestBody 재구성
            for (Object arg : args) {
                if (arg instanceof SurveySubmitRequest submitRequest) {
                    String email = submitRequest.getMemberDto().getEmail();
                    if (email != null) {
                        Optional<Member> memberOptional = memberRepository.findByEmail(email);
                        if (memberOptional.isPresent()) {
                            Member saved = memberOptional.get();
                            memberId = saved.getId();
                            submitRequest.getMemberDto().setId(memberId); // id 갱신
                        }
                    }
                    // requestBody는 id 갱신 후 직렬화
                    requestBody = new ObjectMapper().writeValueAsString(submitRequest);
                    break;
                }
            }

            if (result instanceof ResponseEntity<?> response) {
                statusCode = response.getStatusCode().value();
                responseBody = new ObjectMapper().writeValueAsString(response.getBody());
            }

        } catch (Exception e) {
            statusCode = 500;
            responseBody = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;

            UserActivityLog logEntry = new UserActivityLog();
            logEntry.setMemberId(memberId);
            logEntry.setHttpMethod(method);
            logEntry.setRequestUrl(url);
            logEntry.setRequestBody(requestBody);
            logEntry.setResponseCode(statusCode);
            logEntry.setResponseBody(responseBody);
            logEntry.setExecutionTimeMs(duration);
            logEntry.setCreatedAt(LocalDateTime.now());

            activityLogRepository.save(logEntry);
        }

        return result;
    }



    private String getRequestBody(Object[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            for (Object arg : args) {
                if (!(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse)) {
                    return mapper.writeValueAsString(arg);
                }
            }
        } catch (Exception e) {
            return "body parse error";
        }
        return "";
    }

    private Long getCurrentMemberId(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg instanceof SurveySubmitRequest request) {
                    if (request.getMemberDto() != null) {
                        return request.getMemberDto().getId();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
