package com.back.domain.post.entity;

import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원·글 단위 마지막 조회 시각. 24시간 유니크 조회수 판단에 사용한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_post_view_log_member_post",
        columnNames = {"member_id", "post_id"}))
public class PostViewLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime lastViewedAt;

    @Builder
    private PostViewLog(Member member, Post post, LocalDateTime lastViewedAt) {
        this.member = member;
        this.post = post;
        this.lastViewedAt = lastViewedAt;
    }

    public void touch(LocalDateTime viewedAt) {
        this.lastViewedAt = viewedAt;
    }
}
