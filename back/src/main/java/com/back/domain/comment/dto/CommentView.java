package com.back.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 글 상세에 표시할 댓글 뷰. 삭제된 댓글은 deleted=true로 표시되며 content는 숨긴다(툼스톤).
 */
public record CommentView(
        Long id,
        String authorNickname,
        String content,
        LocalDateTime createDate,
        boolean deleted,
        boolean canModify,
        long recommendCount,
        boolean recommendedByMe,
        List<CommentView> replies
) {
}
