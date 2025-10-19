import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec

plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.aitrimmer.backend.App")
}

tasks.jar {
    archiveBaseName.set("ai-trimmer-backend")
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

val frontendDir = projectDir.resolve("../frontend")

val frontendClean by tasks.registering(Delete::class) {
    delete(frontendDir.resolve("dist"))
}

val frontendInstall by tasks.registering(Exec::class) {
    workingDir = frontendDir
    commandLine("npm", "install")
    inputs.file(frontendDir.resolve("package.json"))
    val packageLock = frontendDir.resolve("package-lock.json")
    if (packageLock.exists()) {
        inputs.file(packageLock)
    }
    outputs.dir(frontendDir.resolve("node_modules"))
}

val frontendBuild by tasks.registering(Exec::class) {
    workingDir = frontendDir
    commandLine("npm", "run", "build")
    dependsOn(frontendInstall)
    inputs.dir(frontendDir.resolve("src"))
    inputs.file(frontendDir.resolve("index.html"))
    inputs.file(frontendDir.resolve("package.json"))
    inputs.file(frontendDir.resolve("tsconfig.json"))
    inputs.file(frontendDir.resolve("vite.config.ts"))
    outputs.dir(frontendDir.resolve("dist"))
}

tasks.named("clean") {
    dependsOn(frontendClean)
}

tasks.named("build") {
    dependsOn(frontendBuild)
}
