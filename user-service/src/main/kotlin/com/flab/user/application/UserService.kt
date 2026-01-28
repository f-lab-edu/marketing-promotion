package com.flab.user.application

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.domain.entity.User
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.dto.SignUpResponse
import com.flab.user.domain.mapper.UserMapper
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    @Transactional
    fun signUp(req: SignUpRequest): SignUpResponse {
        val user = userMapper.toUser(req, BCryptPasswordEncoder().encode(req.password)!!)

        if (userRepository.existsByEmail(user.email)) {
            throw BusinessException(MessageCode.DUPLICATED_EMAIL)
        }

        val savedUser = userRepository.save(user)
        savedUser.createdBy = savedUser.id!!
        savedUser.updatedBy = savedUser.id!!

        return userMapper.toSignUpResponse(savedUser)
    }
}
