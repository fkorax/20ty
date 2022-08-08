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

package io.github.thomorl.twenty.ui

import io.github.thomorl.twenty.ui.util.AnimatedJFrame
import io.github.thomorl.twenty.ui.util.scaledJLabel
import io.github.thomorl.twenty.util.forEach
import io.github.thomorl.twenty.util.sleepSafely
import java.awt.Color
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.cos

class InterruptWindow : AnimatedJFrame("20ty") {
    private val contentBox = JPanel()
    private val message = scaledJLabel("Look at least 20 m into the distance.", 2.0f)
    private val counter = scaledJLabel("0", 6.0f)

    // These values are used to control the color animation
    companion object {
        private const val A = 255
        private const val T = 40_000
        private const val Ah = A / 2
        private const val a: Double = A / 2.0
        private const val b: Double = Math.PI / T   // Not 2*PI because the animation is not supposed to loop back
                                                    // to the start value (only from start to end)
    }

    init {
        // Window configuration
        this.type = Type.NORMAL

        val padSize = counter.font.size

        // Content Pane configuration
        this.contentPane = contentBox.apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
            border = EmptyBorder(padSize, padSize, padSize, padSize)
            background = Color.BLACK
        }

        forEach(message, counter) {
            it.foreground = Color.WHITE
            it.alignmentX = CENTER_ALIGNMENT
        }

        forEach(message, Box.createVerticalStrut(message.font.size), counter,
            this::add)

        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        isResizable = false

        pack()
    }

    override fun animate(currentThread: Thread, sleepTime: Long) {
        animate(currentThread, sleepTime, System.currentTimeMillis())
    }

    private tailrec fun animate(currentThread: Thread, sleepTime: Long, startTime: Long) {
        val elapsedTime: Long = System.currentTimeMillis() - startTime

        // Set the counter and the colors
        counter.text = "${(elapsedTime / 1000).let { if (it >= 21) 40-it else it }}"

        val fg: Int = (a * cos(b *elapsedTime)).toInt() + Ah
        val bg: Int = 255 - fg

        val fgColor = Color(fg, fg, fg)
        message.foreground = fgColor
        counter.foreground = fgColor
        contentBox.background = Color(bg, bg, bg)

        sleepSafely(sleepTime) {
            // Instant abort
            return
        }

        when {
            elapsedTime >= 40_000 -> {
                // Hide the window with a 1 s delay, abort
                sleepSafely(1000) {
                    // Instant abort
                    return
                }

                SwingUtilities.invokeLater {
                    this.isVisible = false
                }
                return
            }
            currentThread.isInterrupted -> {
                // Hide the window, abort
                SwingUtilities.invokeLater {
                    this.isVisible = false
                }
                return
            }
            !this.isVisible -> {
                // Abort
                return
            }
            // Otherwise, continue the recursion
            else -> animate(currentThread, sleepTime, startTime)
        }

    }

}
