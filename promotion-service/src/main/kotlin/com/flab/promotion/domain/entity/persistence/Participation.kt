package com.flab.promotion.domain.entity.persistence

import com.flab.promotion.domain.enums.ParticipationStatus
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
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "PRMT_PARTICIPATION")
@EntityListeners(AuditingEntityListener::class)
class Participation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val promotionId: Long,
    val userId: Long,
    val participationDate: LocalDate,

    @Enumerated(EnumType.STRING)
    var participationStatus: ParticipationStatus,

    val rewardAmount: Long,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    val createdBy: Long = 0,
    var updatedBy: Long = 0
)
