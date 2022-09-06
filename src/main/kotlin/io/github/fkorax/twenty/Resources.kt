/*
 * Copyright (c) 2022  Franchesko Korako
 *
 * This file is part of 20ty.
 *
 * 20ty is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 20ty is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 20ty.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.fkorax.twenty

import java.io.InputStream
import java.net.URL
import javax.swing.ImageIcon

class Resources(root: String, iconRoot: String) {
    companion object {
        @JvmStatic
        private fun formatRoot(root: String): String =
            root.trim().let { if (!it.endsWith('/')) "$it/" else it }
    }

    private val root: String = formatRoot(root)
    private val iconRoot: String = formatRoot(iconRoot)

    fun getIcon(name: String) = ImageIcon(getFile(iconRoot + name))

    fun getFile(name: String): URL? = this.javaClass.getResource(root + name)

    fun getFileAsStream(name: String): InputStream? = this.javaClass.getResourceAsStream(root + name)

}
