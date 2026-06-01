package com.back.global.initData;

import static org.assertj.core.api.Assertions.assertThat;

import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Role;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InitDataServiceTest {

    @Autowired
    private InitDataService initDataService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MemberRepository memberRepository;
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
    @DisplayName("비어 있으면 회원 5명·글 5개·댓글 5개를 생성하고 관리자가 1명 이상이다")
    void seedsWhenEmpty() {
        initDataService.seedSampleData();

        assertThat(memberService.count()).isEqualTo(5);
        assertThat(postService.count()).isEqualTo(5);
        assertThat(commentService.count()).isEqualTo(5);
        long adminCount = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == Role.ADMIN)
                .count();
        assertThat(adminCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("회원이 이미 있으면 시드 로직이 중단되어 추가 생성되지 않는다")
    void skipsWhenNotEmpty() {
        memberService.signup("existing", "pass1234", "기존회원");

        initDataService.seedSampleData();

        assertThat(memberService.count()).isEqualTo(1);
        assertThat(postService.count()).isZero();
    }
}
