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

import java.io.BufferedReader
import java.io.InputStreamReader

const val WINDOWS = "windows"
const val LINUX = "linux"
const val MACOS = "macos"
const val BSD = "bsd"

/**
 * Enriches the System properties with the following keys:
 * `"os.family"`, currently one of `"windows"`, `"macos"`, `"linux"`, `"bsd"`, or `null`;
 * on Linux systems:
 * `"lsb.description"`, the output of `lsb_release -ds`;
 * `"lsb.release"`, the output of `lsb_release -rs`.
 */
fun enrichSystemProperties() {
    if (System.getProperty("util.enriched") == "true") {
        error("System properties have already been enriched")
    }
    // First detect the OS family
    val os = System.getProperty("os.name")?.lowercase()
    if (os != null) {
        // TODO Expand to include other Unix-like systems (MINIX, etc.)
        val osFamily = when {
            "windows" in os -> WINDOWS
            "mac" in os -> MACOS
            "linux" in os -> LINUX
            "bsd" in os -> BSD
            else -> null
        }
        if (osFamily != null) {
            System.setProperty("os.family", osFamily)
            // Retrieve the LSB properties if the system is a Linux system
            if (osFamily == LINUX) {
                forEach(
                    Pair("lsb.release", "-rs"),         // Release
                    Pair("lsb.description", "-ds")      // Description
                ) { (key, arg) ->
                    getProcessOutput("lsb_release", arg)?.let { output -> System.setProperty(key, output) }
                }
            }
        }
        // Set enriched to true
        System.setProperty("util.enriched", "true")
    }
}

private fun getProcessOutput(vararg cmd: String): String? =
    try {
        ProcessBuilder(*cmd).start().inputStream.use { ins ->
            InputStreamReader(ins).use { inReader ->
                BufferedReader(inReader).readLine()
            }
        }
    }
    catch (e: Exception) {      // (Do not attempt to catch Errors)
        null
    }
