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

package io.github.thomorl.twenty.ui.icons

import io.github.thomorl.twenty.ui.UIConstants
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

/**
 * An icon which paints the dense live area of a material icon (`16dp`).
 * For an actual material icon, `2dp` of padding around the perimeter are required.
 *
 * See [Material Design - System icons](https://material.io/design/iconography/system-icons.html#grid-and-keyline-shapes).
 */
interface DLMaterialIcon : Icon {

    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        g?.color = UIConstants.ICON_COLOR
    }

    override fun getIconWidth(): Int = 16

    override fun getIconHeight(): Int = 16

}
