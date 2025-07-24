package com.api.demo.dto;

import lombok.Data;

@Data
public class MemberDto {

    private Long id;

    private String email;

    private String gender;

    private String name;

    private String password;

    private String ageRange;

}
