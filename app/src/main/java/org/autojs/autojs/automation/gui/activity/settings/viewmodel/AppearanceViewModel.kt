package org.autojs.autojs.ui.modern.activity.settings.viewmodel

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.autojs.autojs.app.GlobalAppContext
import org.autojs.autojs.ui.modern.activity.settings.intent.AppearanceIntent
import org.autojs.autojs.ui.modern.activity.settings.model.*
import org.autojs.autojs.util.LocaleUtils
import org.autojs.autojs.util.ViewUtils
import org.autojs.autojs6.R

class AppearanceViewModel : ViewModel() {
    
    private val context: Context = GlobalAppContext.get()
    
    // UI 状态
    private val _uiState = MutableStateFlow(AppearanceUiState())
    val uiState: StateFlow<AppearanceUiState> = _uiState.asStateFlow()
    
    // Toast 消息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    init {
        loadAppearanceSettings()
    }
    
    fun handleIntent(intent: AppearanceIntent) {
        when (intent) {
            is AppearanceIntent.LoadAppearanceSettings -> loadAppearanceSettings()
            is AppearanceIntent.ChangeLanguage -> changeLanguage(intent.language)
            is AppearanceIntent.ChangeNightMode -> changeNightMode(intent.mode)
            is AppearanceIntent.ToggleAutoNightMode -> toggleAutoNightMode(intent.enabled)
            is AppearanceIntent.ChangeKeepScreenOnMode -> changeKeepScreenOnMode(intent.mode)
        }
    }
    
    private fun loadAppearanceSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            
            val currentLanguage = AppLanguage.getPrefLanguage(context)
            val currentThemeColor = ThemeColor.getPrefThemeColor(context) ?: ThemeColor()
            val currentNightMode = NightMode.getPrefNightMode(context)
            val isAutoNightModeEnabled = prefs.getBoolean(
                context.getString(R.string.key_auto_night_mode_enabled), 
                true
            )
            val isAutoNightModeFunctional = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            val currentKeepScreenOnMode = KeepScreenOnMode.getPrefMode(context)
            
            _uiState.update {
                it.copy(
                    currentLanguage = currentLanguage,
                    isSystemLanguageSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                    currentThemeColor = currentThemeColor,
                    themeColorSummary = ThemeColor.getThemeColorSummary(context),
                    currentNightMode = currentNightMode,
                    isAutoNightModeEnabled = isAutoNightModeEnabled,
                    isAutoNightModeFunctional = isAutoNightModeFunctional,
                    currentKeepScreenOnMode = currentKeepScreenOnMode,
                    isLoading = false
                )
            }
        }
    }
    
    private fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            
            // 保存配置
            prefs.edit().putString(
                context.getString(R.string.key_app_language), 
                language.languageTag
            ).apply()
            
            // 立即应用语言（复用老UI的业务逻辑）
            if (language.isAuto) {
                LocaleUtils.setFollowSystem(context)
            } else {
                LocaleUtils.setLocale(context, language.locale)
            }
            
            // 显示提示
            _toastMessage.value = "语言已切换"
            
            _uiState.update { it.copy(currentLanguage = language) }
        }
    }
    
    private fun changeNightMode(mode: NightMode) {
        viewModelScope.launch {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            
            val autoNightKey = context.getString(R.string.key_auto_night_mode_enabled)
            val nightModeEnabledKey = context.getString(R.string.key_night_mode_enabled)
            
            // 保存配置并同步自动夜间模式状态
            val isAutoNightModeEnabled = when (mode) {
                NightMode.FOLLOW_SYSTEM -> true
                NightMode.ALWAYS_ON, NightMode.ALWAYS_OFF -> false
            }
            
            prefs.edit()
                .putBoolean(autoNightKey, isAutoNightModeEnabled)
                .putBoolean(nightModeEnabledKey, when (mode) {
                    NightMode.FOLLOW_SYSTEM -> ViewUtils.isSystemDarkModeEnabled(context)
                    NightMode.ALWAYS_ON -> true
                    NightMode.ALWAYS_OFF -> false
                })
                .apply()
            
            // 立即应用配置
            AppCompatDelegate.setDefaultNightMode(mode.appCompatMode)
            
            // 同步更新UI状态
            _uiState.update { 
                it.copy(
                    currentNightMode = mode,
                    isAutoNightModeEnabled = isAutoNightModeEnabled
                )
            }
        }
    }
    
    private fun toggleAutoNightMode(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            
            val autoNightKey = context.getString(R.string.key_auto_night_mode_enabled)
            val nightModeEnabledKey = context.getString(R.string.key_night_mode_enabled)
            
            // 保存配置
            prefs.edit()
                .putBoolean(autoNightKey, enabled)
                .apply()
            
            // 根据开关状态确定夜间模式
            val newNightMode: NightMode
            if (enabled) {
                // 开启自动夜间模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                newNightMode = NightMode.FOLLOW_SYSTEM
                prefs.edit()
                    .putBoolean(nightModeEnabledKey, ViewUtils.isSystemDarkModeEnabled(context))
                    .apply()
            } else {
                // 关闭自动夜间模式，根据当前系统状态确定手动模式
                val isCurrentlyDark = ViewUtils.isNightModeYes(context.resources.configuration)
                newNightMode = if (isCurrentlyDark) NightMode.ALWAYS_ON else NightMode.ALWAYS_OFF
                AppCompatDelegate.setDefaultNightMode(newNightMode.appCompatMode)
                prefs.edit()
                    .putBoolean(nightModeEnabledKey, isCurrentlyDark)
                    .apply()
            }
            
            // 同步更新UI状态
            _uiState.update { 
                it.copy(
                    isAutoNightModeEnabled = enabled,
                    currentNightMode = newNightMode
                )
            }
        }
    }
    
    private fun changeKeepScreenOnMode(mode: KeepScreenOnMode) {
        viewModelScope.launch {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            
            val modeKey = mode.getKey(context)
            val mainKey = context.getString(R.string.key_keep_screen_on_when_in_foreground)
            val enabledKey = context.getString(R.string.key_keep_screen_on_when_in_foreground_enabled)
            val lastEnabledKey = context.getString(R.string.key_keep_screen_on_when_in_foreground_last_enabled)
            
            // 保存配置
            prefs.edit()
                .putString(mainKey, modeKey)
                .apply()
            
            // 应用配置
            when (mode) {
                KeepScreenOnMode.DISABLED -> {
                    prefs.edit()
                        .putBoolean(enabledKey, false)
                        .apply()
                }
                KeepScreenOnMode.ALL_PAGES, KeepScreenOnMode.HOMEPAGE_ONLY -> {
                    prefs.edit()
                        .putBoolean(enabledKey, true)
                        .putString(lastEnabledKey, modeKey)
                        .apply()
                }
            }
            
            _uiState.update { it.copy(currentKeepScreenOnMode = mode) }
        }
    }
}