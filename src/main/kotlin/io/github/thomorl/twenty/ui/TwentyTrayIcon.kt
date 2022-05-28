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

package io.github.thomorl.twenty.ui

import io.github.thomorl.twenty.Resources
import java.awt.Font
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class TwentyTrayIcon(twentyActionListener: ActionListener) : TrayIcon(Toolkit.getDefaultToolkit().getImage(Resources.get("ui/icons/test-icon.png")), "20ty") {

    init {
        this.popupMenu = popupMenu("20ty") {
            menuItem("20ty") {
                this.font = (font ?: Font.decode(null))?.deriveFont(Font.BOLD) ?: Font(Font.SANS_SERIF, Font.BOLD, 14)
                addActionListener(twentyActionListener)
            }
            separator()
            menuItem("Pause")
            separator()
            menuItem("Info")
            menuItem("Settings")
            separator()
            menuItem("Exit") {
                addActionListener {
                    exitProcess(0)
                }
            }
        }

        this.isImageAutoSize = true
    }

}
