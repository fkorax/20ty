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

package io.github.fkorax.twenty.ui.util

import javax.swing.Action
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

fun emptyBorder(inset: Int) = EmptyBorder(inset, inset, inset, inset)

operator fun Action.get(key: String): Any? =
    this.getValue(key)

operator fun Action.set(key: String, value: Any?) =
    this.putValue(key, value)

fun scaledJLabel(text: String, scaleFactor: Float) = JLabel(text).apply {
    this.scale(scaleFactor)
}

fun JLabel.scale(factor: Float) {
    font = font.deriveFont(font.size2D * factor)
}
