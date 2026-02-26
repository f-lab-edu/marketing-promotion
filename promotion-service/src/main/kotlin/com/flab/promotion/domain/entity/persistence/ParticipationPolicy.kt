package com.flab.promotion.domain.entity.persistence

import com.flab.promotion.domain.enums.PolicyCode
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "PRMT_PARTICIPATION_POLICY")
@EntityListeners(AuditingEntityListener::class)
class ParticipationPolicy(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    var policyCode: PolicyCode,

    var prerequisitePromotionId: Long? = null,  // PREREQUISITE_PROMOTION 정책
    var personalParticipationLimit: Int? = null, // PERSONAL_PARTICIPATION_LIMIT 정책

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    var createdBy: Long = 0,
    var updatedBy: Long = 0
) {
    // 클래스 바디 선언: kotlin.plugin.jpa 가 생성하는 no-arg 생성자로 Hibernate 가 인스턴스화 후 설정
    // Promotion.addPolicy() 호출 시 양방향 참조 동기화
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    lateinit var promotion: Promotion
}
