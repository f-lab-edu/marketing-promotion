package com.flab.promotion.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig(
    @Value("\${kafka.topic.participation.name}") private val topicName: String,
    @Value("\${kafka.topic.participation.partitions}") private val partitions: Int,
    @Value("\${kafka.topic.participation.replicas}") private val replicas: Int
) {
    /**
     * 프로모션 참여 토픽 생성
     *
     * - partitions: 동일한 promotionId는 key 기반 파티셔닝으로 항상 같은 파티션으로 라우팅
     * - replicas: 브로커 수만큼 레플리카를 두어 브로커 장애 시 데이터 유실 방지
     * - min.insync.replicas=2: acks=all 과 함께 최소 2개 ISR 확인 후 ack (가용성/안정성 균형)
     */
    @Bean
    fun promotionParticipationTopic(): NewTopic {
        return TopicBuilder.name(topicName)
            .partitions(partitions)
            .replicas(replicas)
            .config("min.insync.replicas", "2")
            .build()
    }
}
