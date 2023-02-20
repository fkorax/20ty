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

import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * An implementation of [Resources] for instances of [XApp].
 */
internal class AppResources internal constructor(
    private val appClass: Class<out XApp>,
    resPackageRoot: String,
    cacheDirectory: File
) : Resources {

    // The tree of locations
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

        class Cache(root: File) : LocTree<File>(root) {
            override fun ensureValidity(base: File): File =
                base.ensureDirectoryExists()

            override fun combine(base: File, relative: String): File =
                base.resolve(relative)
        }
    }

    private val resTree = LocTree.Res(resPackageRoot)

    private val cacheTree = LocTree.Cache(cacheDirectory)

    private val iconHotCache: MutableMap<Pair<String, Int>, Icon> = HashMap()
    override fun getIcon(name: String, size: Int): Icon {
        val parameters = Pair(name, size)
        // Check if the icon is in the hot cache
        //  If yes, return it.
        return iconHotCache[parameters] ?:
        //  If not: Load or Generate and (hot-)cache the image
        try {
            loadIconResource(
                appClass,
                resTree.icons + name,
                cacheTree.icons.resolve("$name-$size.png"),
                size
            ).also { icon -> iconHotCache[parameters] = icon }
        }
        catch (npe: NullPointerException) {
            throw NoSuchResourceException(name, ResourceType.ICON)
        }
        catch (te: TranscoderException) {
            throw ResourceException(
                name, ResourceType.ICON, "Problem while loading resource: $name", te
            )
        }
    }

}

/**
 * This is a version of a PNG transcoder which also caches the image being transcoded.
 * (A three-way transcoder)
 *
 * After the [writeImage] operation is done, a deep copy of the image is stored
 * as [cachedImage].
 *
 * Based in part on [How to use an SVG image in JavaFX](https://edencoding.com/svg-javafx/).
 *
 * See [Image Transcoder Tutorial](http://dev.cs.ovgu.de/java/batik-1.5/rasterizerTutorial.html)
 */
private class CachedPngTranscoder : PNGTranscoder() {
    lateinit var cachedImage: BufferedImage
        private set

    @Throws(TranscoderException::class)
    override fun writeImage(img: BufferedImage?, to: TranscoderOutput?) {
        // Fail as quickly as possible:
        // If the image is null, throw a TranscoderException
        try {
            img!!
        }
        catch (npe: NullPointerException) {
            throw TranscoderException("Image to be written is null.", npe)
        }

        super.writeImage(img, to)

        // Cache a copy of the image;
        // throw an Exception if it is null
        this.cachedImage = img.deepCopy()
    }
}

private fun loadIconResource(
    appClass: Class<*>,
    resourcePath: String,
    cacheFile: File,
    size: Int
): Icon =
    // Check if a cached file for this icon exists
    // TODO Plan for when the icon could not be loaded
    if (cacheFile.exists()) {
        // If yes, use that file
        // TODO Load the image data instantly
        //  to check for corruption
        ImageIcon(cacheFile.path)
        // TODO Check if the file is not outdated / needs to be updated (recached)
    }
    else {
        // If not, generate such an image from the SVG source
        // and cache it in one pass
        val svgTranscoder = CachedPngTranscoder()
        appClass.getResourceAsStream(resourcePath).use { inStream ->
            if (inStream == null) {
                throw NullPointerException("InputStream was null for: $resourcePath")
            }
            val transInput = TranscoderInput(inStream)
            svgTranscoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, size.toFloat())
            cacheFile.outputStream().use { outStream ->
                try {
                    val transOutput = TranscoderOutput(outStream)
                    svgTranscoder.transcode(transInput, transOutput)
                    outStream.flush()
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    svgTranscoder.transcode(transInput, null)
                }
            }
            // cachedImage is guaranteed to be not null,
            // and if it isn't, an Exception will be thrown
            ImageIcon(svgTranscoder.cachedImage)
        }
    }
