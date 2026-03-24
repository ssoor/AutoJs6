package org.autojs.autojs.ui.modern.activity.task

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.autojs.autojs.AutoJs
import org.autojs.autojs.execution.ScriptExecution
import org.autojs.autojs.execution.ScriptExecutionListener
import org.autojs.autojs6.R

/**
 * 任务 ViewModel
 */
class TaskViewModel() : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    // Toast 消息
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // RxJava 订阅
    private val disposables = CompositeDisposable()
    
    init {
        observeTaskChanges()
        refreshTasks()
    }
    
    /**
     * 处理 Intent
     */
    fun handleIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.Refresh -> refreshTasks()
            is TaskIntent.StopAll -> stopAllTasks()
            is TaskIntent.StopTask -> stopTask(intent.taskId)
            is TaskIntent.ShowTaskDetail -> showTaskDetail(intent.taskId)
            is TaskIntent.OpenScriptFile -> openScriptFile(intent.scriptPath)
            is TaskIntent.RestartTask -> restartTask(intent.taskId, intent.scriptPath)
            is TaskIntent.ShowTaskLog -> showTaskLog(intent.taskId)
        }
    }
    
    /**
     * 观察任务变化
     */
    private fun observeTaskChanges() {
        // 监听定时任务变化
        org.autojs.autojs.timing.TimedTaskManager.timeTaskChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ ->
                viewModelScope.launch {
                    refreshPendingTasks()
                }
            }
            .let { disposables.add(it) }
        
        // 监听 Intent 任务变化
        org.autojs.autojs.timing.TimedTaskManager.intentTaskChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _ ->
                viewModelScope.launch {
                    refreshPendingTasks()
                }
            }
            .let { disposables.add(it) }
        
        // 监听脚本执行变化
        val executionListener = object : ScriptExecutionListener {
            override fun onStart(execution: ScriptExecution) {
                viewModelScope.launch {
                    addRunningTask(execution)
                }
            }
            
            override fun onSuccess(execution: ScriptExecution, result: Any?) {
                viewModelScope.launch {
                    removeRunningTask(execution.id)
                }
            }
            
            override fun onException(execution: ScriptExecution, e: Throwable?) {
                viewModelScope.launch {
                    removeRunningTask(execution.id)
                }
            }
        }
        
        AutoJs.instance.scriptEngineService
            .registerGlobalScriptExecutionListener(executionListener)
    }
    
    /**
     * 停止所有任务
     */
    private fun stopAllTasks() {
        AutoJs.instance.scriptEngineService.stopAllAndToast()
        _toastMessage.value = "已停止所有任务"
    }
    
    /**
     * 停止指定任务
     */
    private fun stopTask(taskId: Long) {
        // 尝试停止运行中的脚本
        val currentTasks = _uiState.value.runningTasks
        val task = currentTasks.find { it.id == taskId }
        
        if (task != null && task.type == TaskType.RUNNING) {
            AutoJs.instance.scriptEngineService
                .getScriptExecutions()
                .find { it.id == taskId.toInt() }
                ?.engine?.forceStop()
            _toastMessage.value = "已停止任务"
        } else {
            // 停止待执行任务
            val timedTask = org.autojs.autojs.timing.TimedTaskManager.getTimedTask(taskId)
            if (timedTask != null) {
                org.autojs.autojs.timing.TimedTaskManager.removeTask(timedTask)
                _toastMessage.value = "已删除定时任务"
            } else {
                val intentTask = org.autojs.autojs.timing.TimedTaskManager.getIntentTask(taskId)
                if (intentTask != null) {
                    org.autojs.autojs.timing.TimedTaskManager.removeTask(intentTask)
                    _toastMessage.value = "已删除定时任务"
                }
            }
        }
    }
    
    /**
     * 查看任务详情
     */
    private fun showTaskDetail(taskId: Long) {
        val task = _uiState.value.runningTasks.find { it.id == taskId }
            ?: _uiState.value.pendingTasks.find { it.id == taskId }
        
        task?.let {
            _toastMessage.value = "任务详情：${it.name}"
            // TODO: 打开详情对话框
        }
    }
    
    /**
     * 打开脚本文件
     */
    private fun openScriptFile(scriptPath: String) {
        // TODO: 跳转到编辑器
        _toastMessage.value = "打开脚本：$scriptPath"
    }
    
    /**
     * 重启任务
     */
    private fun restartTask(taskId: Long, scriptPath: String) {
        // 先停止任务
        stopTask(taskId)
        
        // 重新执行
        viewModelScope.launch {
            try {
                val intent = android.content.Intent().apply {
                    putExtra(org.autojs.autojs.external.ScriptIntents.EXTRA_KEY_PATH, scriptPath)
                }
                val appContext = org.autojs.autojs.app.GlobalAppContext.get()
                org.autojs.autojs.external.ScriptIntents.handleIntent(appContext, intent)
                _toastMessage.value = "任务已重启"
            } catch (e: Exception) {
                _toastMessage.value = "重启失败：${e.message}"
            }
        }
    }
    
    /**
     * 查看任务日志
     */
    private fun showTaskLog(taskId: Long) {
        // TODO: 跳转到日志页面
        _toastMessage.value = "查看任务日志：$taskId"
    }
    
    /**
     * 刷新所有任务
     */
    private fun refreshTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            val runningTasks = AutoJs.instance.scriptEngineService
                .getScriptExecutions()
                .map { it.toTaskItem() }
            
            val pendingTasks = mutableListOf<TaskUiState.TaskItem>()
            
            org.autojs.autojs.timing.TimedTaskManager.allTasksAsList.forEach { task ->
                pendingTasks.add(task.toTaskItem())
            }
            
            org.autojs.autojs.timing.TimedTaskManager.allIntentTasksAsList.forEach { task ->
                pendingTasks.add(task.toTaskItem())
            }
            
            _uiState.update {
                it.copy(
                    runningTasks = runningTasks,
                    pendingTasks = pendingTasks,
                    isRefreshing = false
                )
            }
        }
    }
    
    /**
     * 刷新待执行任务
     */
    private fun refreshPendingTasks() {
        viewModelScope.launch {
            val pendingTasks = mutableListOf<TaskUiState.TaskItem>()
            
            org.autojs.autojs.timing.TimedTaskManager.allTasksAsList.forEach { task ->
                pendingTasks.add(task.toTaskItem())
            }
            
            org.autojs.autojs.timing.TimedTaskManager.allIntentTasksAsList.forEach { task ->
                pendingTasks.add(task.toTaskItem())
            }
            
            _uiState.update { it.copy(pendingTasks = pendingTasks) }
        }
    }
    
    private fun addRunningTask(execution: ScriptExecution) {
        _uiState.update { it.copy(
            runningTasks = it.runningTasks + execution.toTaskItem()
        ) }
    }
    
    private fun removeRunningTask(id: Int) {
        _uiState.update { it.copy(
            runningTasks = it.runningTasks.filter { it.id != id.toLong() }
        ) }
    }
    
    // ==================== 数据转换 ====================
    
    private fun ScriptExecution.toTaskItem(): TaskUiState.TaskItem {
        return TaskUiState.TaskItem(
            id = id.toLong(),
            name = source.name,
            desc = source.elegantPath,
            scriptPath = source.toString(),
            engineName = source.engineName ?: "Unknown",
            icon = getIconForEngine(source.engineName ?: "Unknown"),
            type = TaskType.RUNNING,
            startTime = System.currentTimeMillis()
        )
    }
    
    private fun org.autojs.autojs.timing.TimedTask.toTaskItem(): TaskUiState.TaskItem {
        val engineName = inferEngineName(scriptPath)
        return TaskUiState.TaskItem(
            id = id,
            name = getElegantPath(scriptPath),
            desc = "下次运行: ${formatNextTime(nextTime)}",
            scriptPath = scriptPath,
            engineName = engineName,
            icon = getIconForEngine(engineName),
            type = TaskType.TIMED_TASK,
            nextRunTime = nextTime
        )
    }
    
    private fun org.autojs.autojs.timing.IntentTask.toTaskItem(): TaskUiState.TaskItem {
        val engineName = inferEngineName(scriptPath)
        return TaskUiState.TaskItem(
            id = id,
            name = action,
            desc = "Intent 任务",
            scriptPath = scriptPath,
            engineName = engineName,
            icon = getIconForEngine(engineName),
            type = TaskType.INTENT_TASK
        )
    }
    
    private fun getIconForEngine(engineName: String): Int {
        return when (engineName) {
            "JavaScript", "org.autojs.autojs.script.JavaScriptSource" -> android.R.drawable.ic_menu_edit
            "Auto", "org.autojs.autojs.script.AutoFileSource" -> android.R.drawable.ic_menu_agenda
            else -> android.R.drawable.ic_menu_info_details
        }
    }
    
    private fun formatNextTime(nextTime: Long): String {
        val formatter = org.joda.time.format.DateTimeFormat
            .forPattern("yyyy/MM/dd HH:mm")
        return formatter.print(org.joda.time.DateTime(nextTime))
    }
    
    /**
     * 获取优雅路径
     */
    private fun getElegantPath(path: String): String {
        val workingDir = org.autojs.autojs.util.WorkingDirectoryUtils.path
        return if (path.startsWith(workingDir)) {
            path.substring(workingDir.length).trimStart('/')
        } else {
            path
        }
    }
    
    /**
     * 推断引擎名称
     */
    private fun inferEngineName(scriptPath: String): String {
        return if (scriptPath.endsWith(".js")) {
            "JavaScript"
        } else {
            "Auto"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
