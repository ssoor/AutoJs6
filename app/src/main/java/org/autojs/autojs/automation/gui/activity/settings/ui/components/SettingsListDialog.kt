package org.autojs.autojs.ui.modern.activity.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListOption

@Composable
fun SettingsListDialog(
    title: String,
    options: List<SettingsListOption<*>>,
    currentValue: Any,
    selectedValue: Any? = null,  // 用于比较的选中值
    onDismiss: () -> Unit,
    onOptionSelected: (SettingsListOption<*>) -> Unit
) {
    // 使用selectedValue进行比较，如果没有则使用currentValue
    val compareValue = selectedValue ?: currentValue
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(options.size) { index ->
                    val option = options[index]
                    val isSelected = option.value == compareValue
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(option) }
                        )
                        Text(
                            text = option.title,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}