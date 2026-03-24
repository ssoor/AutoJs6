package org.autojs.autojs.ui.modern.screen

import androidx.compose.runtime.Composable
import org.autojs.autojs.ui.modern.activity.explorer.ui.ExplorerScreen as NewExplorerScreen

/**
 * 文件管理页面
 * 委托给新的 ExplorerScreen 实现
 */
@Composable
fun ExplorerScreen() {
    NewExplorerScreen()
}