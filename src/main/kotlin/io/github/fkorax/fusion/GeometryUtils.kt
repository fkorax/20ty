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

@file:JvmName("GeometryUtils")

package io.github.fkorax.fusion

import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import kotlin.math.min

val Rectangle2D.inscribedEllipse: Ellipse2D
    get() = Ellipse2D.Double(this.x, this.y, this.width, this.height)

fun getInscribedSquare(width: Int, height: Int): Rectangle =
    min(width, height).let { radius ->
        Rectangle(
            (width - radius) / 2,   // X offset
            (height - radius) / 2,  // Y offset
            radius, radius
        )
    }
