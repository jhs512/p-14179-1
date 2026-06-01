package com.back.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.back.domain.post.service.PostService;
import com.back.global.security.SecurityUser;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member author;
    private Member other;
    private Member admin;

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
    }

    private SecurityUser as(Member m) {
        return new SecurityUser(m);
    }

    @Test
    @DisplayName("글 목록은 200을 반환하고 최신순으로 정렬된다")
    void listLatestFirst() throws Exception {
        Post p1 = postService.create(author, "첫번째", "내용1");
        Post p2 = postService.create(author, "두번째", "내용2");
        Post p3 = postService.create(author, "세번째", "내용3");

        var result = mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Page<Post> posts = (Page<Post>) result.getModelAndView().getModel().get("posts");
        List<Long> ids = posts.getContent().stream().map(Post::getId).toList();
        assertThat(ids).containsExactly(p3.getId(), p2.getId(), p1.getId());
    }

    @Test
    @DisplayName("글 목록은 페이지당 10개로 페이징된다")
    void listPaging() throws Exception {
        for (int i = 0; i < 11; i++) {
            postService.create(author, "글" + i, "내용" + i);
        }

        var result = mvc.perform(get("/posts")).andExpect(status().isOk()).andReturn();
        @SuppressWarnings("unchecked")
        Page<Post> posts = (Page<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts.getContent()).hasSize(10);
        assertThat(posts.getTotalElements()).isEqualTo(11);
        assertThat(posts.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("글 상세는 200을 반환한다")
    void detail() throws Exception {
        Post post = postService.create(author, "제목", "내용");
        mvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("post"));
    }

    @Test
    @DisplayName("로그인 회원이 글 상세를 열람하면 조회수가 증가한다")
    void detailIncrementsViewCountForMember() throws Exception {
        Post post = postService.create(author, "제목", "내용");
        mvc.perform(get("/posts/{id}", post.getId()).with(user(as(other))))
                .andExpect(status().isOk());
        assertThat(postService.getDetail(post.getId()).getViewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("비로그인 열람은 조회수를 증가시키지 않는다")
    void detailDoesNotIncrementForAnonymous() throws Exception {
        Post post = postService.create(author, "제목", "내용");
        mvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk());
        assertThat(postService.getDetail(post.getId()).getViewCount()).isZero();
    }

    @Test
    @DisplayName("로그인 회원이 글을 작성하면 저장되고 상세로 리다이렉트된다")
    void createSuccess() throws Exception {
        mvc.perform(post("/posts").with(csrf()).with(user(as(author)))
                        .param("title", "새 글")
                        .param("content", "새 내용"))
                .andExpect(status().is3xxRedirection());

        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("미인증 사용자의 글 작성은 로그인으로 리다이렉트된다")
    void createUnauthenticated() throws Exception {
        mvc.perform(post("/posts").with(csrf())
                        .param("title", "새 글")
                        .param("content", "새 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).contains("/member/login"));

        assertThat(postRepository.count()).isZero();
    }

    @Test
    @DisplayName("빈 제목/내용으로 작성하면 검증 실패로 거부된다")
    void createValidationFails() throws Exception {
        mvc.perform(post("/posts").with(csrf()).with(user(as(author)))
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk());
        assertThat(postRepository.count()).isZero();
    }

    @Test
    @DisplayName("작성자 본인은 글을 수정할 수 있다")
    void editByAuthor() throws Exception {
        Post post = postService.create(author, "원제목", "원내용");
        mvc.perform(post("/posts/{id}/edit", post.getId()).with(csrf()).with(user(as(author)))
                        .param("title", "수정제목")
                        .param("content", "수정내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        assertThat(postService.getDetail(post.getId()).getTitle()).isEqualTo("수정제목");
    }

    @Test
    @DisplayName("관리자는 타인의 글을 수정할 수 있다")
    void editByAdmin() throws Exception {
        Post post = postService.create(author, "원제목", "원내용");
        mvc.perform(post("/posts/{id}/edit", post.getId()).with(csrf()).with(user(as(admin)))
                        .param("title", "관리자수정")
                        .param("content", "내용"))
                .andExpect(status().is3xxRedirection());
        assertThat(postService.getDetail(post.getId()).getTitle()).isEqualTo("관리자수정");
    }

    @Test
    @DisplayName("작성자도 관리자도 아닌 회원의 수정은 거부된다(403)")
    void editByOtherForbidden() throws Exception {
        Post post = postService.create(author, "원제목", "원내용");
        mvc.perform(post("/posts/{id}/edit", post.getId()).with(csrf()).with(user(as(other)))
                        .param("title", "침범")
                        .param("content", "내용"))
                .andExpect(status().isForbidden());
        assertThat(postService.getDetail(post.getId()).getTitle()).isEqualTo("원제목");
    }

    @Test
    @DisplayName("글을 삭제하면 소프트 삭제되어 목록·상세에서 사라지되 행은 남는다")
    void softDelete() throws Exception {
        Post post = postService.create(author, "삭제될글", "내용");
        Long id = post.getId();

        mvc.perform(post("/posts/{id}/delete", id).with(csrf()).with(user(as(author))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // 상세 접근 시 404
        mvc.perform(get("/posts/{id}", id)).andExpect(status().isNotFound());
        // 목록에서 제외
        assertThat(postRepository.count()).isZero();
        // 행은 물리적으로 남아있음 (소프트 삭제)
        Integer rawCount = jdbcTemplate.queryForObject("select count(*) from post", Integer.class);
        assertThat(rawCount).isEqualTo(1);
    }
}
