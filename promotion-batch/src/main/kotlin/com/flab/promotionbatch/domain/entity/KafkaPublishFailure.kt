package com.flab.promotionbatch.domain.entity

import com.flab.promotionbatch.domain.enums.KafkaPublishFailureStatus
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

// promotion-service 의 PRMT_KAFKA_PUBLISH_FAILURE 테이블과 동일한 스키마 매핑
// ddl-auto: none 이므로 스키마 변경 없이 읽기/쓰기만 수행
@Entity
@Table(name = "PRMT_KAFKA_PUBLISH_FAILURE")
@EntityListeners(AuditingEntityListener::class)
class KafkaPublishFailure(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val promotionId: Long,
    val userId: Long,
    val participatedAt: LocalDateTime,
    val startAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: KafkaPublishFailureStatus = KafkaPublishFailureStatus.PENDING,

    var retryCount: Int = 0,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
