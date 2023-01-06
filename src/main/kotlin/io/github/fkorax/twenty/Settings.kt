package io.github.fkorax.twenty

import io.github.fkorax.twenty.Settings.Entry.Group
import io.github.fkorax.twenty.util.*
import java.util.prefs.Preferences
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties

private typealias EntryReference = KProperty1<Settings, Setting<*>>

data class Settings(
    @property:Entry(Group.DAY, 1) val sessionDuration: Setting.SessionMinutes,
    @property:Entry(Group.DAY, 2) val breakDuration: Setting.BreakSeconds,
    @property:Entry(Group.NIGHT, 1) val nightLimitTime: Setting.LocalHmTime,
    @property:Entry(Group.NIGHT, 2) val nightLimitActive: Setting.ActiveOn,
    @property:Entry(Group.NIGHT, 3) val nightSessionDuration: Setting.SessionMinutes,
    @property:Entry(Group.APP_BEHAVIOR, 1) val lookAndFeel: Setting.LookAndFeel,
    @property:Entry(Group.APP_BEHAVIOR, 2) val playAlertSound: Setting.Toggle,
) {

    private constructor(values: Map<EntryReference, Setting<*>?>) : this(
        sessionDuration = values.getCasted(Settings::sessionDuration),
        breakDuration = values.getCasted(Settings::breakDuration),
        nightLimitTime = values.getCasted(Settings::nightLimitTime),
        nightLimitActive = values.getCasted(Settings::nightLimitActive),
        nightSessionDuration = values.getCasted(Settings::nightSessionDuration),
        lookAndFeel = values.getCasted(Settings::lookAndFeel),
        playAlertSound = values.getCasted(Settings::playAlertSound),
    )

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.PROPERTY)
    annotation class Entry(val group: Group, val ordinal: Int) {
        enum class Group {
            /**
             * Used for the standard/day settings,
             * before the night limit is reached
             * (if activated).
             * @see [Group.NIGHT]
             */
            DAY,
            /**
             * Used for settings which control the
             * behavior of the app after the night limit
             * is reached.
             * @see [Group.DAY]
             */
            NIGHT,
            /**
             * Used for settings which relate to the
             * *general* Appearance & Behavior of the app.
             */
            APP_BEHAVIOR
        }

        data class MetaInfo(
            val reference: EntryReference,
            val annotation: Entry,
            val stringConverter: FromString<out Setting<*>>
        )
    }

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
        @JvmField
        val ENTRIES_META_INFO: Map<String, Entry.MetaInfo> = Settings::class.memberProperties.mapNotNull { p ->
            val entryAnnotation: Entry? = p.annotations.find { it is Entry } as Entry?
            entryAnnotation?.let {
                val er = p as EntryReference
                Entry.MetaInfo(er, entryAnnotation, getCompanion(er))
            }
        }
        .associateBy { mi -> mi.reference.name }

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
            val missingKey = ENTRIES_META_INFO.keys.firstOrNull { k -> k !in prefKeys }
            return if (missingKey != null) {
                Result.failure(MissingPreferenceException("Missing entry in preferences: $missingKey", missingKey))
            }
            else {
                try {
                    Result.success(
                        Settings(
                            ENTRIES_META_INFO.mapToHashMap { (name, meta) ->
                                Pair(
                                    meta.reference,
                                    prefs.getOrNull(name)?.let(meta.stringConverter::fromString)
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
