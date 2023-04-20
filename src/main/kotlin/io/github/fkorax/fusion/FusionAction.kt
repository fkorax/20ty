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
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.Action

/**
 * An immutable implementation of [Action], using [Keyword] keys.
 */
class FusionAction
private constructor(
    private val context: Context,
    val nameKey: Keyword,
    val iconKey: Keyword?,
    val iconSize: Int,
    val shortDescriptionKey: Keyword?,
    private val actionPerformed: ActionPerformed
) : AbstractAction(), Context by context {

    private sealed interface ActionPerformed : ActionListener {

        override fun actionPerformed(e: ActionEvent?)

        @JvmInline
        value class Eventful(val delegate: (ActionEvent?) -> Unit) : ActionPerformed {
            override fun actionPerformed(e: ActionEvent?) = delegate(e)
        }

        @JvmInline
        value class Eventless(val delegate: () -> Unit) : ActionPerformed {
            override fun actionPerformed(e: ActionEvent?) = delegate()
        }

    }

    private constructor(context: Context, name: Keyword, icon: Pair<Keyword, Int>?, shortDescription: Keyword?, actionPerformed: ActionPerformed) :
            this(context, name, icon?.first, icon?.second ?: -1, shortDescription, actionPerformed)

    constructor(
        context: Context, name: Keyword, icon: Pair<Keyword, Int>?, shortDescription: Keyword?, actionPerformed: (ActionEvent?) -> Unit) :
            this(context, name, icon, shortDescription, ActionPerformed.Eventful(actionPerformed))

    constructor(
        context: Context, name: Keyword, icon: Pair<Keyword, Int>?, shortDescription: Keyword?, actionPerformed: () -> Unit) :
            this(context, name, icon, shortDescription, ActionPerformed.Eventless(actionPerformed))

    init {
        update()
    }

    override fun putValue(key: String?, value: Any?) =
        throw UnsupportedOperationException("FusionAction is immutable.")

    override fun actionPerformed(e: ActionEvent?) {
        actionPerformed.actionPerformed(e)
    }

    // TODO Implement update() method for Locale
    fun update() {
        super.putValue(Action.NAME, getString(this.nameKey))
        super.putValue(Action.SHORT_DESCRIPTION, shortDescriptionKey?.let(::getString))
        super.putValue(Action.SMALL_ICON, iconKey?.let { key -> getIcon(key, iconSize) })
    }

}
