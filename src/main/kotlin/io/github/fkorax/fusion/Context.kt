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

package io.github.fkorax.fusion

import java.awt.event.ActionEvent
import java.util.*
import javax.swing.Action
import javax.swing.Icon

interface Context {

    val resources: Resources

    val appLocale: Locale

    // FusionActions should be stored in a Context, so they can be updated
    // if the Context is updated

    // If the Locale of a Context changes, all sub-Contexts should be notified.

    fun action(name: Keyword, icon: Pair<Keyword, Int>?, shortDescription: Keyword?, actionPerformed: (ActionEvent?) -> Unit): Action =
        FusionAction(this, name, icon, shortDescription, actionPerformed)

    fun action(name: Keyword, icon: Pair<Keyword, Int>?, shortDescription: Keyword?, actionPerformed: () -> Unit): Action =
        FusionAction(this, name, icon, shortDescription, actionPerformed)

    // TODO The Context loads Resources with the current Locale,
    //  and reloads them if the Locale changes?

    fun getString(key: Keyword): String =
        resources.getString(key, appLocale)

    fun getIcon(key: Keyword, size: Int): Icon =
        resources.getIcon(key, size)

}
