package org.autojs.autojs.ui.modern.activity.settings.intent

import org.autojs.autojs.ui.modern.activity.settings.model.*

sealed class AppearanceIntent {
    object LoadAppearanceSettings : AppearanceIntent()
    
    // 应用语言
    data class ChangeLanguage(val language: AppLanguage) : AppearanceIntent()
    
    // 夜间模式
    data class ChangeNightMode(val mode: NightMode) : AppearanceIntent()
    data class ToggleAutoNightMode(val enabled: Boolean) : AppearanceIntent()
    
    // 前台保持常亮
    data class ChangeKeepScreenOnMode(val mode: KeepScreenOnMode) : AppearanceIntent()
}