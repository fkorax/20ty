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

@file:JvmName("BufferedImageUtils")

package io.github.fkorax.fusion

import java.awt.image.BufferedImage

/**
 * Source: [java - How do you clone a BufferedImage - StackOverflow](https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage)
 */
fun BufferedImage.deepCopy(): BufferedImage {
    val cm = this.colorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = this.copyData(raster.createCompatibleWritableRaster())
    return BufferedImage(cm, raster, isAlphaPremultiplied, null)
}
