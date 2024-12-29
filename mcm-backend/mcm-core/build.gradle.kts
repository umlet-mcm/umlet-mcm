plugins {
    `java-library`

    // mcm.java.conventions is our custom build plugin, which includes some general configuration for Java
    alias(libs.plugins.mcm.java.conventions)

    // since mcm-core is not an executable, we do not apply the spring-boot plugin
    // however, it still needs to be included for spring-dependency-management to function correctly
    alias(libs.plugins.spring.boot) apply false

    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.lombok)
}

// necessary for spring-dependency-management to work without the spring-boot plugin
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(libs.jgit)
    implementation(libs.neo4j) {
        exclude(group = "org.neo4j", module = "neo4j-slf4j-provider")
    }
    implementation(libs.mapstruct)
    testImplementation(project(mapOf("path" to ":mcm-server")))
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.spring.boot.starter.data.neo4j)
    testImplementation(libs.spring.boot.starter.test)

    // These libraries need to be copied to the graphdb plugins directory
    runtimeOnly(files("src\\main\\resources\\graphDB\\plugins\\neo4j-graph-data-science-2.12.0.jar"))
    runtimeOnly(files("src\\main\\resources\\graphDB\\plugins\\apoc-5.25.1-core.jar"))

    // Dependency for copying files
    implementation(libs.commons.io)

    // Test Neo4J
    testImplementation(libs.neo4j.harness) {
        exclude(group = "org.neo4j", module = "neo4j-slf4j-provider")
    }
}

tasks.test {
    useJUnitPlatform()
}
