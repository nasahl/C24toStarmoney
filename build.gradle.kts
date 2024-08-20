plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint-idea") version "11.6.1"
    application
}

group = "de.nasahl"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
//    implementation("io.github.microutils:kotlin-logging:2.0.11")
//    implementation("ch.qos.logback:logback-classic:1.5.6")
//    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("com.jsoizo:kotlin-csv:1.10.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")

    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("de.nasahl.csv2camt.Application.kt")
}

ktlint {
    disabledRules.set(setOf("parameter-list-wrapping"))
}
