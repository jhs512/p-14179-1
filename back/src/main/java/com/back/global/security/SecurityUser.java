package com.back.global.security;

import com.back.domain.member.entity.Member;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class SecurityUser extends User {

    private final Long memberId;
    private final String nickname;

    public SecurityUser(Member member) {
        super(member.getUsername(), member.getPassword(), authorities(member));
        this.memberId = member.getId();
        this.nickname = member.getNickname();
    }

    private static Collection<? extends GrantedAuthority> authorities(Member member) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
