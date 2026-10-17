plugins {
    java
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    group = "com.novashop"
    version = "0.1.0"

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    }

    repositories { mavenCentral() }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports { mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) }
    }

    tasks.withType<Test> { useJUnitPlatform() }
}
