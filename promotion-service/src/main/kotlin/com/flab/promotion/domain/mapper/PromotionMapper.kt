package com.flab.promotion.domain.mapper

import com.flab.promotion.domain.dto.CreatePolicyRequest
import com.flab.promotion.domain.dto.CreatePromotionRequest
import com.flab.promotion.domain.entity.persistence.ParticipationPolicy
import com.flab.promotion.domain.entity.persistence.Promotion
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface PromotionMapper {

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "policies", ignore = true),
        Mapping(target = "createdBy", source = "request.adminId"),
        Mapping(target = "updatedBy", source = "request.adminId"),
        Mapping(target = "accumulatedParticipationCount", constant = "0")
    )
    fun toPromotion(request: CreatePromotionRequest): Promotion

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "promotion", ignore = true),  // Promotion.addPolicy() 에서 설정
        Mapping(target = "createdBy", source = "adminId"),
        Mapping(target = "updatedBy", source = "adminId")
    )
    fun toParticipationPolicy(request: CreatePolicyRequest, adminId: Long): ParticipationPolicy
}
