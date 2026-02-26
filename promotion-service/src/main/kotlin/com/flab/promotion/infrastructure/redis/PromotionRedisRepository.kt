package com.flab.promotion.infrastructure.redis

import com.flab.promotion.domain.dto.PromotionCacheInfo
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Repository
class PromotionRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        private const val KEY_PREFIX = "promotion"

        // Hash field names
        private const val FIELD_MAX_COUNT = "maxCount"
        private const val FIELD_START_AT = "startAt"
        private const val FIELD_END_AT = "endAt"

        // Lua 스크립트 반환 코드
        const val RESULT_ALREADY_PARTICIPATED = -2L  // SET에 이미 존재
        const val RESULT_NOT_FOUND = -1L             // 카운트 키 없음 (만료)
        const val RESULT_SOLD_OUT = -3L              // 슬롯 소진

        /**
         * 중복 체크 + 슬롯 차감을 원자적으로 처리하는 Lua 스크립트
         *
         * Redis는 싱글 스레드 + Lua 스크립트는 원자적으로 실행되므로
         * 조건(읽기)을 먼저 확인하고 통과 시에만 수정한다 → 내부 롤백 불필요
         *
         * KEYS[1]: participants SET 키
         * KEYS[2]: count 키
         * ARGV[1]: member (userId:yyyyMMdd)
         * ARGV[2]: SET TTL (초)
         *
         * 반환값:
         *  -1 = 카운트 키 없음 (PROMOTION_NOT_FOUND)
         *  -3 = 잔여 슬롯 없음 (PROMOTION_FULLY_BOOKED)
         *  -2 = 이미 참여한 유저 (ALREADY_PARTICIPATED)
         *  >= 0 = 차감 후 남은 슬롯 수 (성공)
         */
        private val CHECK_AND_DECREMENT_SCRIPT = RedisScript.of(
            """
            local setKey   = KEYS[1]
            local countKey = KEYS[2]
            local member   = ARGV[1]
            local ttl      = tonumber(ARGV[2])

            -- 1. 카운트 키 존재 확인 (프로모션 만료 체크)
            if redis.call('EXISTS', countKey) == 0 then
                return -1
            end

            -- 2. 잔여 슬롯 확인 (수정 전 선제 체크)
            if tonumber(redis.call('GET', countKey)) <= 0 then
                return -3
            end

            -- 3. 중복 참여 체크
            if redis.call('SISMEMBER', setKey, member) == 1 then
                return -2
            end

            -- 4. 모든 조건 통과 → 수정 (실패 시 내부 롤백 불필요)
            redis.call('SADD', setKey, member)
            redis.call('EXPIRE', setKey, ttl)
            return redis.call('DECR', countKey)
            """.trimIndent(),
            Long::class.java
        )

        /**
         * Kafka 발행 실패 시 INCR + SREM 을 원자적으로 롤백하는 Lua 스크립트
         *
         * KEYS[1]: count 키
         * KEYS[2]: participants SET 키
         * ARGV[1]: member (userId:yyyyMMdd)
         */
        private val ROLLBACK_SCRIPT = RedisScript.of(
            """
            redis.call('INCR', KEYS[1])
            redis.call('SREM', KEYS[2], ARGV[1])
            return 1
            """.trimIndent(),
            Long::class.java
        )
    }

    /**
     * 프로모션 등록 시 호출.
     * - 캐시 키: promotion:{id}:info (Hash) → {maxCount, startAt, endAt}
     * - 슬롯 키: promotion:{id}:{startAt}   → maxCount (DECR 대상)
     * - TTL: 참여마감일시까지
     */
    fun register(promotionId: Long, startAt: LocalDateTime, maxCount: Int, endAt: LocalDateTime) {
        val ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endAt)
        if (ttlSeconds <= 0) return

        val infoKey = buildInfoKey(promotionId)
        redisTemplate.opsForHash<String, String>().putAll(
            infoKey,
            mapOf(
                FIELD_MAX_COUNT to maxCount.toString(),
                FIELD_START_AT to startAt.format(DATETIME_FORMATTER),
                FIELD_END_AT to endAt.format(DATETIME_FORMATTER)
            )
        )
        redisTemplate.expire(infoKey, ttlSeconds, TimeUnit.SECONDS)

        redisTemplate.opsForValue().set(buildCountKey(promotionId, startAt), maxCount.toString(), ttlSeconds, TimeUnit.SECONDS)
    }

    /**
     * 캐시에서 프로모션 정보 조회.
     * null = 존재하지 않거나 TTL 만료 (참여 종료)
     */
    fun getCache(promotionId: Long): PromotionCacheInfo? {
        val entries = redisTemplate.opsForHash<String, String>().entries(buildInfoKey(promotionId))
        if (entries.isEmpty()) return null

        return PromotionCacheInfo(
            maxCount = entries[FIELD_MAX_COUNT]?.toInt() ?: return null,
            startAt = LocalDateTime.parse(entries[FIELD_START_AT] ?: return null, DATETIME_FORMATTER),
            endAt = LocalDateTime.parse(entries[FIELD_END_AT] ?: return null, DATETIME_FORMATTER)
        )
    }

    /**
     * 캐시 삭제 (프로모션 수정/삭제 시 호출).
     */
    fun evictCache(promotionId: Long) {
        redisTemplate.delete(buildInfoKey(promotionId))
    }

    /**
     * 중복 참여 체크 + 슬롯 차감을 Lua 스크립트로 원자적 처리.
     * SADD와 DECR 사이의 부분 실패 문제를 제거.
     *
     * 반환값: RESULT_ALREADY_PARTICIPATED / RESULT_NOT_FOUND / RESULT_SOLD_OUT / 0 이상(성공)
     */
    fun checkAndDecrement(
        promotionId: Long,
        userId: Long,
        date: LocalDate,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): Long {
        val ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), endAt)
        return redisTemplate.execute(
            CHECK_AND_DECREMENT_SCRIPT,
            listOf(buildParticipantsKey(promotionId), buildCountKey(promotionId, startAt)),
            "$userId:${date.format(DATE_FORMATTER)}",
            ttlSeconds.toString()
        ) ?: RESULT_NOT_FOUND
    }

    /**
     * Kafka 발행 실패 시 슬롯 복구 + 참여자 SET 제거를 원자적 처리.
     */
    fun rollbackParticipation(promotionId: Long, startAt: LocalDateTime, userId: Long, date: LocalDate) {
        redisTemplate.execute(
            ROLLBACK_SCRIPT,
            listOf(buildCountKey(promotionId, startAt), buildParticipantsKey(promotionId)),
            "$userId:${date.format(DATE_FORMATTER)}"
        )
    }

    /**
     * DECR된 슬롯만 복구 (정책 위반 시 Kafka 컨슈머에서 호출).
     * SET 항목은 유지 — 동일 유저의 재시도를 차단하기 위함.
     */
    fun rollbackCount(promotionId: Long, startAt: LocalDateTime) {
        redisTemplate.opsForValue().increment(buildCountKey(promotionId, startAt))
    }

    // 해시태그 {promotionId} 적용 → Redis Cluster 환경에서 동일 슬롯 보장
    private fun buildInfoKey(promotionId: Long): String = "$KEY_PREFIX:{$promotionId}:info"
    private fun buildCountKey(promotionId: Long, startAt: LocalDateTime): String =
        "$KEY_PREFIX:{$promotionId}:${startAt.format(DATETIME_FORMATTER)}"
    private fun buildParticipantsKey(promotionId: Long): String = "$KEY_PREFIX:{$promotionId}:participants"
}
