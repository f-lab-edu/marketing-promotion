package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.dto.AvailablePromotionSearchRequest
import com.flab.promotion.domain.entity.persistence.Promotion
import com.flab.promotion.domain.entity.persistence.QParticipation
import com.flab.promotion.domain.entity.persistence.QParticipationPolicy
import com.flab.promotion.domain.entity.persistence.QPromotion
import com.flab.promotion.domain.enums.PolicyCode
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class PromotionRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager
) : PromotionRepositoryCustom {

    // ────────────────────────────────────────────────────────────────────────────
    // QueryDSL 버전
    // ────────────────────────────────────────────────────────────────────────────
    override fun findAvailablePromotionsWithQueryDsl(
        userId: Long,
        request: AvailablePromotionSearchRequest,
        pageable: Pageable
    ): Page<Promotion> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        val qPromotion = QPromotion.promotion

        // 서브쿼리에서 별칭 충돌 방지용 추가 인스턴스
        val qTodayPt  = QParticipation("todayPt")
        val qPolicy1  = QParticipationPolicy("pp1")
        val qCountPt  = QParticipation("countPt")
        val qPolicy2  = QParticipationPolicy("pp2")
        val qPrereqPt = QParticipation("prereqPt")

        val predicate = BooleanBuilder()

        // ── 참여 기간 내 ───────────────────────────────────────────────────────────
        predicate.and(qPromotion.participationStartAt.loe(now))
        predicate.and(qPromotion.participationEndAt.goe(now))

        // ── 당일 이미 참여한 프로모션 제외 ─────────────────────────────────────────
        predicate.and(
            JPAExpressions.selectOne()
                .from(qTodayPt)
                .where(
                    qTodayPt.promotionId.eq(qPromotion.id),
                    qTodayPt.userId.eq(userId),
                    qTodayPt.participationDate.eq(today)
                ).notExists()
        )

        // ── PERSONAL_PARTICIPATION_LIMIT 초과 프로모션 제외 ───────────────────────
        predicate.and(
            JPAExpressions.selectOne()
                .from(qPolicy1)
                .where(
                    qPolicy1.promotionId.eq(qPromotion.id),
                    qPolicy1.policyCode.eq(PolicyCode.PERSONAL_PARTICIPATION_LIMIT),
                    qPolicy1.personalParticipationLimit.longValue()
                        .loe(
                            JPAExpressions.select(qCountPt.count())
                                .from(qCountPt)
                                .where(
                                    qCountPt.promotionId.eq(qPromotion.id),
                                    qCountPt.userId.eq(userId)
                                )
                        )
                ).notExists()
        )

        // ── PREREQUISITE_PROMOTION 미충족 프로모션 제외 ───────────────────────────
        predicate.and(
            JPAExpressions.selectOne()
                .from(qPolicy2)
                .where(
                    qPolicy2.promotionId.eq(qPromotion.id),
                    qPolicy2.policyCode.eq(PolicyCode.PREREQUISITE_PROMOTION),
                    JPAExpressions.selectOne()
                        .from(qPrereqPt)
                        .where(
                            qPrereqPt.promotionId.eq(qPolicy2.prerequisitePromotionId),
                            qPrereqPt.userId.eq(userId)
                        ).notExists()
                ).notExists()
        )

        // ── 동적 검색 조건 ────────────────────────────────────────────────────────
        request.name?.let { predicate.and(qPromotion.name.containsIgnoreCase(it)) }
        request.startDate?.let { predicate.and(qPromotion.participationStartAt.goe(it)) }
        request.endDate?.let { predicate.and(qPromotion.participationEndAt.loe(it)) }

        val content = queryFactory
            .selectFrom(qPromotion)
            .where(predicate)
            .orderBy(qPromotion.rewardAmount.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(qPromotion.count())
            .from(qPromotion)
            .where(predicate)
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
