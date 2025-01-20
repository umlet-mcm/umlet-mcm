group = "at.ac.tuwien.model.change.management"
version = "1.0-SNAPSHOT"

tasks.register("buildElectronApp") {
    group = "custom"
    description = "Run bootJar and package the frontend in an electron app"

    dependsOn("mcm-server:bootJar")

    doLast {
        println("bootJar done, running 'npm run electron:build'")

        val frontendPath = project.rootDir.parentFile.resolve("mcm-frontend")
        print(frontendPath)
        exec {
            workingDir = frontendPath
            commandLine("npm", "run", "electron:build")
        }

        println("frontend build")
    }
}