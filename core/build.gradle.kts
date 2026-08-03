plugins {
    id("zb-module")
    kotlin("plugin.serialization")
}

dependencies {
    intellijPlatform {
        intellijIdea(libs.versions.intellij.idea)
		bundledModule("intellij.spellchecker")
    }
    compileOnly(libs.kotlin.serialization) {
        isTransitive = false
    }
    implementation(project(":lang")) {
        isTransitive = false
    }
}
