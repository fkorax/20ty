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

import io.github.fkorax.twenty.util.forEach
import java.awt.GridLayout
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel

class SettingsDialog(owner: JFrame) : JDialog(owner, "Settings", true) {

    init {
        // Differentiate the layout with different regions and buttons at the bottom
        this.layout = GridLayout(/* Setting::class.sealedSubclasses.size */ 2, 2, 5, 5)

        // Add all the settings widgets
        forEach(
            JLabel("Night Limit Time"),
            SettingWidget.LocalHmTime(),
            JLabel("Play Alert Sound"),
            SettingWidget.Toggle(),
            this::add
        )

        this.pack()
        this.isResizable = false
    }

}