package com.back.domain.recommendation.service;

import com.back.domain.member.entity.Member;
import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.entity.Recommendation;
import com.back.domain.recommendation.repository.RecommendationRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    /**
     * 추천 토글. 본인이 작성한 대상은 추천할 수 없다.
     *
     * @return 토글 후 추천 상태(true=추천됨)
     */
    public boolean toggle(Member member, RecommendTargetType type, Long targetId, Long targetAuthorId) {
        if (member.getId().equals(targetAuthorId)) {
            throw new IllegalStateException("본인이 작성한 글·댓글은 추천할 수 없습니다.");
        }

        return recommendationRepository
                .findByMemberIdAndTargetTypeAndTargetId(member.getId(), type, targetId)
                .map(existing -> {
                    recommendationRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    recommendationRepository.save(Recommendation.builder()
                            .member(member)
                            .targetType(type)
                            .targetId(targetId)
                            .build());
                    return true;
                });
    }

    public long count(RecommendTargetType type, Long targetId) {
        return recommendationRepository.countByTargetTypeAndTargetId(type, targetId);
    }

    public boolean recommendedByMe(Long memberId, RecommendTargetType type, Long targetId) {
        if (memberId == null) {
            return false;
        }
        return recommendationRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    public Map<Long, Long> countsByTargets(RecommendTargetType type, List<Long> targetIds) {
        Map<Long, Long> result = new HashMap<>();
        if (targetIds.isEmpty()) {
            return result;
        }
        for (Object[] row : recommendationRepository.countByTargetTypeAndTargetIdIn(type, targetIds)) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    public Set<Long> recommendedTargetIds(Long memberId, RecommendTargetType type, List<Long> targetIds) {
        if (memberId == null || targetIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(
                recommendationRepository.findTargetIdsByMemberAndTargetTypeAndTargetIdIn(memberId, type, targetIds));
    }
}
