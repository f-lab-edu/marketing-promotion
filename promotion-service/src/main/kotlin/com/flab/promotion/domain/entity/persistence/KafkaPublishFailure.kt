package com.flab.promotion.domain.entity.persistence

import com.flab.promotion.domain.enums.KafkaPublishFailureStatus
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
