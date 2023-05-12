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

import io.github.fkorax.fusion.*
import io.github.fkorax.twenty.util.forEach
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * See also:
 * * [How to Use Swing Timers](https://docs.oracle.com/javase/tutorial/uiswing/misc/timer.html)
 * * [Lesson: Concurrency in Swing](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html)
 */
class HumanInterrupter {
    private val interruptWindow = InterruptWindow()

    companion object {
        /**
         * The constant factor of the window's dimensions relative
         * to the screen's dimension. This factor is the square root
         * of the window's relative size.
         * The factor is currently set to sqrt(0.33) ≈ 0.5745.
         */
        private const val RELATIVE_DIMENSION_FACTOR = 0.5745

        @JvmStatic
        fun testInterrupt() {
            val startTime = System.currentTimeMillis()
            testWindow(object : ComponentAdapter() {
                override fun componentHidden(e: ComponentEvent?) {
                    println("Interrupt window hidden after ${System.currentTimeMillis() - startTime} ms. " +
                            "Test successful?")
                }
            })
        }

        @JvmStatic
        fun testWindow(componentListener: ComponentListener) {
            val interrupter = HumanInterrupter()
            interrupter.interruptWindow.addComponentListener(componentListener)
            interrupter.interruptHuman(20)
        }

    }

    /**
     * Interrupts the human user both visually and with sounds.
     *
     * For now, the interrupt is not animated. In the future, a new parameter
     * `animated: Boolean` will be used to control whether an interrupt is animated
     * or not.
     */
    fun interruptHuman(durationInSeconds: Int) {
        if (interruptWindow.isVisible) {
            println("Interrupt Window is still visible. Not showing again.")
        }
        else {
            SwingUtilities.invokeLater {
                // Create the Swing Timer with the specified duration (in seconds)
                val timer = Timer(durationInSeconds*1000, ::onTimerFinished)
                timer.isRepeats = false

                // Set the counter display to the given duration of the interrupt
                // (In the future, the counter will once again be animated)
                // TODO Do this Locale-sensitively!!
                interruptWindow.counterText = "$durationInSeconds s"

                // Set size relative to screen size (33 % of screen size),
                // so the window appears in the same proportions as the screen,
                // which should give it a more grounded, harmonious appearance.
                // Note that HumanInterrupter has a minimum size,
                // which is the size after packing.
                (Toolkit.getDefaultToolkit().screenSize?.map { dim ->
                    (dim * RELATIVE_DIMENSION_FACTOR).roundToInt()
                } ?: interruptWindow.minimumSize).also(interruptWindow::setSize)
                interruptWindow.adjustDisplaySizes()
                // Center on screen
                interruptWindow.setLocationRelativeTo(null)
                // Show window
                interruptWindow.isVisible = true
                // Put the window to the front, so the user sees it
                // (but don't request focus!!)
                interruptWindow.toFront()

                // Beep
                // TODO Allow the user to turn this off; and later,
                //  to choose in a combo box between a System Beep, 20ty sounds, and a custom sound file
                //  (controlled here by a method parameter)
                playAlertSound()
                // TODO Allow notifications to be turned on/off
                // trayIcon?.displayMessage("Look outside", "Look 20 m into the distance.", TrayIcon.MessageType.NONE)

                // Starting with the beep, a timer is running for the
                // specified duration
                timer.start()
            }
        }
    }

    private fun playAlertSound() = Toolkit.getDefaultToolkit().beep()

    private fun onTimerFinished(e: ActionEvent?) {
        // An alert sound is only played when the window
        // is hidden by the timer (i.e. not closed by the user)
        // If the window is already no longer visible when the timer
        // finishes, nothing needs to be done.
        if (interruptWindow.isVisible) {
            // First, play an alert sound
            playAlertSound()
            // Hide the window
            interruptWindow.isVisible = false
        }
    }


    private class InterruptWindow : JFrame("20ty") {
        companion object {
            private val MESSAGE_SCALE_FACTOR = 2.0f
            private val COUNTER_SCALE_FACTOR = 6.0f
        }

        private val messageDisplay = JLabel("Look at least 20 m into the distance.").apply { scale(MESSAGE_SCALE_FACTOR) }
        private val counterDisplay = JLabel("///").apply { scale(COUNTER_SCALE_FACTOR) }

        var counterText: String
            get() = counterDisplay.text
            set(value) { counterDisplay.text = value }

        init {
            // Window configuration
            this.type = Type.NORMAL
            this.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
            this.isResizable = false

            val padSize = counterDisplay.font.size

            // Content Pane configuration
            this.contentPane = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
                border = EmptyBorder(padSize, padSize, padSize, padSize)
                background = Color.BLACK
            }

            forEach(messageDisplay, counterDisplay) {
                it.foreground = Color.WHITE
                it.alignmentX = CENTER_ALIGNMENT
            }

            verticalGlue()
            forEach(messageDisplay, Box.createVerticalStrut(messageDisplay.font.size), counterDisplay,
                this::add)
            verticalGlue()

            this.isUndecorated = true
            pack()
            this.minimumSize = this.size
        }

        fun adjustDisplaySizes() {
            // Get the window size and adjust:
            // The scale should depend on window width and height.
            val (minWidth, minHeight) = this.minimumSize
            // The font size should grow with the square root of the factors
            val sizeFactor: Double = sqrt(
                min(this.width / minWidth.toDouble(), this.height / minHeight.toDouble())
            )
            val baseSize = UIManager.getDefaults().getFont("Label.font").size2D
            println("Size Factor: $sizeFactor")
            messageDisplay.fontSize2D = (baseSize * MESSAGE_SCALE_FACTOR * sizeFactor).toFloat()
            counterDisplay.fontSize2D = (baseSize * COUNTER_SCALE_FACTOR * sizeFactor).toFloat()
        }

    }


}
