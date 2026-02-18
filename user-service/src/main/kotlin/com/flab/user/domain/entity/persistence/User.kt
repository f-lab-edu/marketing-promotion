package com.flab.user.domain.entity.persistence

import jakarta.persistence.Column
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
import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.domain.enums.UserRole
import com.flab.user.domain.enums.UserStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "MEM_USER")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false)
    var birthDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    var role: UserRole = UserRole.USER,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false)
    var failedLoginCount: Int = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, updatable = false)
    var createdBy: Long = 0,

    @Column
    var updatedBy: Long = 0
) {
    companion object {
        const val MAX_FAILED_LOGIN_COUNT = 5
    }

    fun setSuspend(modifiedBy: Long) {
        if (status == UserStatus.SUSPENDED) {
            throw BusinessException(MessageCode.USER_ALREADY_SUSPENDED)
        }
        status = UserStatus.SUSPENDED
        updatedBy = modifiedBy
    }

    fun setUnsuspend(modifiedBy: Long) {
        if (status != UserStatus.SUSPENDED) {
            throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION)
        }
        status = UserStatus.ACTIVE
        updatedBy = modifiedBy
    }

    fun incrementFailedLoginCount(): Boolean {
        failedLoginCount++
        if (failedLoginCount >= MAX_FAILED_LOGIN_COUNT) {
            status = UserStatus.SUSPENDED
            updatedBy = 0
            return true
        }
        return false
    }

    fun resetFailedLoginCount() {
        failedLoginCount = 0
    }

    fun setWithdraw() {
        if (status == UserStatus.WITHDRAWN) {
            throw BusinessException(MessageCode.USER_ALREADY_WITHDRAWN)
        }
        status = UserStatus.WITHDRAWN
        updatedBy = id!!
    }
}