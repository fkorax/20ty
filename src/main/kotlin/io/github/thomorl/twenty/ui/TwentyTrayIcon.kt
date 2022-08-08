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
import io.github.thomorl.twenty.Twenty
import io.github.thomorl.twenty.ui.util.item
import io.github.thomorl.twenty.ui.util.menu
import io.github.thomorl.twenty.ui.util.popupMenu
import io.github.thomorl.twenty.ui.util.separator
import java.awt.Font
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class TwentyTrayIcon(twentyFun: () -> Unit, interruptFun: () -> Unit) : TrayIcon(
    Toolkit.getDefaultToolkit().getImage(Resources.get("ui/icons/test-icon.png")), "20ty") {

    init {
        // Used multiple times:
        val twentyActionListener = ActionListener { twentyFun() }

        this.popupMenu = popupMenu("20ty") {
            item("20ty") {
                font = (font ?: Font.decode(null))?.deriveFont(Font.BOLD) ?: Font(Font.SANS_SERIF, Font.BOLD, 14)
                addActionListener(twentyActionListener)
            }
            separator()
            item("Pause")      // Continue
            separator()
            // Insert developer options if developer mode is activated
            if (Twenty.developerMode) {
                menu("Developer Options") {
                    item("Test Window") {
                        addActionListener {
                            interruptFun()
                        }
                    }
                }
                separator()
            }
            item("Exit") {
                addActionListener {
                    exitProcess(0)
                }
            }
        }

        this.isImageAutoSize = true

        this.addActionListener(twentyActionListener)
    }

}
