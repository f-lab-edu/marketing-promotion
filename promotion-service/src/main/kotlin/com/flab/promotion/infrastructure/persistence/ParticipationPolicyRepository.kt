package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.ParticipationPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipationPolicyRepository : JpaRepository<ParticipationPolicy, Long> {

    fun findByPromotionId(promotionId: Long): List<ParticipationPolicy>
}
