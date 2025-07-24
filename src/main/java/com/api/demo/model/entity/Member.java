package com.api.demo.model.entity;


import com.api.demo.dto.MemberDto;
import com.api.demo.model.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "member")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String ageRange;

    @Column(length = 255)
    private String password;

    private String status;  // active, inactive, closed, etc.

    private LocalDateTime lastLoginAt;

    private LocalDateTime lastLogoutAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    private Role role;

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Member convert(MemberDto memberDto) {
        Member newMember = new Member();
        newMember.setName(memberDto.getName());
        newMember.setEmail(memberDto.getEmail());
        newMember.setPassword(memberDto.getPassword());
        newMember.setRole(Role.USER); // 기본값 지정
        newMember.setStatus("ACTIVE"); // 기본값 지정
        newMember.setGender(memberDto.getGender());
        newMember.setAgeRange(memberDto.getAgeRange());
        newMember.setCreatedAt(LocalDateTime.now());
        newMember.setUpdatedAt(LocalDateTime.now());
        return newMember;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

}
