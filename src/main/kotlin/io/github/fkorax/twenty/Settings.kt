package io.github.fkorax.twenty

import io.github.fkorax.twenty.util.MissingPreferenceException
import io.github.fkorax.twenty.util.getOrNull
import io.github.fkorax.twenty.util.mapToHashMap
import java.util.prefs.Preferences
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties

private typealias SettingKProp1 = KProperty1<Settings, Setting<*>>

class Settings(
    @property:SettingProperty
    val breakDuration: Setting.BreakDuration,
    @property:SettingProperty
    val sessionDuration: Setting.SessionDuration,
    @property:SettingProperty
    val nightLimitTime: Setting.LocalHmTime,
    @property:SettingProperty
    val nightLimitActive: Setting.ActiveOn,
    @property:SettingProperty
    val nightSessionDuration: Setting.SessionDuration,
    @property:SettingProperty
    val playAlertSound: Setting.Toggle,
    @property:SettingProperty
    val lookAndFeel: Setting.LookAndFeel
) {

    private constructor(values: Map<SettingKProp1, Setting<*>?>) : this(
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
    private annotation class SettingProperty()

    /**
     * The companion object includes a number of static lookup tables,
     * which contain Reflection API results introspecting the actual [Settings] class.
     * These are generated so that the static method [loadFrom] can work in tandem with
     * the private `Settings(Map<...>)` constructor using only references to the properties
     * of Settings (e.g. `Settings::lookAndFeel`), and without requiring intermediary constant values
     * (without string constants, basically).
     */
    companion object {
        /**
         * A static lookup table of all [KProperty1] instances of `Settings` properties
         * annotated with `@`[SettingProperty], associated by their `String` [name][KProperty1.name].
         *
         * Structure of a lookup table entry:
         * ```
         * Map.Entry<String, KProperty1<...>>(
         *     key = property.name,
         *     value = property
         * )
         * ```
         */
        @Suppress("UNCHECKED_CAST")
        private val SETTING_PROPERTIES: Map<String, SettingKProp1> =
            Settings::class.memberProperties
                .filter { p -> p.annotations.any { it is SettingProperty } }
                .map { p -> p as SettingKProp1 }
                .associateBy { p -> p.name }

        /**
         * A static lookup table which associates each [KProperty1] from [SETTING_PROPERTIES]
         * with its corresponding companion object.
         * If the companion object did not implement [Setting.SCompanion.fromString]),
         * a [RuntimeException] is thrown.
         */
        private val COMPANIONS: Map<SettingKProp1, Setting.SCompanion<*>> = SETTING_PROPERTIES.values.associateWith {
            p -> p.returnType.classifier.let {
                if (it is KClass<*>) {
                    it.companionObjectInstance.let { o ->
                        if (o is Setting.SCompanion<*>) {
                            o
                            /*
                             This method used to return the actual
                             fromString: (String) -> Setting<*> method,
                             but since all companion objects automatically implement
                             the SCompanion interface by default, this is unnecessary,
                             and the object can just be returned by default.
                             */
                        }
                        else
                            throw RuntimeException(
                                "Companion of Settings property type ${p.returnType}" +
                                        "does not implement Setting.SettingCompanion"
                            )
                    }
                }
                else {
                    throw RuntimeException(
                        "@SettingProperty '${p.name}' with type '${p.returnType}'" +
                                "is not an instance of a class; classifier instead is '${p.returnType.classifier}'"
                    )
                }
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
            val missingKey = SETTING_PROPERTIES.keys.firstOrNull { k -> k !in prefKeys }
            return if (missingKey != null) {
                Result.failure(MissingPreferenceException("Missing entry in preferences: $missingKey", missingKey))
            }
            else {
                try {
                    Result.success(
                        Settings(
                            SETTING_PROPERTIES.mapToHashMap { (name, property) ->
                                // For each entry in SETTING_PROPERTIES:
                                // Create a pair with the KProperty1 object of class Settings
                                // and the actual loaded value:
                                // (name, property) |-> (property, fromString(prefs[name]))
                                Pair(
                                    property,
                                    prefs.getOrNull(name)?.let(COMPANIONS[property]!!::fromString)
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
