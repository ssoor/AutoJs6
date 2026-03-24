package org.autojs.autojs.ui.modern.activity.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsSwitchItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsNavigationItem

@Composable
fun SettingsGroupCard(
    title: String,
    items: List<SettingsItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            items.forEach { item ->
                when (item) {
                    is SettingsSwitchItem -> SettingsSwitchItemContent(item)
                    is SettingsListItem -> SettingsListItemContent(item)
                    is SettingsNavigationItem -> SettingsNavigationItemContent(item)
                }
            }
        }
    }
}