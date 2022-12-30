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

package io.github.fkorax.twenty

import dorkbox.systemTray.SystemTray
import io.github.fkorax.twenty.ui.BackgroundIndicator
import io.github.fkorax.twenty.ui.HumanInterrupter
import io.github.fkorax.twenty.ui.InfoWindow
import io.github.fkorax.twenty.ui.TwentyAction
import io.github.fkorax.twenty.ui.icons.StopIcon
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.time.LocalTime
import java.util.logging.Logger
import java.util.prefs.Preferences
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

class Twenty {
    companion object {
        // When the program is first started, save a timestamp so the total screen time can be calculated
        @JvmField
        val startTime: Long = System.currentTimeMillis()

        val timeSinceStart: Long get() = System.currentTimeMillis() - startTime

        private var developerMode: Boolean = false

        @JvmField
        val resources = Resources(
            "/${this::class.java.packageName.replace('.', '/')}/res/",
            "icons/"
        )

        @JvmStatic
        fun main(args: Array<String>) {
            // Use the cross-platform LookAndFeel by default
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())

            developerMode = "--dev" in args

            // Only set the SystemTray to DEBUG if we are in developer mode
            SystemTray.DEBUG = developerMode

            Twenty().main()
        }

        private fun testInterrupt() {
            HumanInterrupter.testWindow(object : ComponentAdapter() {
                override fun componentHidden(e: ComponentEvent?) {
                    exitProcess(0)
                }
            })
        }

        /**
         * Twenty relies on these fallback Settings if some parts of
         * the user Settings cannot be applied. In this case, Settings
         * are explicitly 'overridden'. These Settings are not the defaults,
         * and Twenty replaces inapplicable Settings individually (so if one
         * Setting cannot be applied, only that Setting will be replaced with
         * its fallback setting).
         */
        private val FALLBACK_SETTINGS = Settings(
            breakDuration = Setting.BreakSeconds.MIN_VALUE,
            sessionDuration = Setting.SessionMinutes.MAX_VALUE,
            nightLimitTime = Setting.LocalHmTime(LocalTime.of(21, 0)),
            nightLimitActive = Setting.ActiveOn(emptySet()),    // Night limit turned off by default
            nightSessionDuration = Setting.SessionMinutes(5),
            playAlertSound = Setting.Toggle(false),
            lookAndFeel = Setting.LookAndFeel.CROSS_PLATFORM
            // The default cross-platform look and feel should always work...
        )
    }

    private val logger: Logger = Logger.getLogger(this::class.qualifiedName)

    /**
     * The Preferences object to retrieve and store the [settings] in a user node.
     */
    private val preferences: Preferences = Preferences.userNodeForPackage(this::class.java)

    /**
     * The program settings. Initialized to [FALLBACK_SETTINGS], but will later be changed
     * with the loaded settings (if not problems are encountered during loading).
     */
    private val settings: Settings = FALLBACK_SETTINGS

    private val title: String = if (developerMode) "20ty (Developer Mode)" else "20ty"

    private val schedulator = Schedulator()
    private val interrupter = HumanInterrupter()
    private val infoWindow = InfoWindow(title)

    private val showMainWindowAction = TwentyAction("Info", null, "Open the main window.", ::showInfoWindow)
    private val pauseAction = TwentyAction("Pause", null, "Pause the application.") { -> TODO("Implement pause") }
    private val stopAction = TwentyAction("Stop", StopIcon(), "Close the application.", ::stop)
    private val testInterruptAction = TwentyAction("Test Interrupt", null, "Test the interrupt functionality.", ::testInterrupt)

    private val actions = listOf(
        showMainWindowAction,
        pauseAction,
        stopAction
    ).let { if (developerMode) it + testInterruptAction else it }

    fun applySettings(change: Settings) {
        // TODO After every settings change, the new settings should
        //  be stored in preferences
        //  (why are they stored every time? → Sometimes, a fallback could have
        //  been applied, and we want the user to recognize that)
        println("TODO: Apply given settings.")
    }

    fun main() {
        // Try and load the stored Settings
        val storedSettingsChangeResult = Settings.loadFrom(preferences)
        // Apply the stored settings, or apply the default settings,
        // which will also cause them to be stored
        applySettings(storedSettingsChangeResult.getOrDefault(FALLBACK_SETTINGS))

        // Create the BackgroundIndicator support
        try {
            BackgroundIndicator.create(actions)
        }
        catch (t: Throwable) {
            // BackgroundIndicator may encounter weird Exceptions or Errors
            // because it is interacting with native components...
            // In this case, it must be ensured that the process actually stops.
            System.err.println("Encountered serious problem while creating BackgroundIndicator.")
            System.err.println("Stack Trace:")
            t.printStackTrace()
            System.err.println("Aborting program...")
            exitProcess(1)
        }
        schedulator.schedule(interrupter::interruptHuman, 20)
    }

    private fun stop() {
        logger.info("Process stopped by user.")
        exitProcess(0)
    }

    // TODO Rename to MainWindow
    private fun showInfoWindow() {
        SwingUtilities.invokeLater {
            // If the info window is not visible:
            if (!infoWindow.isVisible) {
                // Show the InfoWindow, centered on screen
                infoWindow.setLocationRelativeTo(null)
                infoWindow.isVisible = true
            }
            // Otherwise:
            else {
                // Simply bring the window to the front
                infoWindow.toFront()
            }
        }
    }

}
