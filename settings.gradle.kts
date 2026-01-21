rootProject.name = "marketing-promotion"

include("promotion-service")
include("user-service")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
