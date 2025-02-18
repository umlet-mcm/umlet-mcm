plugins {
    alias(libs.plugins.mcm.java.conventions)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(project(":mcm-core"))
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    testImplementation(libs.spring.boot.starter.test)
    implementation(libs.gson)
    testImplementation(libs.apache.commons.lang3)
}

tasks.test {
    useJUnitPlatform()
}