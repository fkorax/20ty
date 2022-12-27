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

import io.github.fkorax.twenty.Setting
import io.github.fkorax.twenty.util.forEach
import javax.swing.*

// All SettingWidgets are capable of producing the current value
// of the Setting they control, but are responsible for nothing else.
// (This includes handling the text to display)
sealed interface SettingWidget<T : Setting<*>> {

    val value: T

    class LocalHmTime : SettingWidget<Setting.LocalHmTime>, JPanel() {
        // A custom component with two spinner fields,
        // from which the local HH:mm time can be constructed.
        private val hourSpinner = JSpinner(SpinnerNumberModel(19, 0, 24, 1))
        private val minuteSpinner = JSpinner(SpinnerNumberModel(0, 0, 60, 1))

        init {
            forEach(
                hourSpinner,
                JLabel(":"),
                minuteSpinner,
                this::add
            )
        }

        override val value: Setting.LocalHmTime
            get() = Setting.LocalHmTime(hourSpinner.value as Int, minuteSpinner.value as Int)

    }

    class Toggle(text: String? = null) : SettingWidget<Setting.Toggle>, JCheckBox(text) {
        override val value: Setting.Toggle
            get() = Setting.Toggle(this.isSelected)
    }

    class Selection<T>(items: Array<T>) : SettingWidget<T>, JComboBox<T>(items) where T : Enum<T>, T : Setting<*> {
        @Suppress("UNCHECKED_CAST")
        override val value: T
            get() = selectedItem as T
    }

    class IntRanged<T : Setting.Ranged<Int>>(min: Int, max: Int, val constructor: (Int) -> T) : SettingWidget<T>, JSlider(min, max) {

        init {
            this.paintLabels = true
            this.paintTicks = true
        }

        override val value: T
            get() = constructor(this.getValue())
    }

}
