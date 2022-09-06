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

import io.github.fkorax.twenty.ui.HumanInterrupter
import io.github.fkorax.twenty.ui.InfoWindow
import io.github.fkorax.twenty.ui.TraySupport
import io.github.fkorax.twenty.util.enrichSystemProperties
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.time.LocalTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Twenty {
    companion object {
        // When the program is first started, save a timestamp so the total screen time can be calculated
        @JvmField
        val startTime: Long = System.currentTimeMillis()

        val timeSinceStart: Long get() = System.currentTimeMillis() - startTime

        var developerMode: Boolean = false
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            // Enrich the system properties first
            enrichSystemProperties()

            // Use the system's LookAndFeel
            // TODO Maybe switch to an alternative LaF?
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            // TODO Convert args to flags (and detect unknown arguments!)
            val runAppMain: Boolean = when {
                "windowTest" in args -> {
                    windowTest()
                    false
                }
                "--dev" in args -> {
                    developerMode = true
                    true
                }
                else -> true
            }
            if (runAppMain) {
                Twenty().main()
            }
        }

        private fun windowTest() {
            HumanInterrupter.testWindow(object : ComponentAdapter() {
                override fun componentHidden(e: ComponentEvent?) {
                    exitProcess(0)
                }
            })
        }

        @JvmStatic
        private val DEFAULT_SETTINGS = Settings(
            breakDuration = Setting.BreakDuration(Setting.BreakDuration.MINIMUM_SECONDS.seconds),
            sessionDuration = Setting.SessionDuration(Setting.SessionDuration.MAXIMUM_MINUTES.minutes),
            nightLimitTime = Setting.NightLimitTime(LocalTime.of(21, 0)),
            nightLimitActivity = Setting.NightLimitActivity(emptySet()),    // Night limit turned off by default
            playAlertSound = Setting.PlayAlertSound(true),
            lookAndFeel = Setting.LookAndFeel.SYSTEM
        )
    }

    data class Settings(
        val breakDuration: Setting.BreakDuration? = null,
        val sessionDuration: Setting.SessionDuration? = null,
        val nightLimitTime: Setting.NightLimitTime? = null,
        val nightLimitActivity: Setting.NightLimitActivity? = null,
        val playAlertSound: Setting.PlayAlertSound? = null,
        val lookAndFeel: Setting.LookAndFeel? = null
    )

    private val logger: Logger = Logger.getLogger(this::class.qualifiedName)

    private val title: String = if (developerMode) "20ty (Developer Mode)" else "20ty"

    private val interrupter = HumanInterrupter()
    private val traySupport = TraySupport.getTraySupport(title, ::showInfoWindow, interrupter::interruptHuman)
    private val infoWindow = InfoWindow(title)

    fun main() {
        // TODO Use the Schedulator (Schedule(d) Executor / Schedule + Regulator)
        //  to do stuff like this
        val executor = ScheduledThreadPoolExecutor(1)
        // Schedule the interrupt task at a fixed delay
        executor.scheduleWithFixedDelay(
            interrupter::interruptHuman, 20, 20, TimeUnit.MINUTES)
    }

    // TODO Expose functions like these through Actions sorted by groups,
    //  which can then be directly incorporated into the TraySupport
    private fun stop() {
        logger.info("Process stopped by user.")
        exitProcess(0)
    }

    private fun showInfoWindow() {
        SwingUtilities.invokeLater {
            // Show the InfoWindow, centered on screen
            infoWindow.setLocationRelativeTo(null)
            infoWindow.isVisible = true
        }
    }

}
