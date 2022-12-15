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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

sealed interface Setting<T> {
    val value: T

    /**
     * The sealed interface to be implemented by all companion objects
     * of children of [Setting].
     */
    sealed interface SCompanion<T : Setting<*>> {
        /**
         * This function has a default implementation so the compiler doesn't crash
         * when it is referenced from instances of `SCompanion`:
         * ```
         * if (o is Setting.SCompanion<*>) {
         *      // This reference crashes the compiler. Why?
         *      o::fromString
         * }
         * ```
         */
        fun fromString(value: String): T =
            throw UnsupportedOperationException("String parser not implemented")
    }

    @JvmInline
    value class BreakDuration(override val value: Duration) : Setting<Duration> {
        companion object : SCompanion<BreakDuration> {
            const val MIN_SECONDS = 20
            // TODO Add a maximum

            @JvmStatic
            val MIN_VALUE = BreakDuration(MIN_SECONDS.seconds)

            @JvmStatic
            override fun fromString(value: String): BreakDuration =
                BreakDuration(Duration.parse(value))
        }

        init {
            require(value.inWholeSeconds >= MIN_SECONDS)
        }

        override fun toString(): String = value.toString(DurationUnit.SECONDS, 0)

    }

    @JvmInline
    value class SessionDuration(override val value: Duration) : Setting<Duration> {
        companion object : SCompanion<SessionDuration> {
            const val MAX_MINUTES = 20
            // TODO Add a minimum

            @JvmStatic
            val MAX_VALUE = SessionDuration(MAX_MINUTES.minutes)

            @JvmStatic
            override fun fromString(value: String): SessionDuration =
                SessionDuration(Duration.parse(value))
        }

        init {
            require(value.inWholeMinutes <= MAX_MINUTES)
        }

        override fun toString(): String = value.toString(DurationUnit.MINUTES, 0)

    }

    // TODO Make more abstract and rename to LocalHmTime
    @JvmInline
    value class NightLimitTime(override val value: LocalTime) : Setting<LocalTime> {
        companion object : SCompanion<NightLimitTime> {
            @JvmStatic
            override fun fromString(value: String): NightLimitTime =
                NightLimitTime(LocalTime.parse(value))
        }

        override fun toString(): String = value.toString()
    }

    // Make more abstract and rename to ActiveOn
    @JvmInline
    value class NightLimitActive(override val value: Set<DayOfWeek>) : Setting<Set<DayOfWeek>> {

        companion object : SCompanion<NightLimitActive> {
            // TODO Test (with black box, that is, the fromString function itself)
            // WDL = Week Day Literal
            private const val WDL = "(MON|TUES|WEDNES|THURS|FRI|SATUR|SUN)DAY"

            private val SINGLE_REGEX = Regex.fromLiteral("\\[$WDL\\]")
            private val MULTI_REGEX = Regex.fromLiteral(
                "\\[($WDL,)+?($WDL)\\]"
            )

            @JvmStatic
            override fun fromString(value: String) = NightLimitActive(when {
                value == "{}" -> emptySet()
                value.matches(SINGLE_REGEX) -> setOf(DayOfWeek.valueOf(value.slice(1 until value.length)))
                value.matches(MULTI_REGEX) -> {
                    TODO("Take the String apart!")
                }
                else -> throw IllegalArgumentException("String cannot be parsed: $value")
            })
        }

        override fun toString(): String =
            if (value.isEmpty())
                "{}"
            else {
                val sb = StringBuilder("{")
                // Apparently, synchronizing on the StringBuilder
                // offers marginal performance benefits
                synchronized(sb) {
                    value.forEach {
                        sb.append(it)
                        sb.append(',')
                    }
                    // Replace the last comma with a closing bracket
                    sb.setCharAt(sb.length - 1, '}')
                    sb.toString()
                }
            }

    }

    // TODO Make more abstract/general and rename to Toggle
    @JvmInline
    value class PlayAlertSound(override val value: Boolean) : Setting<Boolean> {
        companion object : SCompanion<PlayAlertSound> {
            @JvmStatic
            override fun fromString(value: String): PlayAlertSound = when (value) {
                "true" -> PlayAlertSound(true)
                "false" -> PlayAlertSound(false)
                else -> throw IllegalArgumentException("String value has to be either \"true\" or \"false\": $value")
            }
        }

        override fun toString(): String = if (value) "true" else "false"
    }

    enum class LookAndFeel : Setting<LookAndFeel> {
        SYSTEM,
        CROSS_PLATFORM,
        METAL,
        NIMBUS;

        companion object : SCompanion<LookAndFeel> {
            @JvmStatic
            override fun fromString(value: String) =
                valueOf(value)
        }

        override val value: LookAndFeel
            get() = this
    }

}