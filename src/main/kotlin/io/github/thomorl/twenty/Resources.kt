/*
 * Copyright (c) 2022  Thomas Orlando
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

package io.github.thomorl.twenty

import java.io.InputStream
import java.net.URL

object Resources {
    const val DEFAULT_ANIMATION_FPS = 20L

    // When the program is first started, save a timestamp so the total screen time can be calculated
    @JvmField
    val PROGRAM_START_TIME = System.currentTimeMillis()

    val timeSinceStart: Long get() = System.currentTimeMillis() - PROGRAM_START_TIME

    fun get(name: String): URL = this.javaClass.getResource(name)

    fun getAsStream(name: String): InputStream = this.javaClass.getResourceAsStream(name)

}
