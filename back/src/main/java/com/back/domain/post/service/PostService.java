package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post create(Member author, String title, String content) {
        Post post = Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .build();
        return postRepository.save(post);
    }

    public Page<Post> getList(Pageable pageable) {
        return postRepository.findAllWithAuthor(pageable);
    }

    public Post getDetail(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 글입니다."));
    }

    public void update(Post post, String title, String content) {
        post.update(title, content);
    }

    public void softDelete(Post post) {
        post.softDelete();
    }

    public long count() {
        return postRepository.count();
    }
}
