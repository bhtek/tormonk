import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "./versions.gradle.kts")
val jupiterVersion: String by extra

group = "tormonk"
version = "1.1.0"
val ktor_version = "2.2.4"
val spring_boot_version = "3.1.0"

plugins {
    kotlin("jvm") version "1.9.0"

    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("joda-time:joda-time:2.10.2")
    implementation("com.beust:klaxon:5.6")
    implementation("org.jonnyzzz.kotlin.xml.bind:jdom:0.2.0")

    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.mockito:mockito-core:1.10.19")
    testImplementation("commons-io:commons-io:2.13.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "19"
        // freeCompilerArgs += listOf("-Xcontext-receivers", "-Xbackend-threads 0")
    }
}
