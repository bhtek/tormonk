group = "tormonk"
version = "1.1.0"

plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"

    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

extra["kotlin.version"] = "2.4.21"
extra["junit-jupiter.version"] = "6.1.3"
extra["mockito.version"] = "5.24.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
    implementation("com.beust:klaxon:5.6")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.rometools:rome:2.1.0")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
