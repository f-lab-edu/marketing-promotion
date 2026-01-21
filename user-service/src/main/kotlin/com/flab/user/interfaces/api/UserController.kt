package com.flab.user.interfaces.api

import com.flab.user.application.service.UserService
import com.flab.user.common.exception.CommonHttpException
import com.flab.user.common.exception.DuplicatedEmailException
import com.flab.user.common.exception.ErrorCodes
import com.flab.user.interfaces.dto.SignUpRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun signUp(httpRequest: HttpServletRequest, @Valid @RequestBody request: SignUpRequest): ResponseEntity<Any> {
        try {
            val user = userService.signUp(request)
            val locationUri = URI.create("${httpRequest.requestURL}/${user.id}")
            return ResponseEntity.created(locationUri).build()
        } catch (e: DuplicatedEmailException) {
            throw CommonHttpException(ErrorCodes.DUPLICATED_EMAIL, HttpStatus.CONFLICT)
        }
    }
}
