import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.7.RELEASE" apply(false)
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("plugin.jpa") version "1.4.30-M1" apply(false)
    kotlin("jvm") version "1.4.30-M1"
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
        jcenter()
    }

    group = "net.nostalogic.microservices"
    version = "SNAPSHOT"
    java.sourceCompatibility = JavaVersion.VERSION_15
}

subprojects {

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    // Define default source sets
    sourceSets.getByName("main") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.getByName("test") {
        java.srcDir("src/main/kotlin")
    }
    sourceSets.create("integration-test") {
        java.srcDir("src/main/kotlin")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }

    // Define integration test tasks and dependencies (inherited from test)
    val integrationTest = task<Test>("integration-test") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integration-test"].output.classesDirs
        classpath = sourceSets["integration-test"].runtimeClasspath
        shouldRunAfter("test")
    }
    val `integration-testImplementation` by configurations.getting {
        extendsFrom(configurations.implementation.get())
        extendsFrom(configurations.testImplementation.get())
    }
    tasks.check { dependsOn(integrationTest) }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.apache.commons:commons-lang3:3.11")
        implementation("org.postgresql:postgresql:42.2.18")
        implementation("com.auth0:java-jwt:3.12.0")
        implementation("khttp:khttp:1.0.0")
        implementation("com.google.code.gson:gson:2.8.6")
        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
        runtimeOnly("com.h2database:h2")
        runtimeOnly("org.springframework.boot:spring-boot-devtools")
        providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        testImplementation("io.mockk:mockk:1.10.4")
        `integration-testImplementation`("org.apache.httpcomponents:httpclient:4.5.13")
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "15"
        }
    }
    tasks.getByName<Jar>("jar") {
        enabled = true
    }
    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        classifier = "boot"
    }

}
