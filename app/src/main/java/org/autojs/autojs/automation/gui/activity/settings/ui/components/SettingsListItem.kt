package org.autojs.autojs.ui.modern.activity.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListItem
import androidx.compose.foundation.clickable

@Composable
fun SettingsListItemContent(
    item: SettingsListItem,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = item.title,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
            item.description?.let {
                Text(
                    text = it,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.value,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
            
            // 简单导航：直接在 UI 中处理
            item.neutralButtonText?.let {
                TextButton(onClick = { item.onNeutralButtonClick?.invoke() }) {
                    Text(it)
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    if (showDialog) {
        SettingsListDialog(
            title = item.title,
            options = item.options,
            currentValue = item.value,
            selectedValue = item.selectedValue,
            onDismiss = { showDialog = false },
            onOptionSelected = { option ->
                showDialog = false
                @Suppress("UNCHECKED_CAST")
                item.onValueChange(option.value as Any)
            }
        )
    }
}