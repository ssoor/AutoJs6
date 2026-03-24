package org.autojs.autojs.ui.modern.activity.explorer.ui

import org.autojs.autojs.model.explorer.ExplorerPage
import org.autojs.autojs.model.explorer.ExplorerFileItem

/**
 * 文件浏览器意图
 * 定义用户可以执行的所有操作
 */
sealed class ExplorerIntent {
    
    /**
     * 加载目录
     */
    data class LoadDirectory(val path: String) : ExplorerIntent()
    
    /**
     * 返回上一级
     */
    object GoBack : ExplorerIntent()
    
    /**
     * 向上到父目录
     */
    object GoUp : ExplorerIntent()
    
    /**
     * 进入子目录
     */
    data class EnterDirectory(val item: ExplorerPage) : ExplorerIntent()
    
    /**
     * 执行脚本
     */
    data class RunScript(val item: ExplorerFileItem) : ExplorerIntent()
    
    /**
     * 停止脚本
     */
    data class StopScript(val item: ExplorerFileItem) : ExplorerIntent()
    
    /**
     * 编辑脚本
     */
    data class EditScript(val item: ExplorerFileItem) : ExplorerIntent()
    
    /**
     * 刷新当前目录
     */
    object Refresh : ExplorerIntent()
}