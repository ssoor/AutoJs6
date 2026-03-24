package org.autojs.autojs.ui.modern.activity.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.autojs.autojs.app.GlobalAppContext
import org.autojs.autojs.core.accessibility.AccessibilityTool
import org.autojs.autojs.core.pref.Pref
import org.autojs.autojs.service.AccessibilityService
import org.autojs.autojs.service.ForegroundService
import org.autojs.autojs.ui.modern.activity.settings.model.ServiceIntent
import org.autojs.autojs.ui.modern.activity.settings.model.ServiceUiState
import org.autojs.autojs.util.RootUtils
import org.autojs.autojs.util.SettingsUtils
import org.autojs.autojs6.R
import org.autojs.autojs.automation.service.ServiceManager

/**
 * 服务设置ViewModel
 */
class ServiceViewModel : ViewModel() {
    
    private val context: Context = GlobalAppContext.get()
    
    // UI状态
    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()
    
    // Toast消息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * 处理用户意图
     */
    fun handleIntent(intent: ServiceIntent) {
        when (intent) {
            is ServiceIntent.LoadSettings -> loadSettings()
            is ServiceIntent.RefreshServiceStatus -> refreshServiceStatus()
            is ServiceIntent.ToggleA11yService -> toggleA11yService(intent.enabled)
            is ServiceIntent.ToggleRootAutoEnable -> toggleRootAutoEnable(intent.enabled)
            is ServiceIntent.ToggleSecureSettingsAutoEnable -> toggleSecureSettingsAutoEnable(intent.enabled)
            is ServiceIntent.ToggleStableMode -> toggleStableMode(intent.enabled)
            is ServiceIntent.ToggleForegroundService -> toggleForegroundService(intent.enabled)
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val settings = loadSettingsInternal()
                _uiState.update {
                    it.copy(
                        a11yServiceEnabled = settings.a11yServiceEnabled,
                        a11yConfig = settings.a11yConfig,
                        foregroundServiceEnabled = settings.foregroundServiceEnabled,
                        a11yServiceRunning = settings.a11yServiceRunning,
                        rootAvailable = settings.rootAvailable,
                        secureSettingsAvailable = settings.secureSettingsAvailable,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    /**
     * 刷新服务状态
     */
    private fun refreshServiceStatus() {
        viewModelScope.launch {
            val status = checkServiceStatusInternal()
            _uiState.update {
                it.copy(
                    a11yServiceEnabled = status.a11yEnabled,
                    a11yServiceRunning = status.a11yRunning,
                    foregroundServiceEnabled = status.foregroundRunning
                )
            }
        }
    }
    
    /**
     * 切换无障碍服务
     */
    private fun toggleA11yService(enabled: Boolean) {
        viewModelScope.launch {
            val a11yService = AccessibilityService(context)
            val success = withContext(Dispatchers.IO) {
                if (enabled) {
                    a11yService.start()
                } else {
                    a11yService.stop()
                }
            }
            
            if (success) {
                _toastMessage.value = if (enabled) "无障碍服务已开启" else "无障碍服务已关闭"
                _uiState.update { 
                    it.copy(
                        a11yServiceEnabled = enabled,
                        a11yServiceRunning = enabled
                    )
                }
            } else {
                _toastMessage.value = if (enabled) "开启无障碍服务失败，请手动开启" else "关闭无障碍服务失败"
            }
        }
    }
    
    /**
     * 切换Root权限自动启用
     */
    private fun toggleRootAutoEnable(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit()
                .putBoolean(context.getString(R.string.key_enable_a11y_service_with_root_access), enabled)
                .apply()
            
            _uiState.update { it.copy(
                a11yConfig = it.a11yConfig.copy(rootAutoEnable = enabled)
            )}
            
            _toastMessage.value = if (enabled) {
                "Root权限自动启用已开启"
            } else {
                "Root权限自动启用已关闭"
            }
        }
    }
    
    /**
     * 切换Secure Settings自动启用
     */
    private fun toggleSecureSettingsAutoEnable(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit()
                .putBoolean(context.getString(R.string.key_enable_a11y_service_with_secure_settings), enabled)
                .apply()
            
            _uiState.update { it.copy(
                a11yConfig = it.a11yConfig.copy(secureSettingsAutoEnable = enabled)
            )}
            
            _toastMessage.value = if (enabled) {
                "Secure Settings自动启用已开启"
            } else {
                "Secure Settings自动启用已关闭"
            }
        }
    }
    
    /**
     * 切换稳定模式
     */
    private fun toggleStableMode(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit()
                .putBoolean(context.getString(R.string.key_stable_mode), enabled)
                .apply()
            
            _uiState.update { it.copy(
                a11yConfig = it.a11yConfig.copy(stableMode = enabled)
            )}
            
            _toastMessage.value = if (enabled) {
                "稳定模式已开启，需要重启无障碍服务才能生效"
            } else {
                "稳定模式已关闭，需要重启无障碍服务才能生效"
            }
        }
    }
    
    /**
     * 切换前台服务
     */
    private fun toggleForegroundService(enabled: Boolean) {
        viewModelScope.launch {
            val manager = ServiceManager()
            val success = withContext(Dispatchers.IO) {
                manager.setForegroundServiceEnabled(context, enabled)
            }

            if (success) {
                _toastMessage.value = if (enabled) "前台服务已启动" else "前台服务已停止"
                _uiState.update { it.copy(foregroundServiceEnabled = enabled) }
            } else {
                _toastMessage.value = "操作失败，请重试"
            }
        }
    }
    
    /**
     * 内部加载设置
     */
    private suspend fun loadSettingsInternal(): ServiceUiState = withContext(Dispatchers.IO) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val a11yTool = AccessibilityTool(context)
        
        // 读取无障碍服务配置
        val rootAutoEnable = Pref.shouldStartA11yServiceWithRoot()
        val secureSettingsAutoEnable = Pref.shouldStartA11yServiceWithSecureSettings()
        val stableMode = Pref.isStableModeEnabled
        
        // 检查无障碍服务状态
        val a11yServiceEnabled = a11yTool.hasService()
        val a11yServiceRunning = a11yTool.isRunning()
        
        // 读取前台服务配置
        val foregroundServiceEnabled = prefs.getBoolean(
            context.getString(R.string.key_foreground_service), false
        )
        
        // 检查权限状态
        val rootAvailable = RootUtils.isRootAvailable()
        val secureSettingsAvailable = SettingsUtils.SecureSettings.isGranted(context)
        
        ServiceUiState(
            a11yServiceEnabled = a11yServiceEnabled,
            a11yConfig = ServiceUiState.A11yConfig(
                rootAutoEnable = rootAutoEnable,
                secureSettingsAutoEnable = secureSettingsAutoEnable,
                stableMode = stableMode
            ),
            foregroundServiceEnabled = foregroundServiceEnabled,
            a11yServiceRunning = a11yServiceRunning,
            rootAvailable = rootAvailable,
            secureSettingsAvailable = secureSettingsAvailable
        )
    }
    
    /**
     * 检查服务状态
     */
    private suspend fun checkServiceStatusInternal(): ServiceStatus = withContext(Dispatchers.IO) {
        val a11yTool = AccessibilityTool(context)
        
        val a11yEnabled = a11yTool.hasService()
        val a11yRunning = a11yTool.isRunning()
        val foregroundRunning = ForegroundService(context).isRunning
        
        ServiceStatus(a11yEnabled, a11yRunning, foregroundRunning)
    }
    
    private data class ServiceStatus(
        val a11yEnabled: Boolean,
        val a11yRunning: Boolean,
        val foregroundRunning: Boolean
    )
}
