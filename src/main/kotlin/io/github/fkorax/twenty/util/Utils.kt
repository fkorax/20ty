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

package io.github.fkorax.twenty.util

import java.util.prefs.Preferences

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

fun Preferences.getOrNull(key: String): String? = this.get(key, null)

inline fun <K, V, R, S> Map<K, V>.mapToHashMap(transform: (Map.Entry<K, V>) -> Pair<R, S>): Map<R, S> {
    val results = HashMap<R, S>(this.size)
    this.forEach { entry ->
        val (newKey, newValue) = transform(entry)
        results[newKey] = newValue
    }
    return results
}
