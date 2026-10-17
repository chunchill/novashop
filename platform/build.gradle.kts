plugins {
    `java-library`
}

apply(from = rootProject.file("gradle/ddd-testing.gradle"))

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
}
