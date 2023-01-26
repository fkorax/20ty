/*
 * Copyright © 2022, 2023  Franchesko Korako
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

package io.github.fkorax.twenty.ui.util

import java.awt.Rectangle
import kotlin.math.min

interface SetButtonUI {
    companion object {
        @JvmStatic
        fun toCircle(width: Int, height: Int): Rectangle =
            min(width, height).let { radius ->
                Rectangle(
                    (width - radius) / 2,   // X offset
                    (height - radius) / 2,  // Y offset
                    radius, radius
                )
        }
    }

}
