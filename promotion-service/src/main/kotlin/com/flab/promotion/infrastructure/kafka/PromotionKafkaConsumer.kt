package com.flab.promotion.infrastructure.kafka

import com.flab.promotion.application.ParticipationProcessingService
import com.flab.promotion.domain.dto.PromotionParticipationEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PromotionKafkaConsumer(
    private val participationProcessingService: ParticipationProcessingService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @KafkaListener(
        topics = [PromotionKafkaProducer.TOPIC],
        groupId = "\${spring.kafka.consumer.group-id}",
        concurrency = "\${spring.kafka.listener.concurrency}"
    )
    fun consume(event: PromotionParticipationEvent) {
        logger.info { "Kafka 수신 - promotionId=${event.promotionId}, userId=${event.userId}, participatedAt=${event.participatedAt}" }
        participationProcessingService.process(event)
    }
}
