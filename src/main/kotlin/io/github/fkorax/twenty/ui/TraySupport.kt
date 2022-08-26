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

import io.github.fkorax.twenty.Resources
import io.github.fkorax.twenty.Twenty
import io.github.fkorax.twenty.ui.util.item
import io.github.fkorax.twenty.ui.util.menu
import io.github.fkorax.twenty.ui.util.popupMenu
import io.github.fkorax.twenty.ui.util.separator
import java.awt.Font
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.ActionListener
import java.net.URL
import kotlin.system.exitProcess

sealed class TraySupport {
    companion object {
        fun getTraySupport(twentyFun: () -> Unit, interruptFun: () -> Unit): TraySupport =
            // In practice, we have only one option right now:
            // CrossPlatform or Fallback
            if (SystemTray.isSupported())
                CrossPlatform(twentyFun, interruptFun)
            else
                // TODO Use Fallback instead of throwing an exception
                throw UnsupportedOperationException("Tray is unsupported.")
    }

    private class CrossPlatform(twentyFun: () -> Unit, interruptFun: () -> Unit) : TraySupport() {
        companion object {
            @JvmStatic
            private val trayIconResource: URL
                get() = Resources.get(
                // Use a smaller Ubuntu notification tray compatible icon on Ubuntu 20.0 and later
                if (System.getProperty("lsb.description").startsWith("Ubuntu") &&
                    (System.getProperty("lsb.release").substringBefore('.').toIntOrNull() ?: 0) >= 20)
                    "ui/icons/icon-8.png"
                else
                    "ui/icons/icon-16.png")!!.also { println(System.getProperty("os.name") + " | " + System.getProperty("os.version")) }
        }

        private val trayIcon = TrayIcon(Toolkit.getDefaultToolkit().getImage(trayIconResource), "20ty")

        init {
            // Used multiple times:
            val twentyActionListener = ActionListener { twentyFun() }

            trayIcon.popupMenu = popupMenu("20ty") {
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

            trayIcon.isImageAutoSize = true

            trayIcon.addActionListener(twentyActionListener)

            SystemTray.getSystemTray().add(trayIcon)
        }

    }

    // TODO Fallback, in case there is no tray support

}