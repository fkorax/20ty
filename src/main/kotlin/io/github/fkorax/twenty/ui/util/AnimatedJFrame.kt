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

package io.github.fkorax.twenty.ui.util

import io.github.fkorax.twenty.ui.UIConstants
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame

abstract class AnimatedJFrame(title: String) : JFrame(title), Runnable {
    var animationFPS: Long = UIConstants.DEFAULT_ANIMATION_FPS

    init {
        // An event listener which reacts if this dialog is shown
        this.addComponentListener(object : ComponentAdapter() {
            override fun componentShown(e: ComponentEvent?) {
                // Start an animation thread
                Thread(this@AnimatedJFrame).start()
                // (animation thread closes automatically)
            }
        })
    }

    // Runs the animation loop on a separate thread,
    // which is started if this window is shown, and closes itself
    override fun run() = animate(Thread.currentThread(), 1000L / animationFPS)

    protected abstract fun animate(currentThread: Thread, sleepTime: Long)

}
