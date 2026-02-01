import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    kotlin("plugin.serialization")
}

val ideaCommunityVersion: String by project
val useInstallerProp = property("useInstaller").toString().toBoolean()
val serializationVersion: String by project

dependencies {
    intellijPlatform {
        create(IntelliJPlatformType.IntellijIdea, ideaCommunityVersion) {
            useInstaller = useInstallerProp
        }
		bundledModule("intellij.spellchecker")
    }
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$serializationVersion") {
        isTransitive = false
    }
    implementation(project(":lang")) {
        isTransitive = false
    }
}

tasks.composedJar {
    archiveFileName = "com.falsepattern.zigbrains.core.jar"
}