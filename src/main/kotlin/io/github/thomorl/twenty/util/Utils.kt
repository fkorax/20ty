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

package io.github.thomorl.twenty.util

inline fun <T> forEach(a: T, b: T, block: (T) -> Unit) {
    block(a)
    block(b)
}

inline fun <T> forEach(a: T, b: T, c: T, block: (T) -> Unit) {
    block(a)
    block(b)
    block(c)
}

inline fun <T> forEach(a: T, b: T, c: T, d: T, block: (T) -> Unit) {
    block(a)
    block(b)
    block(c)
    block(d)
}

inline fun sleepSafely(millis: Long, onInterrupt: (e: InterruptedException) -> Unit) {
    try {
        Thread.sleep(millis)
    }
    catch (e: InterruptedException) {
        onInterrupt(e)
    }
}
