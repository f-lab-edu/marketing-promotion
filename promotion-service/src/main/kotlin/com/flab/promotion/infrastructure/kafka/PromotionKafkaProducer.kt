package com.flab.promotion.infrastructure.kafka

import com.flab.promotion.domain.dto.PromotionParticipationEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PromotionKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, PromotionParticipationEvent>
) {
    companion object {
        const val TOPIC = "promotion.participation"
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Kafka 동기 발행. 브로커 응답을 받을 때까지 대기하며 실패 시 예외를 던진다.
     * 호출부(ParticipationService)에서 예외를 받아 Redis 롤백을 처리한다.
     */
    fun sendParticipation(event: PromotionParticipationEvent) {
        kafkaTemplate.send(TOPIC, event.promotionId.toString(), event).get()
        logger.info { "Kafka 발행 성공 - promotionId=${event.promotionId}, userId=${event.userId}" }
    }
}
