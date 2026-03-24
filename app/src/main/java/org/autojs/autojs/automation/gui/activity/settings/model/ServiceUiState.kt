package org.autojs.autojs.ui.modern.activity.settings.model

/**
 * 服务设置UI状态
 */
data class ServiceUiState(
    // 无障碍服务开关状态
    val a11yServiceEnabled: Boolean = false,
    
    // 无障碍服务配置
    val a11yConfig: A11yConfig = A11yConfig(),
    
    // 无障碍服务运行状态
    val a11yServiceRunning: Boolean = false,
    
    // 前台服务状态
    val foregroundServiceEnabled: Boolean = false,
    
    // Root权限状态
    val rootAvailable: Boolean = false,
    
    // Secure Settings权限状态
    val secureSettingsAvailable: Boolean = false,
    
    // UI加载状态
    val isLoading: Boolean = false,
    
    // 错误信息
    val error: String? = null
) {
    /**
     * 无障碍服务配置
     */
    data class A11yConfig(
        // Root权限自动启用
        val rootAutoEnable: Boolean = true,
        // Secure Settings自动启用
        val secureSettingsAutoEnable: Boolean = true,
        // 稳定模式
        val stableMode: Boolean = false
    )
}
