package com.flab.user.infrastructure.persistence

import com.flab.user.domain.entity.persistence.LoginHistory
import org.springframework.data.jpa.repository.JpaRepository

interface UserLoginHistoryRepository : JpaRepository<LoginHistory, Long>
