package org.autojs.autojs.ui.modern.activity.explorer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.autojs.autojs.AutoJs
import org.autojs.autojs.app.GlobalAppContext
import org.autojs.autojs.execution.ScriptExecution
import org.autojs.autojs.execution.ScriptExecutionListener
import org.autojs.autojs.model.explorer.Explorer
import org.autojs.autojs.model.explorer.ExplorerDirPage
import org.autojs.autojs.model.explorer.ExplorerFileItem
import org.autojs.autojs.model.explorer.ExplorerPage
import org.autojs.autojs.model.explorer.Explorers
import org.autojs.autojs.model.script.Scripts
import org.autojs.autojs.project.ProjectConfig
import org.autojs.autojs.ui.modern.activity.explorer.ui.ExplorerIntent
import org.autojs.autojs.ui.modern.activity.explorer.ui.ExplorerUiState
import org.autojs.autojs.util.WorkingDirectoryUtils
import java.io.File

/**
 * 文件浏览器 ViewModel
 * 包含状态管理和业务逻辑
 */
class ExplorerViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "ExplorerViewModel"
    }
    
    // ==================== 状态 ====================
    
    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()
    
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    
    // ==================== 依赖 ====================
    
    private val explorer: Explorer = Explorers.workspace()
    private val scriptEngineService = AutoJs.instance.scriptEngineService
    
    // ==================== 内部状态 ====================
    
    private var currentPage: ExplorerPage? = null
    private val runningScripts = mutableMapOf<String, ScriptExecution>()
    private val disposables = CompositeDisposable()
    
    init {
        android.util.Log.d(TAG, "初始化 ExplorerViewModel")
        
        // 加载默认目录
        handleIntent(ExplorerIntent.LoadDirectory(WorkingDirectoryUtils.path))
        
        // 监听脚本执行状态
        observeScriptExecutions()
    }
    
    // ==================== 意图处理 ====================
    
    /**
     * 处理 UI 意图
     */
    fun handleIntent(intent: ExplorerIntent) {
        android.util.Log.d(TAG, "处理 Intent: $intent")
        
        when (intent) {
            is ExplorerIntent.LoadDirectory -> loadDirectory(intent.path)
            is ExplorerIntent.GoBack -> goBack()
            is ExplorerIntent.GoUp -> goUp()
            is ExplorerIntent.EnterDirectory -> enterDirectory(intent.item)
            is ExplorerIntent.RunScript -> runScript(intent.item)
            is ExplorerIntent.StopScript -> stopScript(intent.item)
            is ExplorerIntent.EditScript -> editScript(intent.item)
            is ExplorerIntent.Refresh -> refreshCurrentDirectory()
        }
    }
    
    // ==================== 业务逻辑 ====================
    
    /**
     * 加载目录
     */
    private fun loadDirectory(path: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        val page = ExplorerDirPage.createRoot(path)
        currentPage = page
        
        explorer.fetchChildren(page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { loadedPage ->
                    val dirs = mutableListOf<ExplorerPage>()
                    val files = mutableListOf<ExplorerFileItem>()
                    
                    loadedPage.forEach { item ->
                        when (item) {
                            is ExplorerPage -> dirs.add(item)
                            is ExplorerFileItem -> files.add(item)
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        currentPath = path,
                        dirs = dirs,
                        files = files,
                        isLoading = false,
                        canGoUp = canGoUp(path),
                        canGoBack = _uiState.value.history.isNotEmpty()
                    )
                },
                { error ->
                    android.util.Log.e(TAG, "目录加载失败", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "加载失败"
                    )
                }
            )
            .let { disposables.add(it) }
    }
    
    /**
     * 进入子目录
     */
    private fun enterDirectory(item: ExplorerPage) {
        val currentPath = _uiState.value.currentPath
        _uiState.value = _uiState.value.copy(history = _uiState.value.history + currentPath)
        handleIntent(ExplorerIntent.LoadDirectory(item.path))
    }
    
    /**
     * 返回上一级
     */
    private fun goBack() {
        val history = _uiState.value.history
        if (history.isNotEmpty()) {
            val previousPath = history.last()
            val newHistory = history.dropLast(1)
            _uiState.value = _uiState.value.copy(history = newHistory)
            handleIntent(ExplorerIntent.LoadDirectory(previousPath))
        }
    }
    
    /**
     * 向上到父目录
     */
    private fun goUp() {
        val currentPath = _uiState.value.currentPath
        val parentPath = File(currentPath).parent
        
        if (parentPath != null && parentPath != currentPath) {
            handleIntent(ExplorerIntent.LoadDirectory(parentPath))
        }
    }
    
    /**
     * 执行脚本
     */
    private fun runScript(item: ExplorerFileItem) {
        if (runningScripts.containsKey(item.path)) {
            _toastMessage.value = "脚本正在运行"
            return
        }
        
        viewModelScope.launch {
            try {
                val scriptFile = item.toScriptFile()
                val execution = Scripts.run(context, scriptFile)
                
                if (execution != null) {
                    runningScripts[item.path] = execution
                    _toastMessage.value = "开始执行: ${item.name}"
                } else {
                    _toastMessage.value = "执行失败"
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "执行脚本失败", e)
                _toastMessage.value = "执行失败: ${e.message}"
            }
        }
    }
    
    /**
     * 停止脚本
     */
    private fun stopScript(item: ExplorerFileItem) {
        val execution = runningScripts[item.path]
        if (execution != null) {
            try {
                execution.engine?.forceStop()
                runningScripts.remove(item.path)
                _toastMessage.value = "已停止: ${item.name}"
            } catch (e: Exception) {
                android.util.Log.e(TAG, "停止脚本失败", e)
                _toastMessage.value = "停止失败: ${e.message}"
            }
        }
    }
    
    /**
     * 编辑脚本
     */
    private fun editScript(item: ExplorerFileItem) {
        viewModelScope.launch {
            try {
                val scriptFile = item.toScriptFile()
                Scripts.edit(context, scriptFile)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "编辑脚本失败", e)
                _toastMessage.value = "编辑失败: ${e.message}"
            }
        }
    }
    
    /**
     * 刷新当前目录
     */
    private fun refreshCurrentDirectory() {
        val currentPath = _uiState.value.currentPath
        if (currentPath.isNotEmpty()) {
            handleIntent(ExplorerIntent.LoadDirectory(currentPath))
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断是否可以向上
     */
    private fun canGoUp(path: String): Boolean {
        val parentPath = File(path).parent
        return parentPath != null && parentPath != path
    }
    
    /**
     * 判断是否是项目
     */
    fun isProject(item: ExplorerPage): Boolean {
        return ProjectConfig.isProject(item)
    }
    
    /**
     * 运行项目
     */
    fun runProject(item: ExplorerPage) {
        viewModelScope.launch {
            try {
                val projectConfig = ProjectConfig.fromProjectDir(item.path)
                if (projectConfig == null) {
                    _toastMessage.value = "项目配置无效"
                    return@launch
                }
                
                val mainScriptFileName = projectConfig.mainScriptFileName
                    ?: ProjectConfig.DEFAULT_MAIN_SCRIPT_FILE_NAME
                val mainScriptFile = File(item.path, mainScriptFileName)
                
                if (!mainScriptFile.exists()) {
                    _toastMessage.value = "主脚本文件不存在: $mainScriptFileName"
                    return@launch
                }
                
                val scriptFile = org.autojs.autojs.model.script.ScriptFile(mainScriptFile)
                val execution = Scripts.run(context, scriptFile)
                
                if (execution != null) {
                    runningScripts[mainScriptFile.path] = execution
                    _toastMessage.value = "开始执行项目: ${item.name}"
                } else {
                    _toastMessage.value = "执行失败"
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "执行项目失败", e)
                _toastMessage.value = "执行失败: ${e.message}"
            }
        }
    }
    
    /**
     * 判断脚本是否正在运行
     */
    fun isScriptRunning(path: String): Boolean {
        return runningScripts.containsKey(path)
    }
    
    /**
     * 监听脚本执行状态
     */
    private fun observeScriptExecutions() {
        val listener = object : ScriptExecutionListener {
            override fun onStart(execution: ScriptExecution) {
                val path = execution.source.toString()
                runningScripts[path] = execution
            }
            
            override fun onSuccess(execution: ScriptExecution, result: Any?) {
                val path = execution.source.toString()
                runningScripts.remove(path)
            }
            
            override fun onException(execution: ScriptExecution, e: Throwable?) {
                val path = execution.source.toString()
                runningScripts.remove(path)
            }
        }
        
        scriptEngineService.registerGlobalScriptExecutionListener(listener)
    }
    
    /**
     * 获取上下文
     */
    private val context: Context
        get() = GlobalAppContext.get()
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}