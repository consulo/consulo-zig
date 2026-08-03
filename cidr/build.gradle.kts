import org.jetbrains.intellij.platform.gradle.Constants

plugins {
    id("zb-module")
}

dependencies {
    intellijPlatform {
        clion(libs.versions.intellij.clion)
        bundledPlugins("com.intellij.nativeDebug")
        bundledModules("intellij.cidr.workspaceModel")
    }
    implementation(project(":core")) {
        isTransitive = false
    }
    implementation(project(":lang")) {
        isTransitive = false
    }

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
