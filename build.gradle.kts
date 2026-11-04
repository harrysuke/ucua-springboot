import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    war
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("gg.jte.gradle") version "3.2.5"
}

group = "com.ucua"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // JTE (Java/Kotlin Template Engine) - server-side HTML rendering
    implementation("gg.jte:jte-spring-boot-starter-3:3.2.5")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // SQLite
    runtimeOnly("org.xerial:sqlite-jdbc:3.47.0.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.3.Final")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Servlet API for WAR packaging
    providedRuntime("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// JTE template precompilation (production)
//
// `precompile()` compiles all .jte templates to .class files in ./jte-classes
// at build time, so the packaged WAR can run with the `prod` profile
// (gg.jte.use-precompiled-templates=true) without a JDK at runtime.
// Dev mode (`./gradlew bootRun`, default profile) still hot-reloads from src/main/jte.
// ---------------------------------------------------------------------------
jte {
    precompile()
}

val jtePrecompiledClasses = fileTree("jte-classes") {
    include("**/*.class")
    include("**/*.bin") // only present if binaryStaticContent is enabled
}

tasks.named<org.gradle.api.tasks.bundling.War>("war") {
    dependsOn("precompileJte")
    from(jtePrecompiledClasses) { into("WEB-INF/classes") }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    dependsOn("precompileJte")
    from(jtePrecompiledClasses) { into("WEB-INF/classes") }
}
