package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.ParticipationPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipationPolicyRepository : JpaRepository<ParticipationPolicy, Long> {

    // promotion 은 @ManyToOne 관계이므로 property traversal (promotion.id) 로 조회
    fun findByPromotion_Id(promotionId: Long): List<ParticipationPolicy>
}
