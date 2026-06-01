package com.back.global.initData;

import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.Role;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitDataService {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 회원이 한 명이라도 있으면 아무것도 하지 않는다.
     * 비어 있으면 회원 5명(관리자 1명 포함)·글 5개·댓글 5개를 생성한다.
     */
    @Transactional
    public void seedSampleData() {
        if (memberService.count() > 0) {
            return;
        }

        List<Member> members = new ArrayList<>();
        members.add(memberService.join("admin", "admin1234", "관리자", Role.ADMIN));
        for (int i = 1; i <= 4; i++) {
            members.add(memberService.signup("user" + i, "user1234", "유저" + i));
        }

        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Member author = members.get(i % members.size());
            posts.add(postService.create(author, "샘플 글 " + i, "샘플 글 " + i + "의 내용입니다."));
        }

        for (int i = 1; i <= 5; i++) {
            Member author = members.get((i + 2) % members.size());
            Post post = posts.get(i % posts.size());
            commentService.addComment(post, author, "샘플 댓글 " + i);
        }
    }
}
