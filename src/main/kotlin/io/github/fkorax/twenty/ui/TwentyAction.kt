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

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Icon

class TwentyAction
private constructor(
    name: String,
    icon: Icon?,
    shortDescription: String,
    private val actionPerformed: ActionPerformed
) : AbstractAction(name, icon) {

    constructor(name: String, icon: Icon?, shortDescription: String, actionPerformed: (ActionEvent?) -> Unit) :
            this(name, icon, shortDescription, ActionPerformed.Eventful(actionPerformed))

    constructor(name: String, icon: Icon?, shortDescription: String, actionPerformed: () -> Unit) :
            this(name, icon, shortDescription, ActionPerformed.Eventless(actionPerformed))

    init {
        putValue(Action.SHORT_DESCRIPTION, shortDescription)
    }

    override fun actionPerformed(e: ActionEvent?) {
        actionPerformed.actionPerformed(e)
    }

}

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
