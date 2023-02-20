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

import javax.swing.Icon

interface Resources {
    companion object {
        // A map of cached (already-created) Resources instances,
        // indexed by Java Class
        private val resourcesInstances: MutableMap<Class<out XApp>, Resources> = HashMap()

        internal fun getFor(appClass: Class<out XApp>): Resources =
            // If a cached Resources instance already exists for that app,
            // return it
            resourcesInstances[appClass] ?:
            // Create a new Resources instance for
            // the specified app class
            AppResources(
                appClass,
                "/${appClass.packageName.replace('.', '/')}/res/",
                XApp.getCacheDirectoryFor(appClass).resolve("res/").ensureDirectoryExists()
            ).also { newInstance ->
                // Cache the newly created Resources instance
                resourcesInstances[appClass] = newInstance
            }
    }

    fun getIcon(name: String, size: Int): Icon

}
