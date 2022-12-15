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
sealed interface SettingWidget<T : Setting<*>> {

    class LocalHmTime : SettingWidget<Setting.LocalHmTime>, JPanel() {

        init {
            // A custom component with two spinner fields,
            // from which the date can be constructed.
            forEach(
                JSpinner(SpinnerNumberModel(19, 0, 24, 1)),
                JLabel(":"),
                JSpinner(SpinnerNumberModel(0, 0, 60, 1)),
                this::add
            )
        }

    }

    class Toggle : SettingWidget<Setting.Toggle>, JCheckBox()

}
