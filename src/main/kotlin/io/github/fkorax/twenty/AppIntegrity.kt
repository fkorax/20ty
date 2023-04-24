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
import io.github.fkorax.twenty.ui.BackgroundIndicator
import java.awt.Component
import javax.swing.JOptionPane
import kotlin.system.exitProcess

/**
 * An integrity subsystem designed to handle errors in a uniform manner
 * and ensure all resources are freed upon shutdown.
 * Needs to be explicitly called to be active – does not execute code by itself.
 * All `AppIntegrity` methods are thread-safe, i.e. `synchronized`.
 */
object AppIntegrity {
    private var shutdownHookInstalled: Boolean = false

    fun installShutdownHook() = synchronized(this) {
        if (shutdownHookInstalled)
            throw IllegalStateException("Shutdown hook has already been installed")
        else {
            // Install a shutdown hook to ensure the SystemTray is shut down correctly
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    SystemTray.get(BackgroundIndicator.SYSTEM_TRAY_NAME).shutdown()
                } catch (t: Throwable) {
                    System.err.println("Error while shutting down SystemTray: $t")
                    System.err.println("(rethrown)")
                    throw t
                }
            })
            shutdownHookInstalled = true
        }
    }

    fun shutdown(dueToProblem: Boolean): Nothing = synchronized(this) {
        exitProcess(if (dueToProblem) 1 else 0)
    }

    fun displayThrowable(throwable: Throwable, uiAnchor: Component? = null) = synchronized(this) {
        // First print stack trace
        throwable.printStackTrace()
        // Notify user
        try {
            JOptionPane.showMessageDialog(
                uiAnchor,
                throwable::class.qualifiedName + ": ${throwable.message}.\nCaused by: ${throwable.cause}",
                throwable::class.simpleName,
                JOptionPane.ERROR_MESSAGE
            )
        }
        catch (t: Throwable) {
            // Something is deeply wrong.
            System.err.println("Encountered error while displaying error message.\n(rethrown)")
            throw t
        }
    }

}
