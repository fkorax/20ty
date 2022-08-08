/*
 * Copyright (c) 2022  Thomas Orlando
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

import java.awt.Component
import java.awt.Graphics
import java.awt.Polygon

class PlayIcon : DLMaterialIcon {
    // The base arrow shape
    private val baseShape = Polygon(
        intArrayOf(5, 12, 5),
        intArrayOf(4, 8, 12),
        3
    )

    override fun paintIcon(c: Component?, g: Graphics?, x: Int, y: Int) {
        super.paintIcon(c, g, x, y)

        if (g != null) {
            baseShape.translate(x, y)
            g.fillPolygon(baseShape)
            baseShape.translate(-x, -y)
        }
    }

}
