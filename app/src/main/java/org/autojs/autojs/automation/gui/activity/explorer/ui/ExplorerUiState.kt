package org.autojs.autojs.ui.modern.activity.explorer.ui

import org.autojs.autojs.model.explorer.ExplorerPage
import org.autojs.autojs.model.explorer.ExplorerFileItem

/**
 * 文件浏览器 UI 状态
 */
data class ExplorerUiState(
    /** 当前路径 */
    val currentPath: String = "",
    
    /** 目录列表 */
    val dirs: List<ExplorerPage> = emptyList(),
    
    /** 文件列表 */
    val files: List<ExplorerFileItem> = emptyList(),
    
    /** 是否正在加载 */
    val isLoading: Boolean = false,
    
    /** 错误信息 */
    val error: String? = null,
    
    /** 是否可以返回上一级 */
    val canGoBack: Boolean = false,
    
    /** 是否可以向上到父目录 */
    val canGoUp: Boolean = false,
    
    /** 浏览历史 */
    val history: List<String> = emptyList()
) {
    val isEmpty: Boolean
        get() = dirs.isEmpty() && files.isEmpty()
}