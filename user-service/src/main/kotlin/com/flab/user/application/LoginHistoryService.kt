package com.flab.user.application

import com.flab.user.domain.entity.persistence.LoginAction
import com.flab.user.domain.entity.persistence.LoginHistory
import com.flab.user.infrastructure.persistence.UserLoginHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class LoginHistoryService(
    private val userLoginHistoryRepository: UserLoginHistoryRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(userId: Long, action: LoginAction, ip: String?, userAgent: String?, errorCode: String? = null) {
        userLoginHistoryRepository.save(
            LoginHistory(userId = userId, action = action, ip = ip, userAgent = userAgent, errorCode = errorCode)
        )
    }
}
