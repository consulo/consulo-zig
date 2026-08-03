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

package com.falsepattern.zigbrains.zig.formatter

import com.falsepattern.zigbrains.zig.ZigLanguage
import com.falsepattern.zigbrains.zig.formatter.cfg.ZigFormatterService
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.codeStyle.lineIndent.FormatterBasedLineIndentProvider

class ZigLineIndentProvider: FormatterBasedLineIndentProvider() {
    override fun getLineIndent(
        project: Project,
        editor: Editor,
        language: Language?,
        offset: Int
    ): String? {
        val lengthKb = editor.document.textLength / 1024
        val maxLength = ZigFormatterService.getInstance(project).smartIndentMaxFileSizeKb
        if (lengthKb > maxLength) {
            return DO_NOT_ADJUST
        }
        return super.getLineIndent(project, editor, language, offset)
    }

    override fun isSuitableFor(language: Language?): Boolean {
        return language == ZigLanguage
    }
}