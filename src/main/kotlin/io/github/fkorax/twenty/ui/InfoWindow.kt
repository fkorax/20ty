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
import io.github.fkorax.twenty.ui.util.scaledJLabel
import io.github.fkorax.twenty.util.forEach
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.EmptyBorder

class InfoWindow(title: String) : JFrame(title) {
    // TODO Move the screen time display into a separate component
    private val screenTimeDisplay = scaledJLabel("<SCREEN TIME>", 2.0f)
    // Only hours and minutes are important, seconds need to be constantly updated and
    // are just visual noise, distracting the human user
    private val dateFormat = SimpleDateFormat("HH:mm", locale).apply {
        // Time Zone set to UTC since elapsed time is displayed
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val logger = Logger.getLogger(this::class.qualifiedName)

    private var lastUpdateMillis = 0L
    private fun updateScreenTimeDisplay(millis: Long) = synchronized(screenTimeDisplay) {
        if (System.currentTimeMillis() - lastUpdateMillis >= 1000) {
            logger.finest("Updated screen time display.")
            screenTimeDisplay.text = dateFormat.format(millis)
            lastUpdateMillis = System.currentTimeMillis()
        }
    }

    private fun updateScreenTimeDisplay() =
        updateScreenTimeDisplay(Twenty.timeSinceStart)

    init {
        // For continuous updating of this information, we don't need an external Thread,
        // we can rely on the user's actions as impulse givers to do that.

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

        val padSize = screenTimeDisplay.font.size

        // Content Pane configuration
        this.contentPane = Box.createVerticalBox().apply {
            border = EmptyBorder(padSize, padSize, padSize, padSize)
        }

        // TODO Display two labels:
        //  Screen Time and time to the next 20ty Event
        val screenTimeLabel = JLabel("Screen Time")

        // The buttons panel
        val buttons = JPanel(BorderLayout()).apply {
            // The 'Close' Button
            // TODO Change this to 'Stop 20ty', since a close button is incredibly redundant
            val closeButton = JButton("Close")
            closeButton.addActionListener {
                this@InfoWindow.isVisible = false
            }
            add(closeButton, BorderLayout.WEST)

            // The 'Settings' Button
            val settingsButton = JButton("Settings", SettingsIcon())
            add(settingsButton, BorderLayout.EAST)
        }

        // Set alignments
        forEach(screenTimeLabel, screenTimeDisplay, buttons) {
            it.alignmentX = CENTER_ALIGNMENT
        }

        // Add components
        forEach(
            screenTimeLabel,
            screenTimeDisplay,
            Box.createVerticalStrut(padSize),
            buttons,
            this::add)

        // Window configuration
        this.isResizable = false
        this.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE

        // Pack, so the dialog gets to its final size
        pack()
    }

}
