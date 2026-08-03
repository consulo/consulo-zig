/*
 * This file is part of ZigBrains.
 *
 * Copyright (C) 2023-2026 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * ZigBrains is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * ZigBrains is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ZigBrains. If not, see <https://www.gnu.org/licenses/>.
 */

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    idea
}

val libs = the<LibrariesForLibs>()

val javaVersion = libs.versions.java.get().toInt()

configure<IdeaModel> {
    module {
        isDownloadSources = true
    }
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
    val jv = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    entryCompression = ZipEntryCompression.STORED
}