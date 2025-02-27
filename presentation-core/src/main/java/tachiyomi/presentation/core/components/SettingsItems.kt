package tachiyomi.presentation.core.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.DisabledByDefault
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tachiyomi.core.preference.TriState
import tachiyomi.presentation.core.theme.header

object SettingsItemsPaddings {
    val Horizontal = 24.dp
    val Vertical = 10.dp
}

@Composable
fun HeadingItem(
    @StringRes labelRes: Int,
) {
    HeadingItem(stringResource(labelRes))
}

@Composable
fun HeadingItem(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.header,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = SettingsItemsPaddings.Vertical),
    )
}

@Composable
fun IconItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    BaseSettingsItem(
        label = label,
        widget = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
    )
}

@Composable
fun SortItem(
    label: String,
    sortDescending: Boolean?,
    onClick: () -> Unit,
) {
    val arrowIcon = when (sortDescending) {
        true -> Icons.Default.ArrowDownward
        false -> Icons.Default.ArrowUpward
        null -> null
    }

    BaseSettingsItem(
        label = label,
        widget = {
            if (arrowIcon != null) {
                Icon(
                    imageVector = arrowIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        onClick = onClick,
    )
}

@Composable
fun CheckboxItem(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    BaseSettingsItem(
        label = label,
        widget = {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
            )
        },
        onClick = onClick,
    )
}

@Composable
fun RadioItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    BaseSettingsItem(
        label = label,
        widget = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        onClick = onClick,
    )
}

@Composable
fun SliderItem(
    label: String,
    min: Int = 0,
    max: Int,
    value: Int,
    valueText: String,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsItemsPaddings.Horizontal,
                vertical = SettingsItemsPaddings.Vertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(modifier = Modifier.weight(0.5f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(valueText)
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            modifier = Modifier.weight(1.5f),
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min,
        )
    }
}

@Composable
fun SelectItem(
    label: String,
    options: Array<out Any?>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = SettingsItemsPaddings.Vertical),
            label = { Text(text = label) },
            value = options[selectedIndex].toString(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, text ->
                DropdownMenuItem(
                    text = { Text(text.toString()) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun TriStateItem(
    label: String,
    state: TriState,
    enabled: Boolean = true,
    onClick: ((TriState) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .clickable(
                enabled = enabled && onClick != null,
                onClick = {
                    when (state) {
                        TriState.DISABLED -> onClick?.invoke(TriState.ENABLED_IS)
                        TriState.ENABLED_IS -> onClick?.invoke(TriState.ENABLED_NOT)
                        TriState.ENABLED_NOT -> onClick?.invoke(TriState.DISABLED)
                    }
                },
            )
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = SettingsItemsPaddings.Vertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val stateAlpha = if (enabled && onClick != null) 1f else ContentAlpha.disabled

        Icon(
            imageVector = when (state) {
                TriState.DISABLED -> Icons.Rounded.CheckBoxOutlineBlank
                TriState.ENABLED_IS -> Icons.Rounded.CheckBox
                TriState.ENABLED_NOT -> Icons.Rounded.DisabledByDefault
            },
            contentDescription = null,
            tint = if (!enabled || state == TriState.DISABLED) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = stateAlpha)
            } else {
                when (onClick) {
                    null -> MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
                    else -> MaterialTheme.colorScheme.primary
                }
            },
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = stateAlpha),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun TextItem(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = 4.dp),
        label = { Text(text = label) },
        value = value,
        onValueChange = onChange,
        singleLine = true,
    )
}

@Composable
private fun BaseSettingsItem(
    label: String,
    widget: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = SettingsItemsPaddings.Vertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        widget(this)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
