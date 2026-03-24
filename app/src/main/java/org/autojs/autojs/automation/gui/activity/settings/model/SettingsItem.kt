package org.autojs.autojs.ui.modern.activity.settings.model

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SettingsItem

// 开关设置项
data class SettingsSwitchItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val isChecked: Boolean,
    val enabled: Boolean = true,
    val onCheckedChange: (Boolean) -> Unit
) : SettingsItem()

// 列表设置项
data class SettingsListItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val value: String,                    // 显示文本
    val selectedValue: Any? = null,       // 用于比较的选中值（枚举类型）
    val options: List<SettingsListOption<*>>,
    val onValueChange: (Any) -> Unit,
    // 简单导航：直接在 UI 中处理
    val neutralButtonText: String? = null,
    val onNeutralButtonClick: (() -> Unit)? = null
) : SettingsItem()

// 导航设置项
data class SettingsNavigationItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit
) : SettingsItem()

// 列表选项
data class SettingsListOption<T>(
    val title: String,
    val value: T
)