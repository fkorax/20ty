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

import java.io.File
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * An implementation of [Resources] for instances of [FusionApp].
 *
 * Resources are stored in the `res/` subdirectory of the `App`'s package
 */
internal class AppResources internal constructor(
    private val appClass: Class<out FusionApp>,
    private val resPackageRoot: String,
    cacheDirectory: File
) : Resources {

    // The tree of locations
    // TODO Only used for icons
    private sealed class LocTree<T>(root: T) {
        @Suppress("LeakingThis")
        val root: T  = ensureValidity(root)
        val icons: T = combineAndEnsure(root, "icons/")

        protected abstract fun ensureValidity(base: T): T

        protected abstract fun combine(base: T, relative: String): T

        fun combineAndEnsure(base: T, relative: String): T =
            ensureValidity(combine(base, relative))

        class Res(root: String) : LocTree<String>(root) {
            override fun ensureValidity(base: String): String =
                base.trim().let { if (!it.endsWith('/')) "$it/" else it }

            override fun combine(base: String, relative: String): String =
                base + relative
        }
    }

    private val resTree = LocTree.Res(resPackageRoot)

    private val cachedIcons: MutableMap<String, Icon> = HashMap()

    @Throws(MissingResourceException::class)
    override fun getIcon(key: Keyword, size: Int): Icon {
        val name = key.name
        // Convert the parameters to Strings and just use those with
        // a unified format, and unify the hot caches
        val parameters = "$name-$size"
        // Check if the icon is in the hot cache
        //  If yes, return it.
        return cachedIcons[parameters] ?:
            //  If not: Load and (hot-)cache the image
            // Resolve the file in question:
            // Look for files with an image extension (.png)
            // TODO Look for icons with the specific size?
            appClass.getResource(resTree.icons + name + ".png")?.let { resourceUrl ->
                ImageIcon(resourceUrl).also { icon -> cachedIcons[parameters] = icon }
            } ?: throw MissingResourceException(
                    "No icon resource for specified key", this::class.simpleName, key.toString())
    }

    private val resourceBundles: MutableMap<Locale, ResourceBundle> = HashMap()

    override fun getString(key: Keyword, locale: Locale): String {
        // We tap into the ResourceBundle API to manage these values
        val stringBundle = getStringResourceBundle(locale)
        return stringBundle.getString(key.name)
    }

    private fun getStringResourceBundle(locale: Locale) =
        // Get the ResourceBundle and cache it
        resourceBundles[locale] ?:
        ResourceBundle.getBundle("${resPackageRoot}values/strings".replace('/', '.').substring(1), locale, appClass.classLoader).also {
            bundle -> resourceBundles[locale] = bundle }

}
