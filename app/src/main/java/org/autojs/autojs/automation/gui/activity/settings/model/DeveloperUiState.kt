package org.autojs.autojs.ui.modern.activity.settings.model

/**
 * 开发者选项UI状态
 */
data class DeveloperUiState(
    // 客户端模式状态
    val clientState: ClientState = ClientState(),
    
    // 服务端模式状态
    val serverState: ServerState = ServerState(),
    
    // UI加载状态
    val isLoading: Boolean = false,
    
    // 错误信息
    val error: String? = null
) {
    /**
     * 客户端模式状态
     */
    data class ClientState(
        // 连接状态
        val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
        
        // 当前服务器地址
        val serverAddress: String = "",
        
        // 地址历史记录
        val addressHistory: List<String> = emptyList(),
        
        // 是否正在连接
        val isConnecting: Boolean = false,
        
        // 错误消息
        val errorMessage: String? = null
    )
    
    /**
     * 服务端模式状态
     */
    data class ServerState(
        // 连接状态
        val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
        
        // 服务端IP地址
        val serverIp: String = "",
        
        // 当前连接数
        val connectionCount: Int = 0,
        
        // 是否正在启动
        val isStarting: Boolean = false
    )
    
    /**
     * 连接状态枚举
     */
    enum class ConnectionState {
        /** 已断开 */
        DISCONNECTED,
        /** 连接中 */
        CONNECTING,
        /** 已连接 */
        CONNECTED
    }
}