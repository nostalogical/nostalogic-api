import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.2" apply(false)
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.jpa") version "1.6.10" apply(false)
    kotlin("jvm") version "1.6.10"
    war
}

allprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "war")

    repositories {
        mavenCentral()
    }

    group = "net.nostalogic.microservices"
    version = "SNAPSHOT"
    java.sourceCompatibility = JavaVersion.VERSION_17
}

subprojects {

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
        implementation("org.apache.commons:commons-lang3:3.12.0")
        implementation("org.postgresql:postgresql:42.6.0")
        implementation("com.auth0:java-jwt:4.4.0")

        implementation("org.springdoc:springdoc-openapi-data-rest:1.7.0")
        implementation("org.springdoc:springdoc-openapi-ui:1.7.0")
        implementation("org.springdoc:springdoc-openapi-kotlin:1.7.0")

        implementation("com.github.kittinunf.fuel:fuel:2.3.1")
        implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")
        implementation("com.google.code.gson:gson:2.8.6")
        implementation("org.json:json:20211205")
        implementation("com.vladmihalcea:hibernate-types-55:2.21.1")
        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
        runtimeOnly("org.springframework.boot:spring-boot-devtools")
        providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
        testImplementation("com.h2database:h2:2.1.214")
        testImplementation("org.testcontainers:postgresql:1.18.3")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        testImplementation("io.mockk:mockk:1.13.5")
        testImplementation("org.apache.httpcomponents:httpclient:4.5.14")
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
    tasks.getByName<Jar>("jar") {
        enabled = true
    }
    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        classifier = "boot"
    }

}
