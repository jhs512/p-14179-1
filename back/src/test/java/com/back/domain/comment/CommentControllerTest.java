package com.back.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.comment.dto.CommentView;
import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.global.security.SecurityUser;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member author;
    private Member other;
    private Member admin;
    private Post post;

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("delete from recommendation");
        jdbcTemplate.execute("delete from post_view_log");
        jdbcTemplate.execute("delete from comment");
        jdbcTemplate.execute("delete from post");
        jdbcTemplate.execute("delete from member");
        author = memberService.signup("author", "pass1234", "작성자");
        other = memberService.signup("other", "pass1234", "타인");
        admin = memberService.join("admin", "pass1234", "관리자", Role.ADMIN);
        post = postService.create(author, "글", "내용");
    }

    private SecurityUser as(Member m) {
        return new SecurityUser(m);
    }

    @Test
    @DisplayName("로그인 회원이 댓글을 작성하면 저장된다")
    void createComment() throws Exception {
        mvc.perform(post("/posts/{postId}/comments", post.getId()).with(csrf()).with(user(as(other)))
                        .param("content", "좋은 글이네요"))
                .andExpect(status().is3xxRedirection());
        assertThat(commentService.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("미인증 사용자의 댓글 작성은 로그인으로 리다이렉트된다")
    void createUnauthenticated() throws Exception {
        mvc.perform(post("/posts/{postId}/comments", post.getId()).with(csrf())
                        .param("content", "댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(r -> assertThat(r.getResponse().getRedirectedUrl()).contains("/member/login"));
        assertThat(commentService.count()).isZero();
    }

    @Test
    @DisplayName("빈 댓글은 저장되지 않는다")
    void blankComment() throws Exception {
        mvc.perform(post("/posts/{postId}/comments", post.getId()).with(csrf()).with(user(as(other)))
                        .param("content", "  "))
                .andExpect(status().is3xxRedirection());
        assertThat(commentService.count()).isZero();
    }

    @Test
    @DisplayName("최상위 댓글에 대댓글을 달면 부모를 참조해 저장된다")
    void createReply() throws Exception {
        Comment parent = commentService.addComment(post, other, "부모 댓글");

        mvc.perform(post("/posts/{postId}/comments", post.getId()).with(csrf()).with(user(as(author)))
                        .param("content", "답글입니다")
                        .param("parentId", parent.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(commentService.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("대댓글에 다시 대댓글을 달면 거부된다(1단계 제한)")
    void replyToReplyRejected() throws Exception {
        Comment parent = commentService.addComment(post, other, "부모");
        Comment reply = commentService.addReply(parent, author, "대댓글");

        mvc.perform(post("/posts/{postId}/comments", post.getId()).with(csrf()).with(user(as(other)))
                        .param("content", "대대댓글")
                        .param("parentId", reply.getId().toString()))
                .andExpect(status().isBadRequest());

        assertThat(commentService.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("작성자 본인은 댓글을 수정할 수 있다")
    void editByAuthor() throws Exception {
        Comment comment = commentService.addComment(post, other, "원댓글");
        mvc.perform(post("/comments/{id}/edit", comment.getId()).with(csrf()).with(user(as(other)))
                        .param("content", "수정댓글"))
                .andExpect(status().is3xxRedirection());
        assertThat(commentService.getComment(comment.getId()).getContent()).isEqualTo("수정댓글");
    }

    @Test
    @DisplayName("작성자도 관리자도 아닌 회원의 댓글 수정은 거부된다(403)")
    void editByOtherForbidden() throws Exception {
        Comment comment = commentService.addComment(post, other, "원댓글");
        mvc.perform(post("/comments/{id}/edit", comment.getId()).with(csrf()).with(user(as(author)))
                        .param("content", "침범"))
                .andExpect(status().isForbidden());
        assertThat(commentService.getComment(comment.getId()).getContent()).isEqualTo("원댓글");
    }

    @Test
    @DisplayName("관리자는 임의의 댓글을 삭제할 수 있다")
    void deleteByAdmin() throws Exception {
        Comment comment = commentService.addComment(post, other, "댓글");
        mvc.perform(post("/comments/{id}/delete", comment.getId()).with(csrf()).with(user(as(admin))))
                .andExpect(status().is3xxRedirection());
        assertThat(commentService.getComment(comment.getId()).isDeleted()).isTrue();
    }

    @Test
    @DisplayName("대댓글이 있는 댓글을 삭제하면 툼스톤으로 남고 대댓글은 유지된다")
    @Transactional
    void softDeleteKeepsReplies() {
        Comment parent = commentService.addComment(post, other, "부모 댓글");
        commentService.addReply(parent, author, "대댓글");

        commentService.softDelete(parent);

        List<CommentView> views = commentService.getCommentViews(post.getId(), null, false);
        assertThat(views).hasSize(1);
        CommentView tombstone = views.get(0);
        assertThat(tombstone.deleted()).isTrue();
        assertThat(tombstone.content()).isNull();
        assertThat(tombstone.replies()).hasSize(1);
        assertThat(tombstone.replies().get(0).content()).isEqualTo("대댓글");
    }

    @Test
    @DisplayName("대댓글이 없는 댓글을 삭제하면 목록에서 완전히 사라진다")
    @Transactional
    void softDeleteWithoutRepliesDisappears() {
        Comment comment = commentService.addComment(post, other, "혼자 댓글");
        commentService.softDelete(comment);

        List<CommentView> views = commentService.getCommentViews(post.getId(), null, false);
        assertThat(views).isEmpty();
    }
}
