package org.autojs.autojs.ui.modern.log

/**
 * 日志意图
 * 定义用户在日志页面可以执行的所有操作
 */
sealed class LogIntent {
    
    /**
     * 清除日志
     */
    object ClearLogs : LogIntent()
    
    /**
     * 复制所有日志
     */
    object CopyAllLogs : LogIntent()
    
    /**
     * 导出日志
     */
    object ExportLogs : LogIntent()
    
    /**
     * 发送日志
     */
    object SendLogs : LogIntent()
    
    /**
     * 调整字体大小
     * @param fontSize 字体大小（sp）
     */
    data class ChangeFontSize(val fontSize: Float) : LogIntent()
    
    /**
     * 增加字体大小
     */
    object IncreaseFontSize : LogIntent()
    
    /**
     * 减少字体大小
     */
    object DecreaseFontSize : LogIntent()
    
    /**
     * 刷新日志
     */
    object Refresh : LogIntent()
}
