package org.autojs.autojs.ui.modern.activity.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.autojs.autojs.ui.modern.activity.task.TaskIntent
import org.autojs.autojs.ui.modern.activity.task.TaskUiState

/**
 * 任务管理屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.isEmpty) {
                EmptyTaskState(onRefresh = { viewModel.handleIntent(TaskIntent.Refresh) })
            } else {
                TaskList(
                    runningTasks = uiState.runningTasks,
                    pendingTasks = uiState.pendingTasks,
                    onTaskAction = { taskId, action ->
                        when (action) {
                            TaskAction.Stop -> viewModel.handleIntent(TaskIntent.StopTask(taskId))
                            TaskAction.Restart -> {
                                val task = uiState.runningTasks.find { it.id == taskId }
                                task?.let {
                                    viewModel.handleIntent(TaskIntent.RestartTask(taskId, task.scriptPath))
                                }
                            }
                            TaskAction.Detail -> viewModel.handleIntent(TaskIntent.ShowTaskDetail(taskId))
                            TaskAction.OpenFile -> {
                                val task = uiState.runningTasks.find { it.id == taskId }
                                    ?: uiState.pendingTasks.find { it.id == taskId }
                                task?.let {
                                    viewModel.handleIntent(TaskIntent.OpenScriptFile(task.scriptPath))
                                }
                            }
                            TaskAction.Log -> viewModel.handleIntent(TaskIntent.ShowTaskLog(taskId))
                        }
                    },
                    onRefresh = { viewModel.handleIntent(TaskIntent.Refresh) }
                )
            }
        }
    }
}

/**
 * 任务操作
 */
enum class TaskAction {
    Stop,      // 停止
    Restart,   // 重启
    Detail,    // 详情
    OpenFile,  // 打开文件
    Log        // 查看日志
}

@Composable
private fun TaskList(
    runningTasks: List<TaskUiState.TaskItem>,
    pendingTasks: List<TaskUiState.TaskItem>,
    onTaskAction: (Long, TaskAction) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (runningTasks.isNotEmpty()) {
            item {
                TaskGroupHeader(title = "运行中 (${runningTasks.size})", icon = Icons.Default.PlayArrow)
            }
            items(runningTasks, key = { it.id }) { task ->
                TaskItem(task = task, onAction = { onTaskAction(task.id, it) })
            }
        }
        
        if (pendingTasks.isNotEmpty()) {
            item {
                TaskGroupHeader(title = "待执行 (${pendingTasks.size})", icon = Icons.Default.Schedule)
            }
            items(pendingTasks, key = { it.id }) { task ->
                TaskItem(task = task, onAction = { onTaskAction(task.id, it) })
            }
        }
    }
}

@Composable
private fun TaskGroupHeader(title: String, icon: ImageVector) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TaskItem(
    task: TaskUiState.TaskItem,
    onAction: (TaskAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = task.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("引擎: ${task.engineName}", style = MaterialTheme.typography.labelSmall)
            }
            
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多")
            }
        }
    }
    
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("停止") },
            leadingIcon = { Icon(Icons.Default.Stop, contentDescription = null) },
            onClick = {
                onAction(TaskAction.Stop)
                showMenu = false
            }
        )
        
        if (task.type == TaskType.RUNNING) {
            DropdownMenuItem(
                text = { Text("重启") },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                onClick = {
                    onAction(TaskAction.Restart)
                    showMenu = false
                }
            )
        }
        
        DropdownMenuItem(
            text = { Text("详情") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            onClick = {
                onAction(TaskAction.Detail)
                showMenu = false
            }
        )
        
        DropdownMenuItem(
            text = { Text("打开文件") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
            onClick = {
                onAction(TaskAction.OpenFile)
                showMenu = false
            }
        )
        
        if (task.type == TaskType.RUNNING) {
            DropdownMenuItem(
                text = { Text("查看日志") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                onClick = {
                    onAction(TaskAction.Log)
                    showMenu = false
                }
            )
        }
    }
}

@Composable
private fun EmptyTaskState(onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(64.dp))
            Text("暂无任务")
            Text("运行中的脚本和定时任务将显示在这里")
            Button(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("刷新")
            }
        }
    }
}
