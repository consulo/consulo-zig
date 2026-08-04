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

package com.falsepattern.zigbrains.clion

import com.falsepattern.zigbrains.debugger.ZigDebuggerDriverConfigurationProvider
import com.falsepattern.zigbrains.debugger.settings.ZigDebuggerSettings
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionGDBDriverConfiguration
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionLLDBDriverConfiguration
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration

class ZigClionDebuggerDriverConfigurationProvider: ZigDebuggerDriverConfigurationProvider() {
    override suspend fun getDebuggerConfiguration(
        project: Project,
        isElevated: Boolean,
        emulateTerminal: Boolean
    ): DebuggerDriverConfiguration? {
        if (SystemInfo.isWindows)
            return null

        if (!ZigDebuggerSettings.instance.useClion)
            return null

        val toolchains = CPPToolchains.getInstance()
        var toolchain = toolchains.getToolchainByNameOrDefault("Zig")
        if (toolchain == null || !toolchain.isDebuggerSupportedReflective) {
            LOG.info("Couldn't find debug-compatible C++ toolchain with name \"Zig\"")
            toolchain = toolchains.defaultToolchain
        }
        if (toolchain == null || !toolchain.isDebuggerSupportedReflective) {
            LOG.info("Couldn't find debug-compatible C++ default toolchain")
            return null
        }
        return when(toolchain.debuggerKindReflective) {
            CPPDebugger.Kind.BUNDLED_GDB,
            CPPDebugger.Kind.CUSTOM_GDB -> CLionGDBDriverConfiguration(project, toolchain, isEmulateTerminal = emulateTerminal)
            CPPDebugger.Kind.BUNDLED_LLDB,
            CPPDebugger.Kind.CUSTOM_LLDB -> CLionLLDBDriverConfiguration(project, toolchain, isEmulateTerminal = emulateTerminal)
            else -> null
        }
    }
}

//Deprecated for removal api, but replacement is complex
private val CPPToolchains.Toolchain.isDebuggerSupportedReflective: Boolean get() {
    return try {
        CPPToolchains.Toolchain::class.java.getDeclaredMethod("isDebuggerSupported").invoke(this) as? Boolean ?: false
    } catch (_: Throwable) {
        false
    }
}

//Deprecated for removal api, but replacement is complex
private val CPPToolchains.Toolchain.debuggerKindReflective: CPPDebugger.Kind? get() {
    return try {
        CPPToolchains.Toolchain::class.java.getDeclaredMethod("getDebuggerKind").invoke(this) as? CPPDebugger.Kind
    } catch (_: Throwable) {
        null
    }
}

private val LOG = logger<ZigClionDebuggerDriverConfigurationProvider>()