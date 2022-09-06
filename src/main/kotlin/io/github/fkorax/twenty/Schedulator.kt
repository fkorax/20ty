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

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * The Schedulator (Scheduled Executor / Schedule Regulator).
 */
class Schedulator {
    private val executor = ScheduledThreadPoolExecutor(1)

    class Scheduled(internal val scheduledFuture: ScheduledFuture<*>,
                    internal val command: Runnable)

    fun schedule(command: Runnable, delay: Long): Scheduled =
        Scheduled(executor.scheduleWithFixedDelay(
            command, delay, delay, TimeUnit.MINUTES), command)

    fun reschedule(scheduled: Scheduled, newDelay: Long): Scheduled {
        // We could also have the main Twenty class do the rescheduling,
        // but why should we? It's easier to let another class handle it
        scheduled.scheduledFuture.cancel(true)
        return schedule(scheduled.command, newDelay)
    }

}
