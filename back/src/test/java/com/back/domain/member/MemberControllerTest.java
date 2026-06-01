package com.back.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.MemberService;
import com.back.global.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("delete from recommendation");
        jdbcTemplate.execute("delete from post_view_log");
        jdbcTemplate.execute("delete from comment");
        jdbcTemplate.execute("delete from post");
        jdbcTemplate.execute("delete from member");
    }

    @Test
    @DisplayName("회원가입 폼은 200을 반환한다")
    void signupForm() throws Exception {
        mvc.perform(get("/member/signup")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("유효한 입력으로 가입하면 회원이 저장되고 비밀번호는 해시로 저장된다")
    void signupSuccess() throws Exception {
        mvc.perform(post("/member/signup").with(csrf())
                        .param("username", "user1")
                        .param("password", "pass1234")
                        .param("nickname", "유저1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));

        Member saved = memberRepository.findByUsername("user1").orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("유저1");
        assertThat(saved.getPassword()).isNotEqualTo("pass1234");
        assertThat(passwordEncoder.matches("pass1234", saved.getPassword())).isTrue();
        assertThat(saved.getRole().name()).isEqualTo("USER");
    }

    @Test
    @DisplayName("중복 아이디로 가입하면 거부되고 추가 저장되지 않는다")
    void signupDuplicate() throws Exception {
        memberService.signup("user1", "pass1234", "유저1");

        mvc.perform(post("/member/signup").with(csrf())
                        .param("username", "user1")
                        .param("password", "other1234")
                        .param("nickname", "다른닉"))
                .andExpect(status().isOk()); // 폼 재렌더링

        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 값으로 가입하면 검증 실패로 거부된다")
    void signupValidationFails() throws Exception {
        mvc.perform(post("/member/signup").with(csrf())
                        .param("username", "")
                        .param("password", "")
                        .param("nickname", ""))
                .andExpect(status().isOk());

        assertThat(memberRepository.count()).isZero();
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인하면 인증된다")
    void loginSuccess() throws Exception {
        memberService.signup("user1", "pass1234", "유저1");

        mvc.perform(formLogin("/member/login").user("user1").password("pass1234"))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인하면 실패한다")
    void loginFailure() throws Exception {
        memberService.signup("user1", "pass1234", "유저1");

        mvc.perform(formLogin("/member/login").user("user1").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrlPattern("/member/login?error*"));
    }

    @Test
    @DisplayName("로그아웃하면 인증이 해제된다")
    void logoutTest() throws Exception {
        mvc.perform(logout("/member/logout"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("미인증 사용자가 내 정보에 접근하면 로그인으로 리다이렉트된다")
    void myInfoUnauthenticated() throws Exception {
        mvc.perform(get("/member/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).contains("/member/login"));
    }

    @Test
    @DisplayName("로그인 회원은 닉네임을 변경할 수 있다")
    void updateNickname() throws Exception {
        Member member = memberService.signup("user1", "pass1234", "유저1");

        mvc.perform(post("/member/me").with(csrf()).with(user(new SecurityUser(member)))
                        .param("nickname", "새닉네임")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/me"));

        assertThat(memberRepository.findByUsername("user1").orElseThrow().getNickname())
                .isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("로그인 회원은 비밀번호를 변경할 수 있고 새 비밀번호로 로그인된다")
    void updatePassword() throws Exception {
        Member member = memberService.signup("user1", "pass1234", "유저1");

        mvc.perform(post("/member/me").with(csrf()).with(user(new SecurityUser(member)))
                        .param("nickname", "유저1")
                        .param("password", "newpass1234"))
                .andExpect(status().is3xxRedirection());

        Member updated = memberRepository.findByUsername("user1").orElseThrow();
        assertThat(passwordEncoder.matches("newpass1234", updated.getPassword())).isTrue();
    }
}
