package org.autojs.autojs.ui.modern.activity.explorer.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.autojs.autojs.ui.modern.activity.explorer.ui.components.DirectoryItem
import org.autojs.autojs.ui.modern.activity.explorer.ui.components.ExplorerTopBar
import org.autojs.autojs.ui.modern.activity.explorer.ui.components.FileItem
import org.autojs.autojs.ui.modern.activity.explorer.viewmodel.ExplorerViewModel

/**
 * 文件浏览器 Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    modifier: Modifier = Modifier,
    viewModel: ExplorerViewModel = viewModel(),
    onNavigateToEditor: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 显示 Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            ExplorerTopBar(
                currentPath = uiState.currentPath,
                canGoBack = uiState.canGoBack,
                canGoUp = uiState.canGoUp,
                onGoBack = { viewModel.handleIntent(ExplorerIntent.GoBack) },
                onGoUp = { viewModel.handleIntent(ExplorerIntent.GoUp) },
                onRefresh = { viewModel.handleIntent(ExplorerIntent.Refresh) }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error!!
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                uiState.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "目录为空",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 目录列表
                        if (uiState.dirs.isNotEmpty()) {
                            items(
                                items = uiState.dirs,
                                key = { it.path }
                            ) { dir ->
                                val isProject = viewModel.isProject(dir)
                                DirectoryItem(
                                    item = dir,
                                    isProject = isProject,
                                    onClick = { 
                                        viewModel.handleIntent(ExplorerIntent.EnterDirectory(dir))
                                    },
                                    onRun = if (isProject) {
                                        { viewModel.runProject(dir) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                        
                        // 文件列表
                        if (uiState.files.isNotEmpty()) {
                            items(
                                items = uiState.files,
                                key = { it.path }
                            ) { file ->
                                FileItem(
                                    item = file,
                                    isRunning = viewModel.isScriptRunning(file.path),
                                    onRun = { 
                                        viewModel.handleIntent(ExplorerIntent.RunScript(file))
                                    },
                                    onStop = { 
                                        viewModel.handleIntent(ExplorerIntent.StopScript(file))
                                    },
                                    onEdit = { 
                                        viewModel.handleIntent(ExplorerIntent.EditScript(file))
                                        onNavigateToEditor(file.path)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}