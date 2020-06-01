import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE" apply(false)
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("plugin.jpa") version "1.3.71"
    kotlin("jvm") version "1.3.71"
}

allprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }
}

group = "net.nostalogic.microservices"
version = "00.01.000-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

subprojects {

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.apache.commons:commons-lang3:3.10")
        implementation("org.postgresql:postgresql:42.2.12")
        implementation("com.auth0:java-jwt:3.10.3")
        runtimeOnly("com.h2database:h2")
        runtimeOnly("org.springframework.boot:spring-boot-devtools")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        testImplementation("io.mockk:mockk:1.10.0")


    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }

    sourceSets.getByName("main") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.getByName("test") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.create("integration-test") {
        java.srcDir("src/main/kotlin")
    }
}
