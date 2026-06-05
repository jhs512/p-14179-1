package com.back.domain.post.controller;

import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.dto.PostForm;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.post.service.ViewCountService;
import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.service.RecommendationService;
import com.back.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final CommentService commentService;
    private final RecommendationService recommendationService;
    private final ViewCountService viewCountService;

    @GetMapping
    @Transactional(readOnly = true)
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Post> posts = postService.getList(pageable);
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/new")
    public String newForm(PostForm postForm) {
        return "post/form";
    }

    @PostMapping
    @Transactional
    public String create(@AuthenticationPrincipal SecurityUser user,
                         @Valid PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        Member author = memberService.findById(user.getMemberId());
        Post post = postService.create(author, postForm.getTitle(), postForm.getContent());
        return "redirect:/posts/" + post.getId();
    }

    @GetMapping("/{id}")
    @Transactional
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal SecurityUser user, Model model) {
        Post post = postService.getDetail(id);
        Long memberId = user != null ? user.getMemberId() : null;
        viewCountService.recordView(post, memberId);
        model.addAttribute("post", post);
        model.addAttribute("canModify", canModify(post, user));
        model.addAttribute("comments", commentService.getCommentViews(
                id, memberId, user != null && user.isAdmin()));
        model.addAttribute("recommendCount",
                recommendationService.count(RecommendTargetType.POST, id));
        model.addAttribute("recommendedByMe",
                recommendationService.recommendedByMe(memberId, RecommendTargetType.POST, id));
        return "post/detail";
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal SecurityUser user,
                           PostForm postForm, Model model) {
        Post post = postService.getDetail(id);
        requireModifiable(post, user);
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());
        model.addAttribute("post", post);
        return "post/form";
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal SecurityUser user,
                       @Valid PostForm postForm, BindingResult bindingResult, Model model) {
        Post post = postService.getDetail(id);
        requireModifiable(post, user);
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post);
            return "post/form";
        }
        postService.update(post, postForm.getTitle(), postForm.getContent());
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal SecurityUser user) {
        Post post = postService.getDetail(id);
        requireModifiable(post, user);
        postService.softDelete(post);
        return "redirect:/posts";
    }

    private boolean canModify(Post post, SecurityUser user) {
        return user != null && (post.isAuthor(user.getMemberId()) || user.isAdmin());
    }

    private void requireModifiable(Post post, SecurityUser user) {
        if (!canModify(post, user)) {
            throw new AccessDeniedException("글을 수정·삭제할 권한이 없습니다.");
        }
    }
}
