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

package io.github.fkorax.fusion

import java.awt.Menu
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.TrayIcon

inline fun TrayIcon.popupMenu(label: String, block: PopupMenu.() -> Unit = {}): PopupMenu =
    PopupMenu(label).also { pm -> this@popupMenu.popupMenu = pm }.apply(block)

inline fun PopupMenu.item(text: String, block: MenuItem.() -> Unit = {}): MenuItem =
    this.add(MenuItem(text).apply(block))

inline fun PopupMenu.menu(text: String, block: Menu.() -> Unit = {}): Menu =
    this.add(Menu(text).apply(block)) as Menu

inline fun Menu.item(text: String, block: MenuItem.() -> Unit = {}): MenuItem =
    this.add(MenuItem(text).apply(block))

fun PopupMenu.separator() = this.addSeparator()
