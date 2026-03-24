package org.autojs.autojs.ui.modern.log

/**
 * 日志UI状态
 */
data class LogUiState(
    /** 日志条目列表 */
    val logs: List<LogItem> = emptyList(),
    
    /** 是否正在加载 */
    val isLoading: Boolean = false,
    
    /** 错误信息 */
    val error: String? = null,
    
    /** 字体大小（sp） */
    val fontSize: Float = 14f,
) {
    // ✅ 改为计算属性，每次访问时重新计算
    val isEmpty: Boolean
        get() = logs.isEmpty()
}
    
/**
 * 日志项数据模型
 */
data class LogItem(
    val id: Int = 0,
    val level: Int = 0,
    val content: String
)
