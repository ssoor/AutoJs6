package org.autojs.autojs.ui.modern.activity.settings.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.autojs.autojs.ui.modern.activity.settings.model.ServiceIntent
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsSwitchItem
import org.autojs.autojs.ui.modern.activity.settings.ui.components.SettingsGroupCard
import org.autojs.autojs.ui.modern.activity.settings.viewmodel.ServiceViewModel

/**
 * 服务设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    viewModel: ServiceViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 显示Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("服务") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 无障碍服务
            item {
                AccessibilityServiceCard(
                    uiState = uiState,
                    onToggleA11yService = { enabled ->
                        viewModel.handleIntent(ServiceIntent.ToggleA11yService(enabled))
                    },
                    onToggleRootAutoEnable = { enabled ->
                        viewModel.handleIntent(ServiceIntent.ToggleRootAutoEnable(enabled))
                    },
                    onToggleSecureSettingsAutoEnable = { enabled ->
                        viewModel.handleIntent(ServiceIntent.ToggleSecureSettingsAutoEnable(enabled))
                    },
                    onToggleStableMode = { enabled ->
                        viewModel.handleIntent(ServiceIntent.ToggleStableMode(enabled))
                    }
                )
            }
            
            // 前台服务
            item {
                ForegroundServiceCard(
                    isEnabled = uiState.foregroundServiceEnabled,
                    onToggle = { enabled ->
                        viewModel.handleIntent(ServiceIntent.ToggleForegroundService(enabled))
                    }
                )
            }
        }
    }
}

/**
 * 无障碍服务卡片
 */
@Composable
private fun AccessibilityServiceCard(
    uiState: org.autojs.autojs.ui.modern.activity.settings.model.ServiceUiState,
    onToggleA11yService: (Boolean) -> Unit,
    onToggleRootAutoEnable: (Boolean) -> Unit,
    onToggleSecureSettingsAutoEnable: (Boolean) -> Unit,
    onToggleStableMode: (Boolean) -> Unit
) {
    val a11yConfig = uiState.a11yConfig
    
    SettingsGroupCard(
        title = "无障碍服务",
        items = buildList {
            // 无障碍服务开关
            add(
                SettingsSwitchItem(
                    title = "无障碍服务",
                    description = buildString {
                        if (uiState.a11yServiceEnabled) {
                            if (uiState.a11yServiceRunning) {
                                append("无障碍服务已开启并运行中")
                            } else {
                                append("无障碍服务已开启但未运行")
                            }
                        } else {
                            append("开启后可进行自动化操作")
                        }
                    },
                    icon = Icons.Default.Accessibility,
                    isChecked = uiState.a11yServiceEnabled,
                    onCheckedChange = onToggleA11yService
                )
            )
            
            // Root权限自动启用
            add(
                SettingsSwitchItem(
                    title = "Root权限自动启用",
                    description = if (uiState.rootAvailable) {
                        if (a11yConfig.rootAutoEnable) "已开启Root权限自动启用" else "使用Root权限自动开启无障碍服务"
                    } else {
                        "未检测到Root权限"
                    },
                    icon = Icons.Default.Security,
                    isChecked = a11yConfig.rootAutoEnable,
                    enabled = uiState.rootAvailable,
                    onCheckedChange = onToggleRootAutoEnable
                )
            )
            
            // Secure Settings自动启用
            add(
                SettingsSwitchItem(
                    title = "Secure Settings自动启用",
                    description = if (uiState.secureSettingsAvailable) {
                        if (a11yConfig.secureSettingsAutoEnable) "已开启Secure Settings自动启用" else "使用Secure Settings API自动开启无障碍服务"
                    } else {
                        "未授予Secure Settings权限"
                    },
                    icon = Icons.Default.Settings,
                    isChecked = a11yConfig.secureSettingsAutoEnable,
                    enabled = uiState.secureSettingsAvailable,
                    onCheckedChange = onToggleSecureSettingsAutoEnable
                )
            )
            
            // 稳定模式
            add(
                SettingsSwitchItem(
                    title = "稳定模式",
                    description = buildString {
                        if (a11yConfig.stableMode) {
                            append("已开启（需重启无障碍服务生效）")
                        } else {
                            append("省略布局细节，提高稳定性")
                        }
                    },
                    icon = Icons.Default.Star,
                    isChecked = a11yConfig.stableMode,
                    onCheckedChange = onToggleStableMode
                )
            )
        }
    )
}

/**
 * 前台服务卡片
 */
@Composable
private fun ForegroundServiceCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val description = buildString {
        if (isEnabled) {
            append("前台服务正在运行，应用将在后台保持稳定")
        } else {
            append("保持应用在后台稳定运行，显示通知")
        }
    }
    
    SettingsGroupCard(
        title = "前台服务",
        items = listOf(
            SettingsSwitchItem(
                title = "前台服务",
                description = description,
                icon = Icons.Default.Notifications,
                isChecked = isEnabled,
                onCheckedChange = onToggle
            )
        )
    )
}
