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
    implementation(project(":core")) {
        isTransitive = false
    }
    implementation(project(":lang")) {
        isTransitive = false
    }
}

tasks {
    compileJava {
        enabled = false
    }
}
