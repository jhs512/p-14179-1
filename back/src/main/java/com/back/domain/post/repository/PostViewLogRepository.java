package com.back.domain.post.repository;

import com.back.domain.post.entity.PostViewLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewLogRepository extends JpaRepository<PostViewLog, Long> {

    Optional<PostViewLog> findByMemberIdAndPostId(Long memberId, Long postId);
}
