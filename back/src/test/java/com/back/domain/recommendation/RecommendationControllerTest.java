package com.back.domain.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.service.RecommendationService;
import com.back.global.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member author;
    private Member reader;
    private Post post;

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

    private SecurityUser as(Member m) {
        return new SecurityUser(m);
    }

    private long postCount() {
        return recommendationService.count(RecommendTargetType.POST, post.getId());
    }

    @Test
    @DisplayName("글을 추천하면 추천수가 1 증가한다")
    void recommendPost() throws Exception {
        mvc.perform(post("/posts/{id}/recommend", post.getId()).with(csrf()).with(user(as(reader))))
                .andExpect(status().is3xxRedirection());
        assertThat(postCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 글을 다시 추천하면 취소되어 추천수가 0이 된다(토글)")
    void togglePost() throws Exception {
        mvc.perform(post("/posts/{id}/recommend", post.getId()).with(csrf()).with(user(as(reader))));
        mvc.perform(post("/posts/{id}/recommend", post.getId()).with(csrf()).with(user(as(reader))));
        assertThat(postCount()).isZero();
    }

    @Test
    @DisplayName("본인이 작성한 글은 추천할 수 없다")
    void cannotRecommendOwnPost() throws Exception {
        mvc.perform(post("/posts/{id}/recommend", post.getId()).with(csrf()).with(user(as(author))))
                .andExpect(status().is3xxRedirection());
        assertThat(postCount()).isZero();
    }

    @Test
    @DisplayName("미인증 사용자의 추천은 로그인으로 리다이렉트된다")
    void recommendUnauthenticated() throws Exception {
        mvc.perform(post("/posts/{id}/recommend", post.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(r -> assertThat(r.getResponse().getRedirectedUrl()).contains("/member/login"));
        assertThat(postCount()).isZero();
    }

    @Test
    @DisplayName("댓글을 추천하면 추천수가 1 증가하고 토글된다")
    void recommendComment() throws Exception {
        Comment comment = commentService.addComment(post, author, "댓글");

        mvc.perform(post("/comments/{id}/recommend", comment.getId()).with(csrf()).with(user(as(reader))))
                .andExpect(status().is3xxRedirection());
        assertThat(recommendationService.count(RecommendTargetType.COMMENT, comment.getId())).isEqualTo(1);

        mvc.perform(post("/comments/{id}/recommend", comment.getId()).with(csrf()).with(user(as(reader))));
        assertThat(recommendationService.count(RecommendTargetType.COMMENT, comment.getId())).isZero();
    }

    @Test
    @DisplayName("본인이 작성한 댓글은 추천할 수 없다")
    void cannotRecommendOwnComment() throws Exception {
        Comment comment = commentService.addComment(post, reader, "내 댓글");
        mvc.perform(post("/comments/{id}/recommend", comment.getId()).with(csrf()).with(user(as(reader))))
                .andExpect(status().is3xxRedirection());
        assertThat(recommendationService.count(RecommendTargetType.COMMENT, comment.getId())).isZero();
    }
}
