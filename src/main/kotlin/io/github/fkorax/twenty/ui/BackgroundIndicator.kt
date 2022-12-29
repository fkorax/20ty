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

import dorkbox.systemTray.SystemTray
import io.github.fkorax.twenty.Twenty
import javax.swing.Action
import javax.swing.ImageIcon
import javax.swing.JMenuItem

sealed class BackgroundIndicator {

    companion object {
        fun create(actions: List<Action>) {
            // TODO Select an appropriate icon based on OS information
            val iconPath = Twenty.resources.getIcon("icon-16.png")
            SystemTrayLibrary(iconPath, actions)
        }
    }

    private class SystemTrayLibrary(icon: ImageIcon, actions: List<Action>) : BackgroundIndicator() {
        init {
            val systemTray = SystemTray.get("20ty")
            systemTray.setImage(icon.image)
            val menu = systemTray.menu
            actions.forEach { action ->
                menu.add(JMenuItem(action))
            }
        }
    }

}
