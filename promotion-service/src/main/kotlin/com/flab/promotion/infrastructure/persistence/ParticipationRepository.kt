package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.Participation
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ParticipationRepository : JpaRepository<Participation, Long> {

    fun existsByPromotionIdAndUserIdAndParticipationDate(
        promotionId: Long,
        userId: Long,
        participationDate: LocalDate
    ): Boolean

    fun countByPromotionIdAndUserId(promotionId: Long, userId: Long): Long

    fun existsByPromotionIdAndUserId(promotionId: Long, userId: Long): Boolean
}
