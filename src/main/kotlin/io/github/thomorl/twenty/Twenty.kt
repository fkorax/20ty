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

import io.github.thomorl.twenty.ui.InfoWindow
import io.github.thomorl.twenty.ui.InterruptWindow
import io.github.thomorl.twenty.ui.TwentyTrayIcon
import java.awt.SystemTray
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import javax.swing.UIManager

object Twenty {

    @JvmStatic
    fun main(args: Array<String>) {
        // Set LookAndFeel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        if (SystemTray.isSupported()) {
            val infoWindow = InfoWindow()

            val trayIcon = TwentyTrayIcon {
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

}
