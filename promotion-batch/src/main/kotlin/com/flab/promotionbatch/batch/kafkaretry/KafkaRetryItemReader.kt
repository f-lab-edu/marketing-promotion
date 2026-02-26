package com.flab.promotionbatch.batch.kafkaretry

import com.flab.promotionbatch.domain.entity.KafkaPublishFailure
import com.flab.promotionbatch.domain.enums.KafkaPublishFailureStatus
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaRetryItemReader(
    private val entityManagerFactory: EntityManagerFactory
) {
    @Bean
    fun kafkaRetryItemReader(): JpaPagingItemReader<KafkaPublishFailure> {
        return JpaPagingItemReaderBuilder<KafkaPublishFailure>()
            .name("kafkaRetryItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                "SELECT f FROM KafkaPublishFailure f " +
                "WHERE f.status = :status " +
                "ORDER BY f.id ASC"
            )
            .parameterValues(mapOf("status" to KafkaPublishFailureStatus.PENDING))
            .pageSize(KafkaRetryJobConfig.CHUNK_SIZE)
            .build()
    }
}
