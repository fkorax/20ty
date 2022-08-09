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
import io.github.fkorax.twenty.ui.util.AnimatedJFrame
import io.github.fkorax.twenty.ui.util.scaledJLabel
import io.github.fkorax.twenty.util.forEach
import io.github.fkorax.twenty.util.sleepSafely
import java.awt.BorderLayout
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class InfoWindow : AnimatedJFrame("20ty") {
    private val dateFormat = SimpleDateFormat("HH:mm:ss", locale).apply {
        // Time Zone set to UTC since elapsed time is displayed
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val screenTimeDisplay = scaledJLabel("<SCREEN TIME>", 2.0f)

    init {
        val padSize = screenTimeDisplay.font.size

        // Content Pane configuration
        this.contentPane = Box.createVerticalBox().apply {
            border = EmptyBorder(padSize, padSize, padSize, padSize)
        }

        // TODO Two labels:
        // Screen Time and Time to the next 20ty Event
        val screenTimeLabel = JLabel("Screen Time")

        // The buttons panel
        val buttons = JPanel(BorderLayout()).apply {
            // The 'Close' Button
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

    override fun animate(currentThread: Thread, sleepTime: Long) {
        while (!currentThread.isInterrupted && this.isVisible) {
            screenTimeDisplay.text = dateFormat.format(Date(Twenty.timeSinceStart))

            sleepSafely(sleepTime) {
                // Instant abort
                return
            }
        }
    }

}
