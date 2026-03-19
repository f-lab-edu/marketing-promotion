package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.Promotion
import jakarta.persistence.QueryHint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface PromotionRepository : JpaRepository<Promotion, Long>, PromotionRepositoryCustom {

    /**
     * 잔여 슬롯이 있을 때만 accumulated_participation_count 를 1 증가.
     * 단일 UPDATE 로 원자적 처리 → SELECT FOR UPDATE 없이 동시성 안전.
     *
     * @return 1 = 성공, 0 = 마감 (accumulated >= max)
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.accumulatedParticipationCount = p.accumulatedParticipationCount + 1 WHERE p.id = :promotionId AND p.accumulatedParticipationCount < p.maxParticipationCount")
    fun incrementCountIfAvailable(@Param("promotionId") promotionId: Long): Int

    /**
     * 참여 가능한 프로모션 목록 조회 — @Query + @QueryHints 버전
     *
     * 제외 조건:
     *  1. 참여 기간이 아닌 프로모션
     *  2. 당일 이미 참여한 프로모션
     *  3. PERSONAL_PARTICIPATION_LIMIT 정책: 유저의 누적 참여횟수 >= 제한값
     *  4. PREREQUISITE_PROMOTION 정책: 선행 프로모션에 단 한 번도 참여한 적 없음
     *
     * pp.promotionId = p.id 로 조인 — ManyToOne 연관관계 없이 FK 직접 비교
     */
    @Query(
        value = """
            SELECT p FROM Promotion p
            WHERE p.participationStartAt <= :now
              AND p.participationEndAt   >= :now
              AND NOT EXISTS (
                  SELECT pt FROM Participation pt
                  WHERE pt.promotionId      = p.id
                    AND pt.userId           = :userId
                    AND pt.participationDate = :today
              )
              AND NOT EXISTS (
                  SELECT pp FROM ParticipationPolicy pp
                  WHERE pp.promotionId = p.id
                    AND pp.policyCode  = com.flab.promotion.domain.enums.PolicyCode.PERSONAL_PARTICIPATION_LIMIT
                    AND pp.personalParticipationLimit <=
                        (SELECT COUNT(pt2) FROM Participation pt2
                          WHERE pt2.promotionId = p.id AND pt2.userId = :userId)
              )
              AND NOT EXISTS (
                  SELECT pp2 FROM ParticipationPolicy pp2
                  WHERE pp2.promotionId = p.id
                    AND pp2.policyCode  = com.flab.promotion.domain.enums.PolicyCode.PREREQUISITE_PROMOTION
                    AND NOT EXISTS (
                        SELECT pt3 FROM Participation pt3
                        WHERE pt3.promotionId = pp2.prerequisitePromotionId
                          AND pt3.userId      = :userId
                    )
              )
              AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%'))
              AND (:startDate IS NULL OR p.participationStartAt >= :startDate)
              AND (:endDate   IS NULL OR p.participationEndAt   <= :endDate)
            ORDER BY p.rewardAmount DESC
        """,
        countQuery = """
            SELECT COUNT(p) FROM Promotion p
            WHERE p.participationStartAt <= :now
              AND p.participationEndAt   >= :now
              AND NOT EXISTS (
                  SELECT pt FROM Participation pt
                  WHERE pt.promotionId      = p.id
                    AND pt.userId           = :userId
                    AND pt.participationDate = :today
              )
              AND NOT EXISTS (
                  SELECT pp FROM ParticipationPolicy pp
                  WHERE pp.promotionId = p.id
                    AND pp.policyCode  = com.flab.promotion.domain.enums.PolicyCode.PERSONAL_PARTICIPATION_LIMIT
                    AND pp.personalParticipationLimit <=
                        (SELECT COUNT(pt2) FROM Participation pt2
                          WHERE pt2.promotionId = p.id AND pt2.userId = :userId)
              )
              AND NOT EXISTS (
                  SELECT pp2 FROM ParticipationPolicy pp2
                  WHERE pp2.promotionId = p.id
                    AND pp2.policyCode  = com.flab.promotion.domain.enums.PolicyCode.PREREQUISITE_PROMOTION
                    AND NOT EXISTS (
                        SELECT pt3 FROM Participation pt3
                        WHERE pt3.promotionId = pp2.prerequisitePromotionId
                          AND pt3.userId      = :userId
                    )
              )
              AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%'))
              AND (:startDate IS NULL OR p.participationStartAt >= :startDate)
              AND (:endDate   IS NULL OR p.participationEndAt   <= :endDate)
        """
    )
    @QueryHints(
        QueryHint(name = "org.hibernate.readOnly", value = "true"),
        QueryHint(name = "org.hibernate.fetchSize", value = "10"),
        QueryHint(name = "jakarta.persistence.query.timeout", value = "5000")
    )
    fun findAvailablePromotionsWithHint(
        @Param("userId")    userId:    Long,
        @Param("today")     today:     LocalDate,
        @Param("now")       now:       LocalDateTime,
        @Param("name")      name:      String?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate")   endDate:   LocalDateTime?,
        pageable: Pageable
    ): Page<Promotion>
}
