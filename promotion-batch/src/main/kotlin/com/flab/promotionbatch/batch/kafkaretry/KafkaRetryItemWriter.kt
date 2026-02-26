package com.flab.promotionbatch.batch.kafkaretry

import com.flab.promotionbatch.domain.entity.KafkaPublishFailure
import com.flab.promotionbatch.infrastructure.KafkaPublishFailureRepository
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class KafkaRetryItemWriter(
    private val kafkaPublishFailureRepository: KafkaPublishFailureRepository
) : ItemWriter<KafkaPublishFailure> {

    override fun write(chunk: Chunk<out KafkaPublishFailure>) {
        kafkaPublishFailureRepository.saveAll(chunk.items)
    }
}
