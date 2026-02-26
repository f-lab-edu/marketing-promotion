package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.Promotion
import org.springframework.data.jpa.repository.JpaRepository

interface PromotionRepository : JpaRepository<Promotion, Long>
