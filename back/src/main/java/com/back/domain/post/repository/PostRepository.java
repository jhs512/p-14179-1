package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p join fetch p.author",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("select p from Post p join fetch p.author where p.id = :id")
    Optional<Post> findByIdWithAuthor(Long id);
}
