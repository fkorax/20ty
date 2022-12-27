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

package io.github.fkorax.twenty.ui

import io.github.fkorax.twenty.Setting
import io.github.fkorax.twenty.ui.util.emptyBorder
import io.github.fkorax.twenty.util.forEach
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.util.*
import javax.swing.*

class SettingsDialog(owner: JFrame) : JDialog(owner, "Settings", true) {
    companion object {
        private fun JComponent.row(vararg components: Component) {
            // TODO Maybe LTR / RTL layouts should be taken into account here?
            val row = Box.createHorizontalBox()
            components.forEach(row::add)
            row.add(Box.createHorizontalGlue())
            this.add(row)
        }

        private fun groupBorder(title: String) = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            emptyBorder(5)
        )
    }

    init {
        this.layout = BorderLayout()

        // Differentiate the layout with different regions and buttons at the bottom
        val settingsBox = Box.createVerticalBox().apply {
            border = emptyBorder(5)

            val generalBox = Box.createVerticalBox().apply {
                border = groupBorder("General")
                // Add all the settings widgets
                row(JLabel("Session Duration (minutes):"),
                    SettingWidget.IntRanged(
                        Setting.SessionMinutes.MIN_MINUTES,
                        Setting.SessionMinutes.MAX_MINUTES
                    ) { Setting.SessionMinutes(it) }.apply {
                        majorTickSpacing = 5
                        minorTickSpacing = 1
                        labelTable = createStandardLabels(5)
                    }
                )
                row(SettingWidget.Toggle(), JLabel("Play Alert Sound"))
            }
            add(generalBox)
            add(Box.createRigidArea(Dimension(0,10)))

            val nightBox = Box.createVerticalBox().apply {
                border = groupBorder("Night")
                row(JLabel("Night Limit Time: "), SettingWidget.LocalHmTime())
            }
            add(nightBox)
            add(Box.createRigidArea(Dimension(0, 10)))

            val uiBox = Box.createVerticalBox().apply {
                border = groupBorder("Appearance & Behavior")
                row(JLabel("Look and Feel: "), SettingWidget.Selection(Setting.LookAndFeel.values()))
            }
            add(uiBox)
        }
        add(JScrollPane(settingsBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH)

        val bottomPanel = JPanel(FlowLayout(FlowLayout.TRAILING)).apply {
            // TODO Esc = Exit
            // Three buttons: Cancel, OK, Apply
            val okButton = JButton("OK")
            // OK button is default button
            this@SettingsDialog.rootPane.defaultButton = okButton
            val cancelButton = JButton("Cancel")
            // Apply button is disabled by default
            val applyButton = JButton("Apply").apply { isEnabled = false }

            forEach(okButton, cancelButton, applyButton, this::add)
        }
        add(bottomPanel, BorderLayout.SOUTH)

        this.pack()
        // The dialog is resizable,
        // but the minimum size is the size after packing
        this.minimumSize = this.size
    }

}