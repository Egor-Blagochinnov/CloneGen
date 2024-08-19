plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.blagochinnoved"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(18)
}

sourceSets.main {
    kotlin.srcDir("build/generated/ksp/main/kotlin")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.10-1.0.24")
    implementation("com.squareup:kotlinpoet:1.13.0")
    implementation("com.squareup:kotlinpoet-ksp:1.13.0")
}