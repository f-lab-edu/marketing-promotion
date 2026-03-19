package com.flab.promotionbatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class PromotionBatchApplication

fun main(args: Array<String>) {
    runApplication<PromotionBatchApplication>(*args)
}
