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

import dorkbox.os.OS
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.util.*
import java.util.prefs.Preferences

/**
 * An abstract Fusion app.
 *
 * Named `XApp` in reference to the SwingLabs `JX`-prefixed components.
 *
 * Fusion is a lightweight framework integrating various aspects of
 * Swing. An `XApp` uses some of Fusion's standard capabilities.
 * Capabilities include:
 *  * Automatic storage of user preferences (settings)
 *  * Standard location for app-specific files
 *  * Resource management
 *  * Localization support
 *
 * ### Resource management
 * There is one unified directory structure across platforms.
 * * `<home>` is the user's home directory.
 * * `<package>` is the package of the `XApp`.
 *
 * Cache directory:
 * * `/<home>/.fusion/cache/<package>/`
 *
 * Data directory:
 * * `/<home>/.fusion/data/<package>/`
 *
 * ### Localization support
 *  * [Internationalization: Understanding Locale in the Java Platform](https://www.oracle.com/technical-resources/articles/javase/locale.html)
 *  * Baeldung:
 *      * [Internationalization and Localization in Java 8](https://www.baeldung.com/java-8-localization)
 *      * [A Guide to the ResourceBundle](https://www.baeldung.com/java-resourcebundle)
 */
abstract class FusionApp : Runnable, Context {
    companion object {

        private inline fun <R> wrapIOException(block: () -> R): R = try {
            block()
        }
        catch (e: IOException) {
            throw FusionException(e)
        }

        private val fusionDirectory: File = wrapIOException {
            System.getProperty("user.home")?.let { userHomePath ->
                File(userHomePath).resolve(".fusion/").also { directory ->
                    directory.ensureDirectoryExists()
                    // If the Fusion directory is not hidden and the platform is Windows:
                    if (!directory.isHidden && OS.isWindows()) {
                        // Hide directory, Windows style
                        Files.setAttribute(directory.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
                    }
                }
            } ?: throw IOException("Could not find user home directory")
        }

        private val cacheDirectory: File = wrapIOException {
            fusionDirectory.resolve("cache/").ensureDirectoryExists()
        }

        private val dataDirectory: File = wrapIOException {
            fusionDirectory.resolve("data/").ensureDirectoryExists()
        }

        internal fun getCacheDirectoryFor(appClass: Class<out FusionApp>): File =
            cacheDirectory.resolve(appClass.packageName.replace('.', '/')).ensureDirectoryExists()

        internal fun getDataDirectoryFor(appClass: Class<out FusionApp>): File =
            dataDirectory.resolve(appClass.packageName.replace('.', '/')).ensureDirectoryExists()

    }

    /**
     * The `Preferences` object to retrieve and store preferences in a user node.
     */
    protected val preferences: Preferences = Preferences.userNodeForPackage(this::class.java)

    /**
     * The `Resources` instance which manages the resources for instances of
     * the same app.
     */
    override val resources: Resources = Resources.getFor(this::class.java)

    protected val dataDirectory: File by lazy {
        getDataDirectoryFor(this::class.java)
    }

    override val appLocale: Locale
        get() = Locale.getDefault()

    protected fun getSubContext(name: String): Context = object : Context {
        override val resources: Resources = this@FusionApp.resources

        override val appLocale: Locale
            get() = this@FusionApp.appLocale
    }

    // TODO Add a flag to enforce uniqueness of an app. (So only one instance can run at a time)
    //  → Enforce inside JVM process and across via I/O locking
}
