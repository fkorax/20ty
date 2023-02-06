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

@file:JvmName("Borders")

package io.github.fkorax.fusion

import javax.swing.BorderFactory
import javax.swing.border.Border
import javax.swing.border.CompoundBorder
import javax.swing.border.TitledBorder

fun compoundBorder(outsideBorder: Border, insideBorder: Border): CompoundBorder =
    BorderFactory.createCompoundBorder(outsideBorder, insideBorder)

fun titledBorder(title: String): TitledBorder =
    BorderFactory.createTitledBorder(title)

fun emptyBorder(inset: Int): Border =
    BorderFactory.createEmptyBorder(inset, inset, inset, inset)
