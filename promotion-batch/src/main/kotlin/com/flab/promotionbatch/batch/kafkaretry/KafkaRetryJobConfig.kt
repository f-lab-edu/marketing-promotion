package com.flab.promotionbatch.batch.kafkaretry

import com.flab.promotionbatch.domain.entity.KafkaPublishFailure
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class KafkaRetryJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val kafkaRetryItemReader: JpaPagingItemReader<KafkaPublishFailure>,
    private val kafkaRetryItemProcessor: KafkaRetryItemProcessor,
    private val kafkaRetryItemWriter: KafkaRetryItemWriter
) {
    companion object {
        const val MAX_RETRY_COUNT = 3
        const val CHUNK_SIZE = 10
    }

    @Bean
    fun kafkaRetryJob(): Job {
        return JobBuilder("kafkaRetryJob", jobRepository)
            .start(kafkaRetryStep())
            .build()
    }

    @Bean
    fun kafkaRetryStep(): Step {
        return StepBuilder("kafkaRetryStep", jobRepository)
            .chunk<KafkaPublishFailure, KafkaPublishFailure>(CHUNK_SIZE, transactionManager)
            .reader(kafkaRetryItemReader)
            .processor(kafkaRetryItemProcessor)
            .writer(kafkaRetryItemWriter)
            .build()
    }
}
