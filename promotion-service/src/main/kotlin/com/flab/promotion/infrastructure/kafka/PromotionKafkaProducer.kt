package com.flab.promotion.infrastructure.kafka

import com.flab.promotion.application.KafkaPublishFailureService
import com.flab.promotion.domain.dto.PromotionParticipationEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PromotionKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, PromotionParticipationEvent>,
    private val kafkaPublishFailureService: KafkaPublishFailureService
) {
    companion object {
        const val TOPIC = "promotion.participation"
        private val logger = KotlinLogging.logger {}
    }

    fun sendParticipation(event: PromotionParticipationEvent) {
        kafkaTemplate.send(TOPIC, event.promotionId.toString(), event)
            .whenComplete { _, ex ->
                if (ex != null) {
                    logger.error(ex) { "Kafka 발행 실패 - promotionId=${event.promotionId}, userId=${event.userId}. 실패 이력 저장" }
                    // Redis 즉시 롤백하지 않음: 배치가 재발행 성공 시 Kafka 컨슈머에서 DB 처리됨
                    kafkaPublishFailureService.save(event)
                } else {
                    logger.info { "Kafka 발행 성공 - promotionId=${event.promotionId}, userId=${event.userId}" }
                }
            }
    }
}
