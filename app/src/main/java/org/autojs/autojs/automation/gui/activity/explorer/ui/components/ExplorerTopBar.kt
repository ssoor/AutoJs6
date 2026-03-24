package org.autojs.autojs.ui.modern.activity.explorer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * 文件浏览器顶部工具栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerTopBar(
    currentPath: String,
    canGoBack: Boolean,
    canGoUp: Boolean,
    onGoBack: () -> Unit,
    onGoUp: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "文件",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onGoUp, enabled = canGoUp) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "向上"
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}