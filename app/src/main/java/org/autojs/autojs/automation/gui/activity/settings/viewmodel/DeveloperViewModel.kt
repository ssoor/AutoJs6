package org.autojs.autojs.ui.modern.activity.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.autojs.autojs.AutoJs
import org.autojs.autojs.app.GlobalAppContext
import org.autojs.autojs.core.pref.Pref
import org.autojs.autojs.pluginclient.DevPluginService
import org.autojs.autojs.pluginclient.JsonSocketClient
import org.autojs.autojs.pluginclient.JsonSocketServer
import org.autojs.autojs.ui.modern.activity.settings.model.DeveloperIntent
import org.autojs.autojs.ui.modern.activity.settings.model.DeveloperUiState
import org.autojs.autojs.util.NetworkUtils

/**
 * 开发者选项ViewModel
 */
class DeveloperViewModel : ViewModel() {
    
    private val context: Context = GlobalAppContext.get()
    private val devPluginService: DevPluginService = AutoJs.instance.devPluginService
    
    // UI状态
    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()
    
    // Toast消息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // 对话框显示事件
    private val _showDialog = MutableSharedFlow<DialogEvent>()
    val showDialog = _showDialog.asSharedFlow()
    
    init {
        loadSettings()
        observeConnectionStates()
    }
    
    /**
     * 处理用户意图
     */
    fun handleIntent(intent: DeveloperIntent) {
        when (intent) {
            is DeveloperIntent.LoadSettings -> loadSettings()
            is DeveloperIntent.ShowConnectDialog -> showConnectDialog()
            is DeveloperIntent.ConnectToServer -> connectToServer(intent.address)
            is DeveloperIntent.DisconnectClient -> disconnectClient()
            is DeveloperIntent.RemoveHistoryAddress -> removeHistoryAddress(intent.address)
            is DeveloperIntent.ClearHistoryAddresses -> clearHistoryAddresses()
            is DeveloperIntent.StartServer -> startServer()
            is DeveloperIntent.StopServer -> stopServer()
        }
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        _uiState.update { 
            it.copy(
                clientState = it.clientState.copy(
                    serverAddress = Pref.getServerAddress(),
                    addressHistory = JsonSocketClient.serverAddressHistory.toList()
                ),
                serverState = it.serverState.copy(
                    serverIp = NetworkUtils.getIpAddress()
                )
            )
        }
    }
    
    /**
     * 订阅连接状态变化
     */
    private fun observeConnectionStates() {
        // 订阅客户端状态
        Observable
            .combineLatest(
                JsonSocketClient.cxnState,
                devPluginService.clientConnectionIpAddress
            ) { state, ip ->
                Pair(state, ip)
            }
            .subscribeOn(Schedulers.io())
            .subscribe { (state, ip) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        clientState = currentState.clientState.copy(
                            connectionState = state.toConnectionState(),
                            serverAddress = ip,
                            isConnecting = state.isConnecting(),
                            errorMessage = state.exception?.message
                        )
                    )
                }
            }
        
        // 订阅服务端状态
        Observable
            .combineLatest(
                JsonSocketServer.cxnState,
                devPluginService.serverConnectionCount
            ) { state, count ->
                Pair(state, count)
            }
            .subscribeOn(Schedulers.io())
            .subscribe { (state, count) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        serverState = currentState.serverState.copy(
                            connectionState = state.toConnectionState(),
                            serverIp = NetworkUtils.getIpAddress(),
                            connectionCount = count
                        )
                    )
                }
            }
    }
    
    /**
     * 显示连接对话框
     */
    private fun showConnectDialog() {
        viewModelScope.launch {
            _showDialog.emit(DialogEvent.ShowConnectDialog)
        }
    }
    
    /**
     * 连接到服务器
     */
    private fun connectToServer(address: String) {
        val trimmedAddress = address.trim()
        if (trimmedAddress.isEmpty()) {
            _toastMessage.value = "服务器地址不能为空"
            return
        }
        
        // 验证地址格式
        val validation = validateAddress(trimmedAddress)
        if (!validation.isValid) {
            _toastMessage.value = validation.errorMessage
            return
        }
        
        viewModelScope.launch {
            devPluginService.connectToRemoteServer(context, trimmedAddress)
                .subscribe(
                    { client ->
                        // 连接成功
                        JsonSocketClient.addIntoHistory(trimmedAddress)
                        Pref.setServerAddress(trimmedAddress)
                        _toastMessage.value = "已连接到 $trimmedAddress"
                    },
                    { error ->
                        // 连接失败
                        _toastMessage.value = "连接失败: ${error.message}"
                    }
                )
        }
    }
    
    /**
     * 断开客户端连接
     */
    private fun disconnectClient() {
        devPluginService.disconnectJsonSocketClient()
        JsonSocketClient.isClientSocketNormallyClosed = true
        _toastMessage.value = "已断开连接"
    }
    
    /**
     * 从历史记录中移除地址
     */
    private fun removeHistoryAddress(address: String) {
        JsonSocketClient.removeFromHistory(address)
        _uiState.update { currentState ->
            currentState.copy(
                clientState = currentState.clientState.copy(
                    addressHistory = JsonSocketClient.serverAddressHistory.toList()
                )
            )
        }
    }
    
    /**
     * 清空历史记录
     */
    private fun clearHistoryAddresses() {
        JsonSocketClient.clearHistory()
        _uiState.update { currentState ->
            currentState.copy(
                clientState = currentState.clientState.copy(
                    addressHistory = emptyList()
                )
            )
        }
        _toastMessage.value = "历史记录已清空"
    }
    
    /**
     * 启动服务端
     */
    private fun startServer() {
        devPluginService.enableLocalServer()
            .subscribe(
                { _toastMessage.value = "服务端已启动" },
                { error ->
                    _toastMessage.value = "启动服务端失败: ${error.message}"
                }
            )
        JsonSocketServer.isServerSocketNormallyClosed = false
    }
    
    /**
     * 停止服务端
     */
    private fun stopServer() {
        devPluginService.disconnectJsonSocketServer()
        JsonSocketServer.isServerSocketNormallyClosed = true
        _toastMessage.value = "服务端已停止"
    }
    
    /**
     * 验证地址格式
     */
    private fun validateAddress(address: String): ValidationResult {
        // IPv4格式
        val ipv4Pattern = Regex("""^(\d{1,3}\.){3}\d{1,3}(:\d{1,5})?$""")
        // IPv6格式 (带方括号或无端口)
        val ipv6Pattern = Regex("""^(\[?[0-9a-fA-F:]+\]?)(:\d{1,5})?$""")
        // 域名格式
        val domainPattern = Regex("""^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)*[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(:\d{1,5})?$""")
        
        return when {
            ipv4Pattern.matches(address) -> ValidationResult(true, null)
            ipv6Pattern.matches(address) -> ValidationResult(true, null)
            domainPattern.matches(address) -> ValidationResult(true, null)
            else -> ValidationResult(false, "地址格式无效")
        }
    }
    
    /**
     * 将DevPluginService.State转换为ConnectionState
     */
    private fun DevPluginService.State.toConnectionState(): DeveloperUiState.ConnectionState {
        return when {
            isDisconnected() -> DeveloperUiState.ConnectionState.DISCONNECTED
            isConnecting() -> DeveloperUiState.ConnectionState.CONNECTING
            isConnected() -> DeveloperUiState.ConnectionState.CONNECTED
            else -> DeveloperUiState.ConnectionState.DISCONNECTED
        }
    }
    
    /**
     * 对话框事件
     */
    sealed class DialogEvent {
        object ShowConnectDialog : DialogEvent()
    }
    
    /**
     * 验证结果
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}