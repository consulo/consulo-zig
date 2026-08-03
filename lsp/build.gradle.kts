plugins {
    id("zb-module")
    kotlin("plugin.serialization")
}

dependencies {
    intellijPlatform {
        intellijIdea(libs.versions.intellij.idea)
    }
    compileOnly(libs.kotlin.serialization) {
        isTransitive = false
    }
    compileOnly(libs.lsp4ij)
    compileOnly(libs.lsp4j)
    implementation(project(":shared")) {
        isTransitive = false
    }
}

tasks {
    compileJava {
        enabled = false
    }
}
