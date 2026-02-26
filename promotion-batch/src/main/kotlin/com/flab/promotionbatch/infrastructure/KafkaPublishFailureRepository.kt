package com.flab.promotionbatch.infrastructure

import com.flab.promotionbatch.domain.entity.KafkaPublishFailure
import org.springframework.data.jpa.repository.JpaRepository

interface KafkaPublishFailureRepository : JpaRepository<KafkaPublishFailure, Long>
