package com.flab.promotion.domain.entity.persistence

import com.flab.promotion.domain.enums.CompletionPolicy
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "PRMT_PROMOTION")
@EntityListeners(AuditingEntityListener::class)
class Promotion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,
    var slogan: String,
    var contentUrl: String,
    var imageUrl: String,
    var maxParticipationCount: Int,
    var accumulatedParticipationCount: Int = 0,
    var rewardAmount: Long,
    var participationStartAt: LocalDateTime,
    var participationEndAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var completionPolicy: CompletionPolicy,

    var contentDeadlineDays: Int? = null,
    var contentStartAt: LocalDateTime? = null,
    var contentEndAt: LocalDateTime? = null,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var createdBy: Long = 0,
    var updatedBy: Long = 0
) {
    @OneToMany(
        mappedBy = "promotion",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var policies: MutableList<ParticipationPolicy> = mutableListOf()

    // 양방향 관계 동기화: policy 의 promotion 참조를 설정하고 컬렉션에 추가
    fun addPolicy(policy: ParticipationPolicy) {
        policies.add(policy)
        policy.promotion = this
    }
}
