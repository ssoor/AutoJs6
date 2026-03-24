package org.autojs.autojs.ui.modern.log

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.autojs.autojs.ui.modern.log.LogIntent
import org.autojs.autojs.ui.modern.log.LogItem
import org.autojs.autojs.ui.modern.log.LogViewModel
import kotlinx.coroutines.launch

/**
 * 日志屏幕
 * 主 UI 组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    
    val coroutineScope = rememberCoroutineScope()
    
    // 显示 Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    
    // 列表状态
    val listState = rememberLazyListState()
    
    LaunchedEffect(uiState.logs.size) {
        android.util.Log.d("LogScreen", "日志数量: ${uiState.logs.size}")
        android.util.Log.d("LogScreen", "isEmpty: ${uiState.isEmpty}")

        if (listState.layoutInfo.visibleItemsInfo.isEmpty()) {
            // 自动滚动到底部
            listState.animateScrollToItem(uiState.logs.size)
        }
    }
    
    Scaffold(
        topBar = {
            LogTopBar(
                onNavigateToSettings = {
                    // 简单导航：直接在 UI 中处理
                    onNavigateToSettings()
                },
                onClearLogs = {
                    viewModel.handleIntent(LogIntent.ClearLogs)
                },
                onCopyAllLogs = {
                    viewModel.handleIntent(LogIntent.CopyAllLogs)
                },
                onExportLogs = {
                    viewModel.handleIntent(LogIntent.ExportLogs)
                },
                onSendLogs = {
                    viewModel.handleIntent(LogIntent.SendLogs)
                }
            )
        },
        bottomBar = {
            LogBottomBar(
                fontSize = uiState.fontSize,
                minFontSize = 8f,
                maxFontSize = 56f,
                onIncreaseFontSize = {
                    viewModel.handleIntent(LogIntent.IncreaseFontSize)
                },
                onDecreaseFontSize = {
                    viewModel.handleIntent(LogIntent.DecreaseFontSize)
                },
                onScrollToTop = {
                    // 简单滚动：直接在 UI 中处理
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                onScrollToBottom = {
                    // 简单滚动：直接在 UI 中处理
                    coroutineScope.launch { listState.animateScrollToItem(uiState.logs.size) }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .pinchToZoom(
                    onZoomIn = { viewModel.handleIntent(LogIntent.IncreaseFontSize) },
                    onZoomOut = { viewModel.handleIntent(LogIntent.DecreaseFontSize) }
                )
        ) {
            if (uiState.isEmpty) {
                EmptyLogState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(
                        items = uiState.logs,
                        key = { it.id }
                    ) { logEntry ->
                        LogItemView(
                            item = logEntry,
                            fontSize = uiState.fontSize,
                            logColor = viewModel.getLogColor(logEntry.level),
                            logLevelName = viewModel.getLogLevelName(logEntry.level)
                        )
                    }
                }
            }
        }
    }
}


/**
 * 双指缩放修饰符
 */
@Composable
private fun Modifier.pinchToZoom(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
): Modifier {
    var scale by remember { mutableFloatStateOf(1f) }
    
    return pointerInput(Unit) {
        detectTransformGestures { _, _, zoom, _ ->
            val newScale = scale * zoom
            if (newScale > 0.9f && newScale < 1.1f) {
                scale = newScale
                if (zoom > 1.0f) {
                    onZoomIn()
                } else if (zoom < 1.0f) {
                    onZoomOut()
                }
            }
        }
    }
}

/**
 * 顶部工具栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogTopBar(
    onNavigateToSettings: () -> Unit,
    onClearLogs: () -> Unit,
    onCopyAllLogs: () -> Unit,
    onExportLogs: () -> Unit,
    onSendLogs: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = {
            Text(
                text = "日志",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onCopyAllLogs) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制所有日志"
                )
            }
            IconButton(onClick = onExportLogs) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "导出日志"
                )
            }
            IconButton(onClick = onSendLogs) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送日志"
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("设置") },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("清除日志") },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onClearLogs()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * 底部工具栏
 */
@Composable
private fun LogBottomBar(
    fontSize: Float,
    minFontSize: Float,
    maxFontSize: Float,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onScrollToTop: () -> Unit,
    onScrollToBottom: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecreaseFontSize,
                enabled = fontSize > minFontSize
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "减小字体"
                )
            }
            
            Text(
                text = "${fontSize.toInt()}sp",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp)
            )
            
            IconButton(
                onClick = onIncreaseFontSize,
                enabled = fontSize < maxFontSize
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "增大字体"
                )
            }
            
            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 8.dp)
            )
            
            IconButton(onClick = onScrollToTop) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "滚动到顶部"
                )
            }
            
            IconButton(onClick = onScrollToBottom) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "滚动到底部"
                )
            }
        }
    }
}

/**
 * 单条日志项
 */
@Composable
private fun LogItemView(
    item: LogItem,
    fontSize: Float,
    logColor: Int,
    logLevelName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 日志级别标签
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(logColor),
                modifier = Modifier.alignByBaseline()
            ) {
                Text(
                    text = logLevelName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            // 日志内容
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyLogState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Message,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "暂无日志",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
