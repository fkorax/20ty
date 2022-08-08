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
import java.awt.Toolkit
import java.awt.TrayIcon
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

class TwentyApp {
    // TODO The tray is optional. But what if there is no tray support?
    private val trayIcon: TrayIcon? =
        if (SystemTray.isSupported())
            TwentyTrayIcon(::showInfoWindow, ::showInterruptWindow).also(SystemTray.getSystemTray()::add)
        else
            null
    private val infoWindow = InfoWindow()
    private val interruptWindow = InterruptWindow()

    fun main() {
        // The Schedulator (Schedule(d) Executor / Schedule + Regulator)
        val executor = ScheduledThreadPoolExecutor(1)
        // Schedule the interrupt task at a fixed delay
        executor.scheduleWithFixedDelay(
            ::showInterruptWindow, 20, 20, TimeUnit.MINUTES)
    }

    private fun showInterruptWindow(): Unit = if (interruptWindow.isVisible) {
        println("Interrupt Window is still visible. Not showing again.")
    }
    else {
        SwingUtilities.invokeLater {
            // Center and show the interrupt window
            interruptWindow.setLocationRelativeTo(null)
            interruptWindow.isVisible = true
            // Put the window to the front, so the user sees it
            interruptWindow.toFront()
            // Beep
            // TODO Allow the user to turn this off, and later,
            //  to choose in a combo box between a System Beep, 20ty sounds, and a custom sound file
            Toolkit.getDefaultToolkit().beep()
            trayIcon?.displayMessage("Look outside", "Look 20 m into the distance.", TrayIcon.MessageType.NONE)
            // TODO Also beep on window hidden
        }
    }

    private fun showInfoWindow() {
        SwingUtilities.invokeLater {
            // Show the InfoWindow, centered on screen
            infoWindow.setLocationRelativeTo(null)
            infoWindow.isVisible = true
        }
    }

}
