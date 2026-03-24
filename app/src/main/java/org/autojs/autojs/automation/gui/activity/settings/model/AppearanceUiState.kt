package org.autojs.autojs.ui.modern.activity.settings.model

data class AppearanceUiState(
    // 应用语言
    val currentLanguage: AppLanguage = AppLanguage.AUTO,
    val isSystemLanguageSupported: Boolean = true,
    
    // 主题颜色
    val currentThemeColor: ThemeColor = ThemeColor(),
    val themeColorSummary: String = "",
    
    // 夜间模式
    val currentNightMode: NightMode = NightMode.FOLLOW_SYSTEM,
    val isAutoNightModeEnabled: Boolean = true,
    val isAutoNightModeFunctional: Boolean = true,
    
    // 前台保持常亮
    val currentKeepScreenOnMode: KeepScreenOnMode = KeepScreenOnMode.DISABLED,
    
    // UI 状态
    val isLoading: Boolean = false,
    val error: String? = null
)