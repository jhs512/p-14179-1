package com.back.domain.comment.controller;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final MemberService memberService;

    @PostMapping("/posts/{postId}/comments")
    @Transactional
    public String create(@PathVariable Long postId,
                         @AuthenticationPrincipal SecurityUser user,
                         @RequestParam String content,
                         @RequestParam(required = false) Long parentId,
                         RedirectAttributes ra) {
        Post post = postService.getDetail(postId);

        if (content == null || content.isBlank()) {
            ra.addFlashAttribute("commentError", "댓글 내용을 입력해주세요.");
            return "redirect:/posts/" + postId;
        }

        Member author = memberService.findById(user.getMemberId());
        if (parentId != null) {
            Comment parent = commentService.getComment(parentId);
            commentService.addReply(parent, author, content);
        } else {
            commentService.addComment(post, author, content);
        }
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/comments/{id}/edit")
    @Transactional
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal SecurityUser user,
                       @RequestParam String content,
                       RedirectAttributes ra) {
        Comment comment = commentService.getComment(id);
        requireModifiable(comment, user);

        if (content == null || content.isBlank()) {
            ra.addFlashAttribute("commentError", "댓글 내용을 입력해주세요.");
        } else {
            commentService.update(comment, content);
        }
        return "redirect:/posts/" + comment.getPost().getId();
    }

    @PostMapping("/comments/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal SecurityUser user) {
        Comment comment = commentService.getComment(id);
        requireModifiable(comment, user);
        Long postId = comment.getPost().getId();
        commentService.softDelete(comment);
        return "redirect:/posts/" + postId;
    }

    private void requireModifiable(Comment comment, SecurityUser user) {
        if (comment.isDeleted()
                || user == null
                || !(comment.isAuthor(user.getMemberId()) || user.isAdmin())) {
            throw new AccessDeniedException("댓글을 수정·삭제할 권한이 없습니다.");
        }
    }
}
