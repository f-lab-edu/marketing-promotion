package com.flab.promotion.domain.enums

enum class CompletionPolicy {
    D_PLUS_N,   // 컨텐츠 마감기한: 참여일로부터 D+N일
    FROM_TO     // 컨텐츠 시작~마감일시 고정
}

enum class PolicyCode {
    PERSONAL_PARTICIPATION_LIMIT,   // 개인 참여횟수 제한
    PREREQUISITE_PROMOTION          // 선행 프로모션
}

enum class KafkaPublishFailureStatus {
    PENDING,    // 재처리 대기
    COMPLETED,  // 재처리 성공
    ABANDONED   // 최대 재시도 횟수 초과, 수동 처리 필요
}

enum class ParticipationStatus(val description: String) {
    PROCESSING("진행중"),
    CANCELLED("참여취소"),
    EXPIRED("기간만료"),
    REWARD_PENDING("리워드 지급 대기"),
    REWARD_COMPLETED("리워드 지급 완료")
}
