package com.back.domain.comment.service;

import com.back.domain.comment.dto.CommentView;
import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.repository.CommentRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.service.RecommendationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecommendationService recommendationService;

    public Comment addComment(Post post, Member author, String content) {
        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    public Comment addReply(Comment parent, Member author, String content) {
        if (parent.isReply()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다.");
        }
        Comment reply = Comment.builder()
                .post(parent.getPost())
                .author(author)
                .content(content)
                .parent(parent)
                .build();
        return commentRepository.save(reply);
    }

    public Comment getComment(Long id) {
        return commentRepository.findByIdWithAuthorAndPost(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."));
    }

    public void update(Comment comment, String content) {
        comment.update(content);
    }

    public void softDelete(Comment comment) {
        comment.softDelete();
    }

    public long count() {
        return commentRepository.count();
    }

    /**
     * 글 상세에 표시할 댓글 뷰 트리를 조립한다.
     * - 대댓글이 삭제되면 표시에서 제외한다.
     * - 최상위 댓글이 삭제된 경우: 살아있는 대댓글이 있으면 툼스톤으로 남기고, 없으면 제외한다.
     */
    public List<CommentView> getCommentViews(Long postId, Long currentMemberId, boolean admin) {
        List<Comment> all = commentRepository.findByPostIdWithAuthor(postId);

        List<Long> ids = all.stream().map(Comment::getId).toList();
        Map<Long, Long> recommendCounts = recommendationService.countsByTargets(RecommendTargetType.COMMENT, ids);
        Set<Long> myRecommends = recommendationService.recommendedTargetIds(currentMemberId, RecommendTargetType.COMMENT, ids);

        Map<Long, List<Comment>> repliesByParent = all.stream()
                .filter(Comment::isReply)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<CommentView> result = new ArrayList<>();
        for (Comment c : all) {
            if (c.isReply()) {
                continue;
            }
            List<CommentView> replyViews = repliesByParent.getOrDefault(c.getId(), List.of()).stream()
                    .filter(r -> !r.isDeleted())
                    .map(r -> toView(r, currentMemberId, admin, recommendCounts, myRecommends, List.of()))
                    .toList();

            if (c.isDeleted() && replyViews.isEmpty()) {
                continue;
            }
            result.add(toView(c, currentMemberId, admin, recommendCounts, myRecommends, replyViews));
        }
        return result;
    }

    private CommentView toView(Comment c, Long currentMemberId, boolean admin,
                               Map<Long, Long> recommendCounts, Set<Long> myRecommends, List<CommentView> replies) {
        boolean canModify = !c.isDeleted()
                && currentMemberId != null
                && (c.isAuthor(currentMemberId) || admin);
        String content = c.isDeleted() ? null : c.getContent();
        return new CommentView(
                c.getId(),
                c.getAuthor().getNickname(),
                content,
                c.getCreateDate(),
                c.isDeleted(),
                canModify,
                recommendCounts.getOrDefault(c.getId(), 0L),
                myRecommends.contains(c.getId()),
                replies
        );
    }
}
