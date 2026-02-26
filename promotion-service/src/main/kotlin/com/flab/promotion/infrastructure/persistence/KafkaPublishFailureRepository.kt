package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.KafkaPublishFailure
import com.flab.promotion.domain.enums.KafkaPublishFailureStatus
import org.springframework.data.jpa.repository.JpaRepository

interface KafkaPublishFailureRepository : JpaRepository<KafkaPublishFailure, Long> {

    fun findByStatusOrderByIdAsc(status: KafkaPublishFailureStatus): List<KafkaPublishFailure>
}
