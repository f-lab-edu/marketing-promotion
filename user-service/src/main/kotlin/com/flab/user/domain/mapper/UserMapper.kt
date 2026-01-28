package com.flab.user.domain.mapper

import com.flab.user.domain.entity.User
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.dto.SignUpResponse
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface UserMapper {

    @Mappings(
        Mapping(target = "password", source = "encodedPassword"),
        Mapping(target = "id", ignore = true),
        Mapping(target = "status", expression = "java(com.flab.user.domain.entity.UserStatus.ACTIVE)"),
        Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())"),
        Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())"),
        Mapping(target = "createdBy", constant = "0L"),
        Mapping(target = "updatedBy", constant = "0L")
    )
    fun toUser(request: SignUpRequest, encodedPassword: String): User

    @Mapping(source = "id", target = "userId")
    fun toSignUpResponse(user: User): SignUpResponse
}
