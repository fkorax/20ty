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

package io.github.fkorax.fusion

import java.awt.Component
import java.awt.Container
import java.awt.LayoutManager
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

fun Container.horizontalBox(block: Box.() -> Unit = {}): Box =
    fadd(Box.createHorizontalBox(), block)

fun Container.verticalBox(block: Box.() -> Unit = {}): Box =
    fadd(Box.createVerticalBox(), block)

fun Container.verticalGlue(): Component =
    fadd(Box.createVerticalGlue()) {}

fun Container.panel(layout: LayoutManager, block: JPanel.() -> Unit = {}): JPanel =
    fadd(JPanel(layout), block)

fun Container.button(text: String, block: JButton.() -> Unit = {}): JButton =
    fadd(JButton(text), block)

fun Container.scaledJLabel(text: String, scaleFactor: Float, block: JLabel.() -> Unit = {}) =
    fadd(JLabel(text).apply { this.scale(scaleFactor) }, block)

private fun <T : Component> Container.fadd(comp: T, block: T.() -> Unit): T {
    block(comp)
    this.add(comp)
    return comp
}
