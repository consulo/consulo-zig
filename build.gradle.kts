import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.PublishPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.changelog") version "2.5.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.1" apply false
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4"
    idea
    `maven-publish`
}
val publishVersions = listOf("251", "252", "253", "261")
val pluginVersionFull get() = "$pluginVersion-$pluginSinceBuild"
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val javaVersion = property("javaVersion").toString().toInt()
val lsp4ijVersion: String by project
val runIdeTarget: String by project
val lsp4ijNightly = property("lsp4ijNightly").toString().toBoolean()
val useInstallerProp = property("useInstaller").toString().toBoolean()
val lsp4ijPluginString = "com.redhat.devtools.lsp4ij:$lsp4ijVersion${if (lsp4ijNightly) "@nightly" else ""}"
val ideaCommunityVersion: String by project
val clionVersion: String by project
val lspCompat = property("lspCompat").toString().toBoolean()
val cidrCompat = property("cidrCompat").toString().toBoolean()

group = "com.falsepattern"
version = pluginVersionFull

idea.project.settings.runConfigurations {
    create("1. Run with LSP", org.jetbrains.gradle.ext.Gradle::class.java) {
        taskNames = listOf("runIde")
        scriptParameters = "-PlspCompat=true"
    }
    create("2. Run without LSP", org.jetbrains.gradle.ext.Gradle::class.java) {
        taskNames = listOf("runIde")
        scriptParameters = "-PlspCompat=false"
    }
    create("3. Run with LSP, no cidr", org.jetbrains.gradle.ext.Gradle::class.java) {
        taskNames = listOf("runIde")
        scriptParameters = "-PlspCompat=true -PcidrCompat=false -PrunIdeTarget=ideaCommunity"
    }
    create("4. Run without LSP, no cidr", org.jetbrains.gradle.ext.Gradle::class.java) {
        taskNames = listOf("runIde")
        scriptParameters = "-PlspCompat=false -PcidrCompat=false -PrunIdeTarget=ideaCommunity"
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.intellij.platform.module")
    apply(plugin = "idea")

    extensions.configure<KotlinJvmProjectExtension>("kotlin") {
        jvmToolchain(javaVersion)
        compilerOptions {
            jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
             freeCompilerArgs.addAll("-Xlambdas=indy")
        }
    }
}

tasks {
    processResources {
        from("LICENSE")
        from("licenses") {
            into("licenses")
        }
    }
}

allprojects {
    idea {
        module {
            isDownloadSources = true
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
            @Suppress("UnstableApiUsage")
            vendor = JvmVendorSpec.JETBRAINS
        }
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    setUrl("https://mvn.falsepattern.com/releases")
                    name = "mavenpattern"
                }
            }
            filter {
                includeModule("com.redhat.devtools.intellij", "lsp4ij")
            }
        }
        mavenCentral()

        intellijPlatform {
            defaultRepositories()
            snapshots()
        }
    }
}

dependencies {
    intellijPlatform {
        when(runIdeTarget) {
            "ideaCommunity" -> create(IntelliJPlatformType.IntellijIdea, ideaCommunityVersion) {
                useInstaller = useInstallerProp
            }
            "clion" -> create(IntelliJPlatformType.CLion, clionVersion) {
                useInstaller = useInstallerProp
            }
        }

        pluginVerifier()
        zipSigner()
        if (lspCompat) {
            plugin(lsp4ijPluginString)
        }
        pluginModule(runtimeOnly(project(":lang")))
        pluginModule(runtimeOnly(project(":core")))
        if (cidrCompat) {
            pluginModule(runtimeOnly(project(":cidr")))
            pluginModule(runtimeOnly(project(":clion")))
        }
        if (lspCompat) {
            pluginModule(runtimeOnly(project(":lsp")))
        }
    }
}

intellijPlatform {
    pluginConfiguration {
        version = pluginVersionFull

        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog

        changeNotes = provider { pluginVersion }.map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        }

        ideaVersion {
            sinceBuild = pluginSinceBuild
            if (pluginUntilBuild.isNotBlank()) {
                untilBuild = pluginUntilBuild
            } else {
                untilBuild = provider { null }
            }
        }
    }

    signing {
        certificateChainFile = file("secrets/chain.crt")
        privateKeyFile = file("secrets/private.pem")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    pluginVerification {
        ides {
            select {
                types = listOf(
                    IntelliJPlatformType.IntellijIdea,
                    IntelliJPlatformType.CLion
                )
            }
        }
    }
    buildSearchableOptions = false
    instrumentCode = false
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks {
    publishPlugin {
        dependsOn(patchChangelog)
    }
    compileJava {
        enabled = false
    }
    classes {
        enabled = false
    }
    verifyPluginSignature {
        certificateChainFile = file("secrets/chain.crt")
        inputArchiveFile = signPlugin.map { it.signedArchiveFile }.get()
        dependsOn(signPlugin)
    }
    publishPlugin {
        enabled = false
    }
}


fun distFile(it: String) = layout.buildDirectory.file("dist/ZigBrains-$pluginVersion-$it-signed.zip")

publishVersions.forEach {
    tasks.register<PublishPluginTask>("jbpublish-$it").configure {
        archiveFile = distFile(it)
        token = providers.environmentVariable("IJ_PUBLISH_TOKEN")
        channels = if (pluginVersion.contains("-")) listOf("nightly") else listOf("default")
        setDependsOn(dependsOn.filter { if (it is TaskProvider<*>) it.name != "signPlugin" && it.name != "buildPlugin" else true })
    }
    tasks.named("publish").configure {
        dependsOn("jbpublish-$it")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.falsepattern"
            artifactId = "zigbrains"
            version = pluginVersion

            publishVersions.forEach {
                artifact(distFile(it)) {
                    classifier = "$it-signed"
                    extension = "zip"
                }
            }
        }
    }
    repositories {
        maven {
            name = "mavenpattern"
            url = uri("https://mvn.falsepattern.com/releases/");
            credentials {
                username = System.getenv("MAVEN_DEPLOY_USER")
                password = System.getenv("MAVEN_DEPLOY_PASSWORD")
            }
        }
    }
}