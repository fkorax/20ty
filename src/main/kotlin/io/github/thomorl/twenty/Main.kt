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

package io.github.thomorl.twenty

import java.awt.*
import java.awt.event.ActionListener
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // Set LookAndFeel
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    if (SystemTray.isSupported()) {
        val infoWindow = InfoWindow()

        val trayIcon = createTrayIcon {
            SwingUtilities.invokeLater {
                // Show the InfoWindow, centered on screen
                infoWindow.setLocationRelativeTo(null)
                infoWindow.isVisible = true
            }
        }

        SystemTray.getSystemTray().add(trayIcon)
    }

    val interruptWindow = InterruptWindow()

    // The Schedulator (Schedule(d) Executor / Schedule + Regulator)
    val executor = ScheduledThreadPoolExecutor(1)
    // Schedule the interrupt task at a fixed delay
    executor.scheduleWithFixedDelay(
        { SwingUtilities.invokeLater {
            // Center and show the interrupt window
            interruptWindow.setLocationRelativeTo(null)
            interruptWindow.isVisible = true
            // Put the window to the front, so the user sees it
            interruptWindow.toFront()
        }}, 20, 20, TimeUnit.MINUTES)
}

// TrayIcon gets an ActionListener
fun createTrayIcon(twentyActionListener: ActionListener): TrayIcon = TrayIcon(
    Toolkit.getDefaultToolkit().getImage(Resources.get("test-icon.png")),
    "20ty",
    PopupMenu("20ty").apply {
        add(MenuItem("20ty").apply {
            this.font = (font ?: Font.decode(null))?.deriveFont(Font.BOLD) ?: Font(Font.SANS_SERIF, Font.BOLD, 14)
            addActionListener(twentyActionListener)
        })
        addSeparator()
        add(MenuItem("Exit").apply {
            addActionListener {
                exitProcess(0)
            }
        })
    }
).also { it.isImageAutoSize = true }
