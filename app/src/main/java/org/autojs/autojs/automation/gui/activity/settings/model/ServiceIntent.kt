package org.autojs.autojs.ui.modern.activity.settings.model

/**
 * 服务设置意图定义
 */
sealed class ServiceIntent {
    /**
     * 加载设置
     */
    object LoadSettings : ServiceIntent()
    
    /**
     * 刷新服务状态
     */
    object RefreshServiceStatus : ServiceIntent()
    
    // ========== 无障碍服务 ==========
    
    /**
     * 切换无障碍服务开关
     */
    data class ToggleA11yService(val enabled: Boolean) : ServiceIntent()
    
    /**
     * 切换Root权限自动启用
     */
    data class ToggleRootAutoEnable(val enabled: Boolean) : ServiceIntent()
    
    /**
     * 切换Secure Settings自动启用
     */
    data class ToggleSecureSettingsAutoEnable(val enabled: Boolean) : ServiceIntent()
    
    /**
     * 切换稳定模式
     */
    data class ToggleStableMode(val enabled: Boolean) : ServiceIntent()
    
    // ========== 前台服务 ==========
    
    /**
     * 切换前台服务
     */
    data class ToggleForegroundService(val enabled: Boolean) : ServiceIntent()
}
