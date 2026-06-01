package com.back.domain.recommendation.controller;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.service.RecommendationService;
import com.back.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;

    @PostMapping("/posts/{id}/recommend")
    @Transactional
    public String recommendPost(@PathVariable Long id,
                                @AuthenticationPrincipal SecurityUser user,
                                RedirectAttributes ra) {
        Post post = postService.getDetail(id);
        Member member = memberService.findById(user.getMemberId());
        try {
            recommendationService.toggle(member, RecommendTargetType.POST, post.getId(), post.getAuthor().getId());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("recommendError", e.getMessage());
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/comments/{id}/recommend")
    @Transactional
    public String recommendComment(@PathVariable Long id,
                                   @AuthenticationPrincipal SecurityUser user,
                                   RedirectAttributes ra) {
        Comment comment = commentService.getComment(id);
        if (comment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 댓글은 추천할 수 없습니다.");
        }
        Member member = memberService.findById(user.getMemberId());
        Long postId = comment.getPost().getId();
        try {
            recommendationService.toggle(member, RecommendTargetType.COMMENT, comment.getId(), comment.getAuthor().getId());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("recommendError", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }
}
