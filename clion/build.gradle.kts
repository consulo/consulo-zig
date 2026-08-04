import org.jetbrains.intellij.platform.gradle.Constants

plugins {
    id("zb-module")
}

dependencies {
    intellijPlatform {
        clion(libs.versions.intellij.clion)
        bundledModules("intellij.clion.toolchains", "intellij.clion.execution", "intellij.cidr.debugger.core", "intellij.cidr.debugger.backend")
    }
    api(project(":cidr"))
}
configurations[Constants.Configurations.INTELLIJ_PLATFORM_BUNDLED_PLUGINS].dependencies.configureEach {
    if (this is ExternalModuleDependency) {
        this.isTransitive = false
    }
}
tasks {
    compileJava {
        enabled = false
    }
}
