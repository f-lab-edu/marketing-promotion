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
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "MEM_USER_LOGIN_HISTORY")
@EntityListeners(AuditingEntityListener::class)
class LoginHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val action: LoginAction,

    @Column(length = 45)
    val ip: String?,

    @Column(length = 500)
    val userAgent: String?,

    @Column(length = 100)
    val errorCode: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class LoginAction {
    LOGIN,
    LOGIN_FAIL,
    LOGOUT,
    SUSPEND,
    UNSUSPEND,
    WITHDRAW
}
