package io.github.fkorax.twenty

import io.github.fkorax.twenty.util.FromString
import io.github.fkorax.twenty.util.MissingPreferenceException
import io.github.fkorax.twenty.util.getOrNull
import io.github.fkorax.twenty.util.mapToHashMap
import java.util.prefs.Preferences
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties

private typealias EntryReference = KProperty1<Settings, Setting<*>>

data class Settings(
    @property:Entry val breakDuration: Setting.BreakSeconds,
    @property:Entry val sessionDuration: Setting.SessionMinutes,
    @property:Entry val nightLimitTime: Setting.LocalHmTime,
    @property:Entry val nightLimitActive: Setting.ActiveOn,
    @property:Entry val nightSessionDuration: Setting.SessionMinutes,
    @property:Entry val playAlertSound: Setting.Toggle,
    @property:Entry val lookAndFeel: Setting.LookAndFeel
) {

    private constructor(values: Map<EntryReference, Setting<*>?>) : this(
        breakDuration = values.getCasted(Settings::breakDuration),
        sessionDuration = values.getCasted(Settings::sessionDuration),
        nightLimitTime = values.getCasted(Settings::nightLimitTime),
        nightLimitActive = values.getCasted(Settings::nightLimitActive),
        nightSessionDuration = values.getCasted(Settings::nightSessionDuration),
        playAlertSound = values.getCasted(Settings::playAlertSound),
        lookAndFeel = values.getCasted(Settings::lookAndFeel)
    )

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    private annotation class Entry

    companion object {
        /**
         * A static lookup table of the properties of `Settings` marked with `@`[Entry] and their
         * [FromString]-implementing companion objects (the necessary "meta" information),
         * associated by their `String` [name][KProperty1.name].
         *
         * Structure of a lookup table entry:
         * ```
         * // String -> Pair(KProperty1, SCompanion)
         * Map.Entry<String, Pair<KProperty1<...>, SCompanion<*>>(
         *     key = property.name,
         *     value = (property, getSCompanion(property))
         * )
         * ```
         *
         * These are generated so that the static method [loadFrom] can work with
         * the private `Settings(Map<...>)` constructor using references to the properties
         * of Settings (e.g. `Settings::lookAndFeel`), without intermediary constant values
         * (without String constants, basically).
         */
        @Suppress("UNCHECKED_CAST")
        private val ENTRIES_META: Map<String, Pair<EntryReference, FromString<out Setting<*>>>> =
            Settings::class.memberProperties
                .filter { p -> p.annotations.any { it is Entry } }
                .map { p -> (p as EntryReference).let { prop -> Pair(prop, getCompanion(prop)) } }
                .associateBy { (p,_) -> p.name }

        /**
         * Retrieves the companion object for the given entry reference.
         * If the companion object did not implement the [FromString] interface,
         * a [RuntimeException] is thrown.
         */
        private fun getCompanion(prop: EntryReference): FromString<out Setting<*>> =
            prop.returnType.classifier.let { classifier ->
                if (classifier is KClass<*>) {
                    classifier.companionObjectInstance.let { o ->
                        @Suppress("UNCHECKED_CAST")
                        if (o is FromString<*>)
                            o as FromString<out Setting<*>>
                        else
                            throw RuntimeException(
                                "Companion of Settings property type ${prop.returnType}" +
                                        "does not implement Setting.SettingCompanion"
                            )
                    }
                }
                else {
                    throw RuntimeException(
                        "Property '${prop.name}' with type '${prop.returnType}'" +
                                "is not an instance of a class; classifier instead is '${prop.returnType.classifier}'"
                    )
                }
            }

        private inline fun <reified T, K, V> Map<K, V>.getCasted(key: K): T =
            this[key] as T

        /**
         * The static method [loadFrom] loads the settings from a given
         * [Preferences] instance, transforms and parses them into a `Map`,
         * passes it to the `private constructor(Map<...>)` of [Settings],
         * and returns the constructed [Settings] instance.
         *
         * First checks if all necessary keys are present, and then loads each key
         * by calling the relevant `fromString(String)` method of each [Setting] subclass.
         * If there is a problem loading a specific value for a key (i.e. an `Exception`
         * is thrown), the function aborts and returns `Success.failure(Exception)`.
         *
         * Despite first checking the keys, the loading is performed in a fault-tolerant way,
         * where a missing setting will result in an `UNDEFINED` value in the respective `SettingsChange` field,
         * meaning no change of whatever value was there before the settings where loaded.
         * (Keys can be present in the checks and still be missing due to unexpected bugs;
         *  after all, the `Preferences` *backend* is complex and platform-dependent.)
         */
        @JvmStatic
        fun loadFrom(prefs: Preferences): Result<Settings> = synchronized(prefs) {
            val prefKeys = prefs.keys()
            val missingKey = ENTRIES_META.keys.firstOrNull { k -> k !in prefKeys }
            return if (missingKey != null) {
                Result.failure(MissingPreferenceException("Missing entry in preferences: $missingKey", missingKey))
            }
            else {
                try {
                    Result.success(
                        Settings(
                            ENTRIES_META.mapToHashMap { (name, meta) ->
                                val (property, companion) = meta
                                Pair(
                                    property,
                                    prefs.getOrNull(name)?.let(companion::fromString)
                                )
                            }
                        )
                    )
                }
                catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

}
