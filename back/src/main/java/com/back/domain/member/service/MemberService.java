package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member signup(String username, String password, String nickname) {
        return join(username, password, nickname, Role.USER);
    }

    public Member join(String username, String password, String nickname, Role role) {
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(role)
                .build();

        return memberRepository.save(member);
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public Optional<Member> findByUsernameOptional(String username) {
        return memberRepository.findByUsername(username);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public void updateNickname(Member member, String nickname) {
        member.updateNickname(nickname);
    }

    public void updatePassword(Member member, String rawPassword) {
        member.updatePassword(passwordEncoder.encode(rawPassword));
    }

    public long count() {
        return memberRepository.count();
    }
}
