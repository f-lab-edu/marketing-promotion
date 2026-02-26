package com.flab.promotion.application

import com.flab.promotion.domain.dto.PromotionParticipationEvent
import com.flab.promotion.domain.entity.persistence.KafkaPublishFailure
import com.flab.promotion.infrastructure.persistence.KafkaPublishFailureRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class KafkaPublishFailureService(
    private val kafkaPublishFailureRepository: KafkaPublishFailureRepository
) {
    // whenComplete 콜백은 Kafka 스레드에서 실행되므로 REQUIRES_NEW로 독립 트랜잭션 보장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(event: PromotionParticipationEvent) {
        kafkaPublishFailureRepository.save(
            KafkaPublishFailure(
                promotionId = event.promotionId,
                userId = event.userId,
                participatedAt = event.participatedAt,
                startAt = event.startAt
            )
        )
    }
}
