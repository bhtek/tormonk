import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "./versions.gradle.kts")
val jupiterVersion: String by extra

group = "tormonk"
version = "1.1.0"
val ktor_version = "2.2.4"
val spring_boot_version = "3.1.0"

plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"

    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
//    jcenter()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.beust:klaxon:5.6")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.rometools:rome:2.1.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("joda-time:joda-time:2.10.2")

    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:1.10.19")
    testImplementation("commons-io:commons-io:2.13.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        // freeCompilerArgs.addAll("-Xcontext-receivers", "-Xbackend-threads", "0")
    }
}
