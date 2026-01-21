package com.flab.user.application.service

import com.flab.user.common.exception.DuplicatedEmailException
import com.flab.user.domain.User
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.interfaces.dto.SignUpRequest
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun signUp(request: SignUpRequest): User {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicatedEmailException()
        }

        return try {
            userRepository.save(request.toUser(passwordEncoder.encode(request.password)))
        } catch (e: DuplicateKeyException) {
            throw DuplicatedEmailException()
        }
    }
}
