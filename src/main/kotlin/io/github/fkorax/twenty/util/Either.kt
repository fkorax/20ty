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

package io.github.fkorax.twenty.util

/**
 * An algebraic sum type implementation: `Either<A, B> = A | B`.
 * The [Left] and [Right] types require the full type information (both `A` and `B`).
 *
 * In the future, sugar functions will be provided in case either `A` or `B`
 * are declared as `out` or `*` (in which case [Nothing] can be used for the right type
 * in `Left` and the left type in `Right`).
 */
sealed class Either<A, B> {

    data class Left<A, B>(val value: A) : Either<A, B>()

    data class Right<A, B>(val value: B) : Either<A, B>()

}
