/*
 * Copyright © 2022, 2023  Franchesko Korako
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
import dorkbox.systemTray.Menu
import dorkbox.systemTray.SystemTray
import io.github.fkorax.fusion.*
import io.github.fkorax.twenty.ui.util.Tree
import io.github.fkorax.twenty.ui.util.Tree.Node.Branch
import io.github.fkorax.twenty.ui.util.Tree.Node.Leaf
import io.github.fkorax.twenty.util.section
import java.awt.TrayIcon
import javax.swing.*
import java.awt.Menu as AWTMenu
import java.awt.SystemTray as AWTSystemTray

private typealias ActionTree = Tree<Action>
private typealias ActionLeaf = Leaf<Action>
private typealias ActionBranch = Branch<Action>
private typealias ActionNodeProcessor = Tree.NodeProcessor<Action>

sealed class BackgroundIndicator {

    companion object {
        fun create(actionTree: ActionTree, resources: Resources): BackgroundIndicator {
            // Select an appropriate icon based on OS information
            // TODO Guard this hacky conversion from Icon to ImageIcon
            //  (or offer an alternative route to extract the image if this fails,
            //   so the constructors can take plain icons)
            val icon = (if (OS.isLinux() && OSUtil.Linux.isUbuntu())
                resources.getIcon(Keyword["indicator-ubuntu"], 24)
            else
                resources.getIcon(Keyword["indicator]"], 32)) as ImageIcon

            return try {
                try {
                    SystemTrayLibrary(icon, actionTree)
                }
                catch (e: Exception) {
                    section("Exception handling") {
                        System.err.println("Problem in SystemTray library:")
                        e.printStackTrace()
                        System.err.println("Attempting to use AWT SystemTray instead...")
                    }
                    // Use the AWT SystemTray as fallback
                    AwtSystemTray(icon, actionTree)
                }
            }
            catch (t: Throwable) {
                // Interacting with native libraries/components
                // is tricky, so we catch and print any Throwable
                // (including Errors) before rethrowing it again
                // The rethrow is necessary because of things like
                // ThreadDeath, -> s. documentation of ThreadDeath
                System.err.println("Error while instantiating BackgroundIndicator:")
                System.err.println("${t::class.qualifiedName}: ${t.message}")
                System.err.println("(rethrown)")
                // Rethrow
                throw t
            }
        }
    }

    private class SystemTrayLibrary(icon: ImageIcon, actionTree: ActionTree) : BackgroundIndicator() {
        init {
            // Get a SystemTray instance for the specified name
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
                // TODO Set a ToolTipText, generated from the DefaultAction
                // TODO Set a Status on appropriate platforms (e.g. Windows)

                // Choose the appropriate NodeProcessor implementation
                // based on the desktop environment of the platform
                (when {
                    OSUtil.DesktopEnv.isGnome() -> ::GnomeNodeProcessor
                    else -> ::DefaultNodeProcessor
                })(menu).processNode(actionTree.root)
            }
            catch (t: Throwable) {
                // Shut down the system tray, just in case
                // it didn't happen already (correctly)
                systemTray.shutdown()
                // Rethrow
                throw t
            }
        }

        private open class DefaultNodeProcessor(protected val menu: Menu) : ActionNodeProcessor {
            override fun processLeaf(leaf: ActionLeaf) {
                menu.add(JMenuItem(leaf.value))
            }

            override fun processBranch(branch: ActionBranch): DefaultNodeProcessor =
                DefaultNodeProcessor(menu.add(JMenu(branch.text)))
        }

        /**
         * The only difference between [GnomeNodeProcessor] and [DefaultNodeProcessor] is
         * that on GNOME environments, submenus in the tray menu are not supported,
         * so this node processor simply inserts a separator to differentiate
         * between items belonging to different menus.
         */
        private class GnomeNodeProcessor(menu: Menu) : DefaultNodeProcessor(menu) {
            override fun processBranch(branch: ActionBranch): GnomeNodeProcessor {
                menu.add(JSeparator())
                return this
            }
        }

    }

    private class AwtSystemTray(icon: ImageIcon, actionTree: ActionTree) : BackgroundIndicator() {
        init {
            val trayIcon = TrayIcon(icon.image, "20ty")
            trayIcon.isImageAutoSize = true
            // TODO Add a default action (the tray icon action listener),
            //   and a way to pass the title...
            // TODO Tooltip should be the default Action's tooltip
            trayIcon.popupMenu("20ty") {
                AwtNodeProcessor(this).processNode(actionTree.root)
            }
            // Add the icon to the system tray
            AWTSystemTray.getSystemTray().add(trayIcon)
        }

        @JvmInline
        private value class AwtNodeProcessor(private val menu: AWTMenu) : ActionNodeProcessor {
            override fun processLeaf(leaf: ActionLeaf) {
                val action = leaf.value
                menu.item(action[Action.NAME] as String) {
                    this.addActionListener(action)
                }
            }

            override fun processBranch(branch: ActionBranch): AwtNodeProcessor =
                AwtNodeProcessor(menu.menu(branch.text))

        }

    }

}
