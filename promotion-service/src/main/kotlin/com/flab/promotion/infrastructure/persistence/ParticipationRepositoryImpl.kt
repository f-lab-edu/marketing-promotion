package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.dto.ParticipationHistoryResponse
import com.flab.promotion.domain.dto.ParticipationHistorySearchRequest
import com.flab.promotion.domain.entity.persistence.QParticipation
import com.flab.promotion.domain.entity.persistence.QPromotion
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ParticipationRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ParticipationRepositoryCustom {

    override fun findHistory(
        userId: Long,
        request: ParticipationHistorySearchRequest,
        pageable: Pageable
    ): Page<ParticipationHistoryResponse> {
        val qParticipation = QParticipation.participation
        val qPromotion = QPromotion.promotion

        val predicate = BooleanBuilder()
            .and(qParticipation.userId.eq(userId))

        request.fromDate?.let { predicate.and(qParticipation.participationDate.goe(it)) }
        request.toDate?.let { predicate.and(qParticipation.participationDate.loe(it)) }
        request.status?.let { predicate.and(qParticipation.participationStatus.eq(it)) }

        val content = queryFactory
            .select(
                Projections.constructor(
                    ParticipationHistoryResponse::class.java,
                    qParticipation.id,
                    qParticipation.promotionId,
                    qPromotion.name,
                    qParticipation.participationDate,
                    qParticipation.participationStatus,
                    qParticipation.rewardAmount,
                    qParticipation.createdAt
                )
            )
            .from(qParticipation)
            .join(qPromotion).on(qPromotion.id.eq(qParticipation.promotionId))
            .where(predicate)
            .orderBy(qParticipation.participationDate.desc(), qParticipation.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(qParticipation.count())
            .from(qParticipation)
            .where(predicate)
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
