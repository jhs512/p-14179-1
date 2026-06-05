package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.post.entity.Post;
import com.back.domain.post.entity.PostViewLog;
import com.back.domain.post.repository.PostViewLogRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    private static final Duration WINDOW = Duration.ofHours(24);

    private final PostViewLogRepository postViewLogRepository;
    private final MemberRepository memberRepository;

    public void recordView(Post post, Long memberId) {
        recordView(post, memberId, LocalDateTime.now());
    }

    /**
     * 글 열람을 기록하고 24시간 유니크 규칙에 따라 조회수를 증가시킨다.
     * 비로그인(memberId == null)은 카운트하지 않는다.
     */
    public void recordView(Post post, Long memberId, LocalDateTime now) {
        if (memberId == null) {
            return;
        }

        PostViewLog log = postViewLogRepository.findByMemberIdAndPostId(memberId, post.getId()).orElse(null);

        if (log == null) {
            Member member = memberRepository.getReferenceById(memberId);
            postViewLogRepository.save(PostViewLog.builder()
                    .member(member)
                    .post(post)
                    .lastViewedAt(now)
                    .build());
            post.increaseViewCount();
            return;
        }

        if (!log.getLastViewedAt().isAfter(now.minus(WINDOW))) {
            log.touch(now);
            post.increaseViewCount();
        }
    }
}
