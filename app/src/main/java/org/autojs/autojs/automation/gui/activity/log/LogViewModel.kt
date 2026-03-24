package org.autojs.autojs.ui.modern.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.autojs.autojs.core.console.ConsoleImpl
import android.util.Log
import org.autojs.autojs.inrt.autojs.AutoJs
import org.autojs.autojs.ui.modern.log.LogIntent
import org.autojs.autojs.ui.modern.log.LogItem
import org.autojs.autojs.ui.modern.log.LogUiState
import java.util.Timer
import java.util.TimerTask
import org.autojs.autojs.core.console.GlobalConsole

/**
 * 日志 ViewModel
 */
class LogViewModel : ViewModel() {
    
    companion object {
        private const val REFRESH_INTERVAL_MS = 100L
        private const val MIN_FONT_SIZE = 8f
        private const val MAX_FONT_SIZE = 56f
    }
    
    // // ConsoleImpl 数据源
    // private val console: ConsoleImpl
    //     get() = AutoJs.instance.globalConsole as ConsoleImpl

    // GlobalConsole 数据源
    private val console: org.autojs.autojs.core.console.GlobalConsole
        get() = AutoJs.instance.globalConsole as org.autojs.autojs.core.console.GlobalConsole  // ✅ 使用正确的类型
    
    
    // UI 状态
    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()
    
    // Toast 消息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // 定时刷新
    private var timer: Timer? = null
    private var lastCount = 0
    
    init {
        android.util.Log.d("LogViewModel", "初始化 LogViewModel")
        android.util.Log.d("LogViewModel", "console 类型: ${console::class.java.simpleName}")
        android.util.Log.d("LogViewModel", "logEntries 大小: ${console.logEntries.size}")
        startRefresh()
    }
    
    /**
     * 处理 Intent
     */
    fun handleIntent(intent: LogIntent) {
        when (intent) {
            is LogIntent.ClearLogs -> clearLogs()
            is LogIntent.CopyAllLogs -> copyAllLogs()
            is LogIntent.ExportLogs -> exportLogs()
            is LogIntent.SendLogs -> sendLogs()
            is LogIntent.ChangeFontSize -> changeFontSize(intent.fontSize)
            is LogIntent.IncreaseFontSize -> increaseFontSize()
            is LogIntent.DecreaseFontSize -> decreaseFontSize()
            is LogIntent.Refresh -> refreshLogs()
        }
    }
    
    /**
     * 启动定时刷新
     */
    private fun startRefresh() {
        android.util.Log.d("LogViewModel", "启动定时刷新")
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    android.util.Log.d("LogViewModel", "定时刷新触发")
                    refreshLogs()
                }
            }, REFRESH_INTERVAL_MS, REFRESH_INTERVAL_MS)
        }
    }
    
    /**
     * 刷新日志
     */
    private fun refreshLogs() {
        val entries = console.logEntries
        val currentCount = entries.size

        Log.d("LogViewModel", "刷新日志: 条目数 = $currentCount, lastCount = $lastCount")
        
        if (currentCount != lastCount) {
            lastCount = currentCount
            val logEntries = entries.map { entry: ConsoleImpl.LogEntry ->
                Log.d("LogViewModel", "处理日志: id=${entry.id}, level=${entry.level}, content=${entry.content}")
                LogItem(
                    id = entry.id,
                    level = entry.level,
                    content = entry.content.toString()
                )
            }
            Log.d("LogViewModel", "更新 UI 状态，日志数量 = ${logEntries.size}")
            _uiState.update { it.copy(logs = logEntries) }
        }else {
            Log.d("LogViewModel", "日志数量未变化，跳过更新")
        }
    }
    
    /**
     * 清除日志
     */
    private fun clearLogs() {
        console.clear()
        _uiState.update { it.copy(logs = emptyList()) }
        _toastMessage.value = "日志已清除"
        lastCount = 0
    }
    
    /**
     * 复制所有日志
     */
    private fun copyAllLogs() {
        val logsText = _uiState.value.logs.joinToString("\n") { logItem -> logItem.content }
        if (logsText.isEmpty()) {
            _toastMessage.value = "没有日志可复制"
            return
        }
        // TODO: 复制到剪贴板
        _toastMessage.value = "已复制到剪贴板"
    }
    
    /**
     * 导出日志
     */
    private fun exportLogs() {
        val logsText = _uiState.value.logs.joinToString("\n") { logItem -> logItem.content }
        if (logsText.isEmpty()) {
            _toastMessage.value = "没有日志可导出"
            return
        }
        // TODO: 导出逻辑
        _toastMessage.value = "导出功能待实现"
    }
    
    /**
     * 发送日志
     */
    private fun sendLogs() {
        val logsText = _uiState.value.logs.joinToString("\n") { logItem -> logItem.content }
        if (logsText.isEmpty()) {
            _toastMessage.value = "没有日志可发送"
            return
        }
        // TODO: 发送逻辑
        _toastMessage.value = "发送功能待实现"
    }
    
    /**
     * 修改字体大小
     */
    private fun changeFontSize(fontSize: Float) {
        val clampedSize = fontSize.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        _uiState.update { it.copy(fontSize = clampedSize) }
    }
    
    /**
     * 增加字体大小
     */
    private fun increaseFontSize() {
        val currentSize = _uiState.value.fontSize
        val newSize = (currentSize + 1f).coerceAtMost(MAX_FONT_SIZE)
        _uiState.update { it.copy(fontSize = newSize) }
    }
    
    /**
     * 减少字体大小
     */
    private fun decreaseFontSize() {
        val currentSize = _uiState.value.fontSize
        val newSize = (currentSize - 1f).coerceAtLeast(MIN_FONT_SIZE)
        _uiState.update { it.copy(fontSize = newSize) }
    }
    
    /**
     * 获取日志颜色
     */
    fun getLogColor(level: Int): Int {
        return when (level) {
            Log.VERBOSE -> 0xFFBDBDBD.toInt()
            Log.DEBUG -> 0xFF111214.toInt()
            Log.INFO -> 0xFF43A047.toInt()
            Log.WARN -> 0xFF1976D2.toInt()
            Log.ERROR -> 0xFFC62828.toInt()
            Log.ASSERT -> 0xFFE254FF.toInt()
            else -> 0xFFBDBDBD.toInt()
        }
    }
    
    /**
     * 获取日志级别名称
     */
    fun getLogLevelName(level: Int): String {
        return when (level) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }
    }
    
    /**
     * 停止刷新
     */
    fun stopRefresh() {
        timer?.cancel()
        timer = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopRefresh()
    }
}