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

import io.github.fkorax.twenty.ui.InterruptWindow
import io.github.fkorax.twenty.util.enrichSystemProperties
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.UIManager
import kotlin.system.exitProcess

object Twenty {
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
            "-dev" in args -> {
                developerMode = true
                true
            }
            else -> true
        }
        if (runAppMain) {
            TwentyApp().main()
        }
    }

    private fun windowTest() {
        val testWindow = InterruptWindow()
        testWindow.addComponentListener(object : ComponentAdapter() {
            override fun componentHidden(e: ComponentEvent?) {
                exitProcess(0)
            }
        })
        testWindow.isVisible = true
    }

}