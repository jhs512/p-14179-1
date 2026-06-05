package com.back.domain.comment.repository;

import com.back.domain.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제된 댓글도 포함해 조회한다 (대댓글 유지 표시를 위해 툼스톤 처리)
    @Query("select c from Comment c join fetch c.author where c.post.id = :postId order by c.id asc")
    List<Comment> findByPostIdWithAuthor(Long postId);

    @Query("select c from Comment c join fetch c.author join fetch c.post where c.id = :id")
    Optional<Comment> findByIdWithAuthorAndPost(Long id);
}
