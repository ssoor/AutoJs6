package org.autojs.autojs.ui.modern.activity.settings.model

/**
 * 开发者选项意图定义
 */
sealed class DeveloperIntent {
    /**
     * 加载设置
     */
    object LoadSettings : DeveloperIntent()
    
    // ========== 客户端模式 ==========
    
    /**
     * 显示连接对话框
     */
    object ShowConnectDialog : DeveloperIntent()
    
    /**
     * 连接到服务器
     */
    data class ConnectToServer(val address: String) : DeveloperIntent()
    
    /**
     * 断开客户端连接
     */
    object DisconnectClient : DeveloperIntent()
    
    /**
     * 从历史记录中移除地址
     */
    data class RemoveHistoryAddress(val address: String) : DeveloperIntent()
    
    /**
     * 清空历史记录
     */
    object ClearHistoryAddresses : DeveloperIntent()
    
    // ========== 服务端模式 ==========
    
    /**
     * 启动服务端
     */
    object StartServer : DeveloperIntent()
    
    /**
     * 停止服务端
     */
    object StopServer : DeveloperIntent()
}