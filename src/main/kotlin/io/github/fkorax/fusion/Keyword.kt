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

/**
 * A unique string keyword, inspired by the Clojure keyword type.
 *
 * While symbols contain a String which can be accessed,
 * but are unique, meaning two symbols are only equal if they
 * refer to the same object.
 */
@JvmInline
value class Keyword private constructor(val name: String) {
    companion object {
        private val KEYWORD_TABLE: MutableMap<String, Keyword> = HashMap()

        // Keywords are unique, meaning they can only be created once.
        // Validity is only checked upon creation
        operator fun get(name: String): Keyword =
            KEYWORD_TABLE[name] ?: if (isValidKeywordString(name)) {
                Keyword(name).also { newSymbol -> KEYWORD_TABLE[name] = newSymbol }
            }
            else {
                throw IllegalArgumentException("Invalid keyword string: $name")
            }

        /**
         * A keyword string, like a symbol, begins with a non-numeric
         * character and can contain alphanumeric characters and *, +, !, -,
         * _, and ?.
         */
        private fun isValidKeywordString(string: String): Boolean {
            // A quick parser
            if (string.isEmpty() || string[0].isDigit()) {
                return false
            }
            else {
                for (cp in string.codePoints()) {
                    // Idealized:
                    // !(cp in 48..57 || cp in 65..90 || cp in 97..122
                    //                                || cp in setOf(33, 42, 43, 45, 63, 95))
                    // Optimized:
                    if (
                        (cp < 48
                                // cp !in {!, *, +, -}
                                && !(cp == 33 || cp == 42 || cp == 43 || cp == 45))
                        || (cp > 57
                                // cp !in {?}
                                && cp != 63
                                // cp !in next range
                                && cp < 65)
                        || (cp > 90
                                // cp !in {_}
                                && cp != 95
                                // cp !in next range
                                && cp < 97)
                        || cp > 122) {
                        return false
                    }
                }
                return true
            }
        }
    }

    override fun toString(): String {
        return ":$name"
    }

}
