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

    sealed interface Ranged<T : Comparable<T>> : Setting<T> {
        val minValue: T
        val maxValue: T
    }

    @JvmInline
    value class BreakSeconds(override val value: Int) : Ranged<Int> {
        companion object : SCompanion<BreakSeconds> {
            const val MIN_SECONDS = 20
            // Maximum duration time: 1 minute
            const val MAX_SECONDS = 60

            @JvmStatic
            val MIN_VALUE = BreakSeconds(MIN_SECONDS)
            @JvmStatic
            val MAX_VALUE = BreakSeconds(MAX_SECONDS)

            @JvmStatic
            override fun fromString(value: String): BreakSeconds =
                BreakSeconds(Integer.parseInt(value))
        }

        init {
            require(value >= MIN_SECONDS)
            require(value <= MAX_SECONDS)
        }

        override val minValue: Int
            get() = MIN_SECONDS
        override val maxValue: Int
            get() = MAX_SECONDS

        override fun toString(): String = value.toString()

    }

    @JvmInline
    value class SessionMinutes(override val value: Int) : Ranged<Int> {
        companion object : SCompanion<SessionMinutes> {
            const val MIN_MINUTES = 5
            const val MAX_MINUTES = 20

            @JvmStatic
            val MIN_VALUE = SessionMinutes(MIN_MINUTES)
            @JvmStatic
            val MAX_VALUE = SessionMinutes(MAX_MINUTES)

            @JvmStatic
            override fun fromString(value: String): SessionMinutes =
                SessionMinutes(Integer.parseInt(value))
        }

        init {
            require(value >= MIN_MINUTES)
            require(value <= MAX_MINUTES)
        }

        override val minValue: Int
            get() = MIN_MINUTES

        override val maxValue: Int
            get() = MAX_MINUTES

        override fun toString(): String = value.toString()

    }

    @JvmInline
    value class LocalHmTime(override val value: LocalTime) : Setting<LocalTime> {
        companion object : SCompanion<LocalHmTime> {
            @JvmStatic
            override fun fromString(value: String): LocalHmTime =
                LocalHmTime(LocalTime.parse(value))
        }

        constructor(hour: Int, minute: Int) : this(LocalTime.of(hour, minute))

        init {
            // Ensure that the LocalTime value conforms to
            // the HH:mm format (no seconds, no nanoseconds)
            require(value.second == 0) { "Non-zero second value in given LocalTime" }
            require(value.nano == 0) { "Non-zero nano value in given LocalTime" }
        }

        override fun toString(): String = value.toString()
    }

    @JvmInline
    value class ActiveOn(override val value: Set<DayOfWeek>) : Setting<Set<DayOfWeek>> {

        companion object : SCompanion<ActiveOn> {
            // TODO Test (with black box, that is, the fromString function itself)
            // WDL = Week Day Literal
            private const val WDL = "(MON|TUES|WEDNES|THURS|FRI|SATUR|SUN)DAY"

            private val SINGLE_REGEX = Regex.fromLiteral("\\[$WDL\\]")
            private val MULTI_REGEX = Regex.fromLiteral(
                "\\[($WDL,)+?($WDL)\\]"
            )

            @JvmStatic
            override fun fromString(value: String) = ActiveOn(when {
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

    @JvmInline
    value class Toggle(override val value: Boolean) : Setting<Boolean> {
        companion object : SCompanion<Toggle> {
            @JvmStatic
            override fun fromString(value: String): Toggle = when (value) {
                "true" -> Toggle(true)
                "false" -> Toggle(false)
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