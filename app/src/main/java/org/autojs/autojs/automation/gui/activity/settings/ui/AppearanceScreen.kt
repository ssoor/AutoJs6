package org.autojs.autojs.ui.modern.activity.settings.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.autojs.autojs.ui.modern.activity.settings.intent.AppearanceIntent
import org.autojs.autojs.ui.modern.activity.settings.model.AppLanguage
import org.autojs.autojs.ui.modern.activity.settings.model.KeepScreenOnMode
import org.autojs.autojs.ui.modern.activity.settings.model.NightMode
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListOption
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListItem as ModelSettingsListItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsNavigationItem as ModelSettingsNavigationItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsSwitchItem as ModelSettingsSwitchItem
import org.autojs.autojs.ui.modern.activity.settings.ui.components.SettingsGroupCard
import org.autojs.autojs.ui.modern.activity.settings.viewmodel.AppearanceViewModel
import org.autojs.autojs6.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: AppearanceViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 显示 Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("常规") },
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 应用语言
                item {
                    SettingsGroupCard(
                        title = "应用语言",
                        items = listOf(
                            ModelSettingsListItem(
                                title = context.getString(R.string.text_app_language),
                                description = context.getString(uiState.currentLanguage.titleRes),
                                icon = Icons.Default.Language,
                                value = context.getString(uiState.currentLanguage.titleRes),
                                selectedValue = uiState.currentLanguage,
                                options = AppLanguage.values().map {
                                    SettingsListOption(
                                        title = context.getString(it.titleRes),
                                        value = it
                                    )
                                },
                                onValueChange = { option ->
                                    if (option is AppLanguage) {
                                        viewModel.handleIntent(AppearanceIntent.ChangeLanguage(option))
                                    }
                                },
                                // 简单导航：直接在 UI 中处理
                                neutralButtonText = if (uiState.isSystemLanguageSupported) "系统设置" else null,
                                onNeutralButtonClick = {
                                    if (uiState.isSystemLanguageSupported) {
                                        val intent = Intent(Intent.ACTION_MAIN).apply {
                                            setClassName("com.android.settings", "com.android.settings.LanguageSettings")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        )
                    )
                }
                
                // 主题颜色
                item {
                    SettingsGroupCard(
                        title = "主题颜色",
                        items = listOf(
                            ModelSettingsNavigationItem(
                                title = context.getString(R.string.text_theme_color),
                                description = uiState.themeColorSummary,
                                icon = Icons.Default.Palette,
                                // 简单导航：直接在 UI 中处理
                                onClick = {
                                    val intent = Intent(context, org.autojs.autojs.theme.app.ColorSelectBaseActivity::class.java)
                                    context.startActivity(intent)
                                }
                            )
                        )
                    )
                }
                
                // 夜间模式
                item {
                    SettingsGroupCard(
                        title = "夜间模式",
                        items = buildList {
                            // 自动夜间模式（如果支持）
                            if (uiState.isAutoNightModeFunctional) {
                                add(
                                    ModelSettingsSwitchItem(
                                        title = context.getString(R.string.text_auto_night_mode),
                                        description = "自动跟随系统夜间模式",
                                        icon = Icons.Default.AutoMode,
                                        isChecked = uiState.isAutoNightModeEnabled,
                                        onCheckedChange = { enabled ->
                                            viewModel.handleIntent(AppearanceIntent.ToggleAutoNightMode(enabled))
                                        }
                                    )
                                )
                            }

                            // 夜间模式选择
                            add(
                                ModelSettingsListItem(
                                    title = context.getString(R.string.text_night_mode),
                                    description = "选择夜间模式",
                                    icon = Icons.Default.DarkMode,
                                    value = context.getString(uiState.currentNightMode.titleRes),
                                    selectedValue = uiState.currentNightMode,
                                    options = NightMode.values().map { mode ->
                                        SettingsListOption(
                                            title = context.getString(mode.titleRes),
                                            value = mode
                                        )
                                    },
                                    onValueChange = { option ->
                                        if (option is NightMode) {
                                            viewModel.handleIntent(AppearanceIntent.ChangeNightMode(option))
                                        }
                                    }
                                )
                            )
                        }
                    )
                }
                
                // 前台保持常亮
                item {
                    SettingsGroupCard(
                        title = "前台保持常亮",
                        items = listOf(
                            ModelSettingsListItem(
                                title = context.getString(R.string.text_keep_screen_on_when_in_foreground),
                                description = "选择保持常亮的页面",
                                icon = Icons.Default.Brightness7,
                                value = context.getString(uiState.currentKeepScreenOnMode.titleRes),
                                selectedValue = uiState.currentKeepScreenOnMode,
                                options = KeepScreenOnMode.values().map { mode ->
                                    SettingsListOption(
                                        title = context.getString(mode.titleRes),
                                        value = mode
                                    )
                                },
                                onValueChange = { option ->
                                    if (option is KeepScreenOnMode) {
                                        viewModel.handleIntent(AppearanceIntent.ChangeKeepScreenOnMode(option))
                                    }
                                }
                            )
                        )
                    )
                }
            }
        }
    }
}