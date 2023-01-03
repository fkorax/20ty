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

import dorkbox.os.OS
import dorkbox.os.OSUtil
import dorkbox.systemTray.SystemTray
import io.github.fkorax.fusion.get
import io.github.fkorax.fusion.item
import io.github.fkorax.fusion.popupMenu
import io.github.fkorax.twenty.Twenty
import io.github.fkorax.twenty.util.section
import java.awt.TrayIcon
import javax.swing.Action
import javax.swing.ImageIcon
import javax.swing.JMenuItem
import java.awt.SystemTray as AWTSystemTray

sealed class BackgroundIndicator {

    companion object {
        fun create(actions: List<Action>): BackgroundIndicator {
            // Select an appropriate icon based on OS information
            val icon = Twenty.resources.getIcon(
                if (OS.isLinux() && OSUtil.Linux.isUbuntu())
                    "indicator-ubuntu.png"
                else
                    "indicator.png"
            )
            return try {
                try {
                    SystemTrayLibrary(icon, actions)
                }
                catch (e: Exception) {
                    section("Exception handling") {
                        System.err.println("Problem in SystemTray library:")
                        e.printStackTrace()
                        System.err.println("Attempting to use AWT SystemTray instead...")
                    }
                    // Use AWT SystemTray as fallback
                    AWT(icon, actions)
                }
            }
            catch (t: Throwable) {
                // Interacting with native libraries/components
                // is tricky, so we catch and print any Throwable
                // (including Errors) before rethrowing it again
                // The rethrow is necessary because of things like
                // ThreadDeath, -> s. its documentation
                System.err.println("Error while instantiating BackgroundIndicator:")
                System.err.println("${t::class.qualifiedName}: ${t.message}")
                System.err.println("(rethrown)")
                throw t
            }
        }
    }

    private class SystemTrayLibrary(icon: ImageIcon, actions: List<Action>) : BackgroundIndicator() {
        init {
            val systemTray = SystemTray.get("20ty")
            try {
                // First: Install shutdown hook
                systemTray.installShutdownHook()
                // Set the tray icon image
                systemTray.setImage(icon.image)
                // Build the menu from the given
                // list of actions
                val menu = systemTray.menu
                // TODO Choose platform appropriate icons
                //  by referencing a to-be-defined property
                //  of TwentyAction
                actions.forEach { action ->
                    menu.add(JMenuItem(action))
                }
            }
            catch (t: Throwable) {
                // Shut down the system tray, just in case
                // it didn't happen already (correctly)
                systemTray.shutdown()
            }
        }
    }

    private class AWT(icon: ImageIcon, actions: List<Action>) : BackgroundIndicator() {
        init {
            val trayIcon = TrayIcon(icon.image, "20ty")
            trayIcon.isImageAutoSize = true
            // TODO Add a default action (the tray icon action listener),
            //   and a way to pass the title...
            // TODO Tooltip should be the default Action's tooltip
            trayIcon.popupMenu("20ty") {
                actions.forEach { action ->
                    item(action[Action.NAME] as String) {
                        this.addActionListener(action)
                    }
                }
            }
            // Add the icon to the system tray
            AWTSystemTray.getSystemTray().add(trayIcon)
        }
    }

}
