package com.flab.promotionbatch.domain.enums

enum class KafkaPublishFailureStatus {
    PENDING,    // 재처리 대기
    COMPLETED,  // 재처리 성공
    ABANDONED   // 최대 재시도 횟수 초과, 수동 처리 필요
}
