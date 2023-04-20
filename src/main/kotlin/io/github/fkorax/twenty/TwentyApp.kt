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

package io.github.fkorax.twenty

import dorkbox.systemTray.SystemTray
import io.github.fkorax.fusion.FusionApp
import io.github.fkorax.twenty.ui.HumanInterrupter
import io.github.fkorax.twenty.ui.MainWindow
import java.time.LocalTime
import java.util.logging.Logger
import javax.swing.UIManager
import kotlin.system.exitProcess

class TwentyApp : FusionApp(), SharedOperations {
    companion object {
        // When the program is first started, save a timestamp so the total screen time can be calculated
        // TODO Move this to the Schedulator? Into its own component? Away from the app!
        @JvmField
        val startTime: Long = System.currentTimeMillis()

        val elapsedTime: Long get() = System.currentTimeMillis() - startTime

        private var developerMode = false

        @JvmStatic
        fun main(args: Array<String>) {
            // Use the cross-platform LookAndFeel by default
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())

            developerMode = "--dev" in args

            // Only set the SystemTray to DEBUG if we are in developer mode
            SystemTray.DEBUG = developerMode

            try {
                TwentyApp().run()
            }
            catch (e: Exception) {
                // Ensure the process closes if an Exception is thrown here
                e.printStackTrace()
                // TODO Unify error management
                exitProcess(1)
            }
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
     * The program settings. Initialized to [FALLBACK_SETTINGS], but will later be changed
     * with the loaded settings (if no problems are encountered during loading).
     */
    private val settings: Settings = FALLBACK_SETTINGS

    private val title: String = if (developerMode) "20ty (Developer Mode)" else "20ty"

    private val schedulator = Schedulator()
    private val interrupter = HumanInterrupter()
    private val mainWindow = MainWindow(getSubContext("main"), this, title)

    private fun applySettings(change: Settings) {
        // TODO After every settings change, the new settings should
        //  be stored in preferences
        //  (why are they stored every time? → Sometimes, a fallback could have
        //  been applied, and we want the user to recognize that)
        println("TODO: Apply given settings.")
        // TODO The Interrupt should not show while the Settings are activated
    }

    override fun run() {
        // Try and load the stored Settings
        val storedSettingsChangeResult = Settings.loadFrom(preferences)
        // Apply the stored settings, or apply the default settings,
        // which will also cause them to be stored
        applySettings(storedSettingsChangeResult.getOrDefault(FALLBACK_SETTINGS))

        schedulator.schedule({ interrupter.interruptHuman(20) }, 20)
    }

    override val isDeveloperMode: Boolean
        get() = developerMode

    override fun stop() {
        logger.info("Process stopped by user.")
        exitProcess(0)
    }

}
