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

import io.github.fkorax.twenty.Twenty
import io.github.fkorax.twenty.ui.icons.SettingsIcon
import io.github.fkorax.twenty.ui.icons.StopIcon
import io.github.fkorax.twenty.ui.util.emptyBorder
import io.github.fkorax.twenty.ui.util.scale
import io.github.fkorax.twenty.util.forEach
import io.github.fkorax.twenty.util.section
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.swing.*
import kotlin.system.exitProcess

class InfoWindow(title: String) : JFrame(title) {

    private class ScreenTimeDisplay : JLabel() {
        companion object {
            const val MINUTES_SINGLE_THRESHOLD = 1000*60*10 // 10 minutes
            const val MINUTES_DOUBLE_THRESHOLD = 1000*60*60    // 60 minutes = 1 hour
            const val HOURS_SINGLE_THRESHOLD = 1000*60*60*10   // 10 hours

            @JvmField
            val TIME_ZONE_UTC: TimeZone = TimeZone.getTimeZone("UTC")
        }
        private enum class DateFormatType(val formatString: String) {
            MINUTES_SINGLE ("m 'min'"),
            MINUTES_DOUBLE ("mm 'min'"),
            HOURS_SINGLE ("H 'h' mm 'min'"),
            HOURS_DOUBLE ("HH 'h' mm 'min'")
            ;

            val dateFormat: SimpleDateFormat by lazy {
                SimpleDateFormat(formatString).also {
                    it.timeZone = TIME_ZONE_UTC
                }
            }

        }

        var millis: Long = 0L
            set(value) {
                // Ensure no negative numbers are being given
                require(value >= 0) { "Milliseconds must not be negative." }
                // Check which date format to use
                val dateFormatType = when {
                    value < MINUTES_SINGLE_THRESHOLD -> DateFormatType.MINUTES_SINGLE
                    value < MINUTES_DOUBLE_THRESHOLD -> DateFormatType.MINUTES_DOUBLE
                    value < HOURS_SINGLE_THRESHOLD -> DateFormatType.HOURS_SINGLE
                    else -> DateFormatType.HOURS_DOUBLE
                }

                field = value
                this.text = dateFormatType.dateFormat.format(millis)
            }

        init {
            this.scale(2.0f)

            this.millis = 0L
        }

    }

    private val logger = Logger.getLogger(this::class.qualifiedName)

    private val screenTimeDisplay = ScreenTimeDisplay()

    private var lastUpdateMillis = 0L
    private fun updateScreenTimeDisplay() = synchronized(screenTimeDisplay) {
        if (System.currentTimeMillis() - lastUpdateMillis >= 1000) {
            screenTimeDisplay.millis = Twenty.timeSinceStart
            logger.finest("Updated screen time display.")
            lastUpdateMillis = System.currentTimeMillis()
        }
    }

    init {
        // For continuous updating of this information, we don't need an external Thread,
        // we can rely on the user's actions as impulse givers to do that.

        section("listeners") {
            // We react to mouse moved events to update
            // the time information, but in relatively fixed time intervals,
            // ignoring all events in-between,...
            val awtEventListener = { e: AWTEvent? ->
                if (e?.source == this && e.id == MouseEvent.MOUSE_MOVED) {
                    updateScreenTimeDisplay()
                }
            }
            // ...but only if this window is visible
            this.addComponentListener(object : ComponentAdapter() {
                override fun componentShown(e: ComponentEvent?) {
                    logger.fine("AWTEventListener added")
                    Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, AWTEvent.MOUSE_MOTION_EVENT_MASK)
                }
                override fun componentHidden(e: ComponentEvent?) {
                    logger.fine("AWTEventListener removed")
                    Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener)
                }
            })

            // Every time the window focus is gained (which in practice means lost & gained),
            // update the screen time information, so that the screen is always up-to-date
            // when the user switches to it
            this.addWindowFocusListener(object : WindowAdapter() {
                override fun windowGainedFocus(e: WindowEvent?) =
                    updateScreenTimeDisplay()
            })
        }

        val padSize = screenTimeDisplay.font.size

        // Content Pane configuration
        val contentPane = this.contentPane
        contentPane.layout = BorderLayout()
        if (contentPane is JComponent) {
            contentPane.border = emptyBorder(padSize / 2)
        }

        section("screenTimePanel") {
            val screenTimePanel = Box.createVerticalBox()
            screenTimePanel.border = BorderFactory.createCompoundBorder(
                // A small hack to create a platform-appropriate border
                BorderFactory.createTitledBorder(""), emptyBorder(padSize),
            )
            // TODO Display two labels:
            //  Screen Time and time to the next 20ty Event
            val screenTimeLabel = JLabel("Screen Time")
            // Center both components horizontally
            forEach(screenTimeLabel, screenTimeDisplay) {
                it.alignmentX = CENTER_ALIGNMENT
            }
            forEach(screenTimeLabel, screenTimeDisplay, screenTimePanel::add)
            add(screenTimePanel, BorderLayout.CENTER)
        }

        section("bottomPanel") {
            // The buttons panel
            val buttons = Box.createHorizontalBox().apply {
                border = BorderFactory.createEmptyBorder(padSize / 2, 0, 0, 0)

                // The 'Stop' Button
                val stopButton = JButton("Stop", StopIcon())
                stopButton.addActionListener {
                    exitProcess(0)
                }

                // The 'Settings' Button
                val settingsButton = JButton("Settings", SettingsIcon())

                forEach(stopButton, Box.createHorizontalGlue(), settingsButton, this::add)
            }
            add(buttons, BorderLayout.SOUTH)
        }

        // Window configuration
        this.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE

        // Pack, so the dialog gets to its final size
        pack()
        // Set the minimum size to the size after packing
        this.minimumSize = size
    }

}
