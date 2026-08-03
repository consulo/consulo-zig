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

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.plugin.kotlin.jvm)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.intellij)
    implementation(libs.plugin.foojay.resolver)
    implementation(libs.plugin.idea.ext)
    implementation(libs.plugin.changelog)
    implementation(libs.plugin.vcu)
    implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}