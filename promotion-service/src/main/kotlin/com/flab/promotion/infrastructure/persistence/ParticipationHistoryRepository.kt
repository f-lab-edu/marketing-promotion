package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.entity.persistence.ParticipationHistory
import org.springframework.data.jpa.repository.JpaRepository

interface ParticipationHistoryRepository : JpaRepository<ParticipationHistory, Long>
