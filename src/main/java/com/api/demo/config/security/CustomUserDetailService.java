package com.api.demo.config.security;

import com.api.demo.model.entity.Member;
import com.api.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> memberOptional = memberService.getMemberByEmail(username);
        if (memberOptional.isEmpty()) {
            log.error("아이디 {}: 에 해당하는 사용자를 찾을 수 없습니다.", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return memberOptional.get();
    }

}