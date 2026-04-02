import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

val lsp4jVersion: String by project
val clionVersion: String by project
val useInstallerProp = property("useInstaller").toString().toBoolean()

dependencies {
    intellijPlatform {
        create(IntelliJPlatformType.CLion, clionVersion) {
            useInstaller = useInstallerProp
        }
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

tasks.composedJar {
    archiveFileName = "com.falsepattern.zigbrains.cidr.jar"
}