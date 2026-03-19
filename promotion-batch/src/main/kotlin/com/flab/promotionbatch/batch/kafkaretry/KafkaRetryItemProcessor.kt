package com.flab.promotionbatch.batch.kafkaretry

import com.flab.promotionbatch.domain.dto.PromotionParticipationEvent
import com.flab.promotionbatch.domain.entity.KafkaPublishFailure
import com.flab.promotionbatch.domain.enums.KafkaPublishFailureStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class KafkaRetryItemProcessor(
    private val kafkaTemplate: KafkaTemplate<String, PromotionParticipationEvent>
) : ItemProcessor<KafkaPublishFailure, KafkaPublishFailure> {

    private val logger = KotlinLogging.logger {}

    override fun process(failure: KafkaPublishFailure): KafkaPublishFailure {
        if (failure.retryCount >= KafkaRetryJobConfig.MAX_RETRY_COUNT) {
            logger.warn { "최대 재시도 횟수 초과 - id=${failure.id}, promotionId=${failure.promotionId}" }
            failure.status = KafkaPublishFailureStatus.ABANDONED
            return failure
        }

        try {
            kafkaTemplate.send(
                TOPIC,
                failure.promotionId.toString(),
                PromotionParticipationEvent(
                    promotionId = failure.promotionId,
                    userId = failure.userId,
                    participatedAt = failure.participatedAt,
                    startAt = failure.startAt
                )
            ).get(5, TimeUnit.SECONDS)

            failure.status = KafkaPublishFailureStatus.COMPLETED
            logger.info { "Kafka 재발행 성공 - id=${failure.id}, promotionId=${failure.promotionId}" }
        } catch (e: Exception) {
            failure.retryCount++
            logger.error(e) { "Kafka 재발행 실패 - id=${failure.id}, retryCount=${failure.retryCount}" }
        }

        return failure
    }

    companion object {
        const val TOPIC = "promotion.participation"
    }
}
