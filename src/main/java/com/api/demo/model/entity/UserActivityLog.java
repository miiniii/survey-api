package com.api.demo.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_activity_log")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String httpMethod;

    private String requestUrl;

    @Lob
    private String requestBody;

    private int responseCode; //결과 코드

    @Lob
    private String responseBody; //결과 내용

    private Long executionTimeMs; //실행시간

    private LocalDateTime createdAt;
}
