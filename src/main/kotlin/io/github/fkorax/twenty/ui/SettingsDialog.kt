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

package io.github.fkorax.twenty.ui

import io.github.fkorax.fusion.*
import io.github.fkorax.twenty.Setting
import io.github.fkorax.twenty.Settings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import kotlin.reflect.KClass

class SettingsDialog(owner: JFrame) : JDialog(owner, "Settings", true) {

    init {
        this.layout = BorderLayout()

        val settingsBox = Box.createVerticalBox().apply {
            border = emptyBorder(5)
            
            for (group in Settings.Entry.Group.values()) {
                verticalBox {
                    border = groupBorder(group.name)

                    processGroup(this, group)
                }
                add(Box.createRigidArea(Dimension(0,10)))
            }
        }
        add(JScrollPane(settingsBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
            BorderLayout.NORTH)

        val bottomPanel = panel(FlowLayout(FlowLayout.TRAILING)) {
            // TODO Esc = Exit
            // Three buttons: Cancel, OK, Apply
            val okButton = button("OK")
            // OK button is default button
            this@SettingsDialog.rootPane.defaultButton = okButton
            val cancelButton = button("Cancel")
            // Apply button is disabled by default
            val applyButton = button("Apply") {
                isEnabled = false
            }
        }
        add(bottomPanel, BorderLayout.SOUTH)

        this.pack()
        // The dialog is resizable,
        // but the minimum size is the size after packing
        this.minimumSize = this.size
    }

}

private fun processGroup(container: JComponent, group: Settings.Entry.Group) = Settings.ENTRIES_META_INFO.values
    .filter { it.annotation.group == group }
    .sortedBy { it.annotation.ordinal }
    .forEach { entryMetaInfo: Settings.Entry.MetaInfo ->
        val name = entryMetaInfo.reference.name
        entryMetaInfo.reference.returnType.classifier?.let { classifier ->
            if (classifier is KClass<*>) {
                container.settingWidget(name, classifier)
            }
            else {
                throw RuntimeException("Classifier is not a KClass<*>: $classifier")
            }
        } ?: throw NullPointerException("No classifier found")
    }

// PRIVATE EXTENSION FUNCTIONS

private fun JComponent.row(vararg components: Component) {
    // TODO Maybe LTR / RTL layouts should be taken into account here?
    val row = Box.createHorizontalBox()
    components.forEach(row::add)
    row.add(Box.createHorizontalGlue())
    this.add(row)
}

private fun groupBorder(title: String) = compoundBorder(
    titledBorder(title),
    emptyBorder(5)
)

// Generates a SettingWidget
private fun JComponent.settingWidget(name: String, klass: KClass<*>) {
    when (klass) {
        Setting.Toggle::class -> row(
            SettingWidget.Toggle(name)
        )
        Setting.LookAndFeel::class -> row(
            JLabel(name),
            SettingWidget.Selection(Setting.LookAndFeel.values())
        )
        Setting.SessionMinutes::class -> {
            row(JLabel(name))
            row(SettingWidget.IntRanged(
                Setting.SessionMinutes.MIN_MINUTES,
                Setting.SessionMinutes.MAX_MINUTES
            ) { Setting.SessionMinutes(it) }.apply {
                majorTickSpacing = 5
                minorTickSpacing = 1
                labelTable = createStandardLabels(5)
            }, JLabel("minutes"))
        }
        Setting.BreakSeconds::class -> {
            row(JLabel(name))
            row(SettingWidget.IntRanged(
                Setting.BreakSeconds.MIN_SECONDS,
                Setting.BreakSeconds.MAX_SECONDS
            ) {Setting.BreakSeconds(it) }.apply {
                majorTickSpacing = 5
                minorTickSpacing = 1
                labelTable = createStandardLabels(5)
            }, JLabel("seconds"))
        }
        Setting.LocalHmTime::class -> {
            row(JLabel(name), SettingWidget.LocalHmTime())
        }
        Setting.ActiveOn::class -> {
            row(JLabel(name), SettingWidget.ActiveOn())
        }
        else -> throw RuntimeException("Unsupported entry type: ${klass.simpleName}")
    }
}
