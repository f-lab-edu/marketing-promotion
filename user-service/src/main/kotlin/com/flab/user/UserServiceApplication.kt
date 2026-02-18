package com.flab.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.mongodb.config.EnableMongoAuditing

@EnableJpaAuditing
@EnableMongoAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
class UserServiceApplication

fun main(args: Array<String>) {
	runApplication<UserServiceApplication>(*args)
}
