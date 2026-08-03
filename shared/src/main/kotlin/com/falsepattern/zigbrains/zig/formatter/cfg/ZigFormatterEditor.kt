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

package com.falsepattern.zigbrains.zig.formatter.cfg

import com.falsepattern.zigbrains.ZigBrainsBundle
import com.falsepattern.zigbrains.project.settings.ZigProjectConfigurationProvider
import com.falsepattern.zigbrains.shared.SubConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.Panel
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import javax.swing.text.PlainDocument

class ZigFormatterEditor: SubConfigurable<Project> {
    private var smartIndentMaxFileSize = ExtendableTextField(5).also { (it.document as PlainDocument).documentFilter = object: DocumentFilter() {
        override fun insertString(fb: FilterBypass?, offset: Int, string: String?, attr: AttributeSet?) {
            if (!string.isNullOrEmpty() && string.toIntOrNull() == null) {
                return
            }
            super.insertString(fb, offset, string, attr)
        }

        override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String?, attrs: AttributeSet?
        ) {
            if (!text.isNullOrEmpty() && text.toIntOrNull() == null) {
                return
            }
            super.replace(fb, offset, length, text, attrs)
        }
    } }

    override fun attach(panel: Panel): Unit = with(panel) {
        row(ZigBrainsBundle.message("settings.formatter.indent-max-size.label")) {
            cell(smartIndentMaxFileSize)
            text(ZigBrainsBundle.message("settings.formatter.indent-max-size.unit"))
        }
    }

    override fun isModified(context: Project): Boolean {
        return ZigFormatterService.getInstance(context).smartIndentMaxFileSizeKb != (smartIndentMaxFileSize.text.toIntOrNull() ?: 128)
    }

    override fun apply(context: Project) {
        ZigFormatterService.getInstance(context).smartIndentMaxFileSizeKb = smartIndentMaxFileSize.text.toIntOrNull() ?: 128
    }

    override fun reset(context: Project?) {
        smartIndentMaxFileSize.text = (context?.let { ZigFormatterService.getInstance(it).smartIndentMaxFileSizeKb } ?: 128).toString()
    }

    override fun dispose() {

    }

    class Provider: ZigProjectConfigurationProvider {
        override fun create(sharedState: ZigProjectConfigurationProvider.IUserDataBridge): SubConfigurable<Project>? {
            return ZigFormatterEditor();
        }

        override val index: Int
            get() = 200
    }
}