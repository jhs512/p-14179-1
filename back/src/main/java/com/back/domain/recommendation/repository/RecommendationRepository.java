package com.back.domain.recommendation.repository;

import com.back.domain.recommendation.entity.RecommendTargetType;
import com.back.domain.recommendation.entity.Recommendation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    Optional<Recommendation> findByMemberIdAndTargetTypeAndTargetId(
            Long memberId, RecommendTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(RecommendTargetType targetType, Long targetId);

    boolean existsByMemberIdAndTargetTypeAndTargetId(
            Long memberId, RecommendTargetType targetType, Long targetId);

    @Query("select r.targetId, count(r) from Recommendation r "
            + "where r.targetType = :type and r.targetId in :ids group by r.targetId")
    List<Object[]> countByTargetTypeAndTargetIdIn(
            @Param("type") RecommendTargetType type, @Param("ids") List<Long> ids);

    @Query("select r.targetId from Recommendation r "
            + "where r.member.id = :memberId and r.targetType = :type and r.targetId in :ids")
    List<Long> findTargetIdsByMemberAndTargetTypeAndTargetIdIn(
            @Param("memberId") Long memberId, @Param("type") RecommendTargetType type, @Param("ids") List<Long> ids);
}
