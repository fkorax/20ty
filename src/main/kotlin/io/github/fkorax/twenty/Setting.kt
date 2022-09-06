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

import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.time.Duration

sealed interface Setting<T> {
    val value: T

    @JvmInline
    value class BreakDuration(override val value: Duration) : Setting<Duration> {
        companion object {
            const val MINIMUM_SECONDS = 20
        }
        init {
            require(value.inWholeSeconds >= MINIMUM_SECONDS)
        }
    }

    @JvmInline
    value class SessionDuration(override val value: Duration) : Setting<Duration> {
        companion object {
            const val MAXIMUM_MINUTES = 20
        }
        init {
            require(value.inWholeMinutes <= MAXIMUM_MINUTES)
        }
    }

    @JvmInline
    value class NightLimitTime(override val value: LocalTime) : Setting<LocalTime>

    @JvmInline
    value class NightLimitActivity(override val value: Set<DayOfWeek>) : Setting<Set<DayOfWeek>>

    @JvmInline
    value class PlayAlertSound(override val value: Boolean) : Setting<Boolean>

    enum class LookAndFeel : Setting<LookAndFeel> {
        SYSTEM,
        METAL,
        NIMBUS;

        override val value: LookAndFeel
            get() = this
    }
}