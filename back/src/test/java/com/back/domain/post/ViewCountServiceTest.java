package com.back.domain.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.post.service.ViewCountService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewCountServiceTest {

    @Autowired
    private ViewCountService viewCountService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member author;
    private Member reader;
    private Post post;
    private final LocalDateTime base = LocalDateTime.of(2026, 1, 1, 12, 0);

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("delete from recommendation");
        jdbcTemplate.execute("delete from post_view_log");
        jdbcTemplate.execute("delete from comment");
        jdbcTemplate.execute("delete from post");
        jdbcTemplate.execute("delete from member");
        author = memberService.signup("author", "pass1234", "작성자");
        reader = memberService.signup("reader", "pass1234", "독자");
        post = postService.create(author, "글", "내용");
    }

    @Test
    @DisplayName("회원의 첫 열람은 조회수를 1 증가시킨다")
    void firstViewIncrements() {
        viewCountService.recordView(post, reader.getId(), base);
        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 회원이 24시간 내 다시 열람하면 증가하지 않는다")
    void withinWindowNoIncrement() {
        viewCountService.recordView(post, reader.getId(), base);
        viewCountService.recordView(post, reader.getId(), base.plusHours(23));
        assertThat(post.getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 회원이 24시간 이후 다시 열람하면 증가한다")
    void afterWindowIncrements() {
        viewCountService.recordView(post, reader.getId(), base);
        viewCountService.recordView(post, reader.getId(), base.plusHours(25));
        assertThat(post.getViewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("비로그인 열람은 조회수를 증가시키지 않는다")
    void anonymousNoIncrement() {
        viewCountService.recordView(post, null, base);
        assertThat(post.getViewCount()).isZero();
    }
}
