package org.autojs.autojs.ui.modern.activity.settings.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.autojs.autojs.pluginclient.JsonSocketClient
import org.autojs.autojs.pluginclient.JsonSocketServer
import org.autojs.autojs.ui.modern.activity.settings.model.DeveloperIntent
import org.autojs.autojs.ui.modern.activity.settings.model.DeveloperUiState
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsListItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsNavigationItem
import org.autojs.autojs.ui.modern.activity.settings.model.SettingsSwitchItem
import org.autojs.autojs.ui.modern.activity.settings.ui.components.ServerAddressDialog
import org.autojs.autojs.ui.modern.activity.settings.ui.components.SettingsGroupCard
import org.autojs.autojs.ui.modern.activity.settings.viewmodel.DeveloperViewModel

/**
 * 开发者选项页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    viewModel: DeveloperViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 显示Toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    
    // 对话框显示状态
    var showConnectDialog by remember { mutableStateOf(false) }
    
    // 监听对话框事件
    LaunchedEffect(Unit) {
        viewModel.showDialog.collect { event ->
            when (event) {
                is DeveloperViewModel.DialogEvent.ShowConnectDialog -> {
                    showConnectDialog = true
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开发者选项") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 客户端模式
            item {
                ClientModeCard(
                    uiState = uiState,
                    onToggleClientMode = { isChecked ->
                        if (isChecked) {
                            viewModel.handleIntent(DeveloperIntent.ShowConnectDialog)
                        } else {
                            viewModel.handleIntent(DeveloperIntent.DisconnectClient)
                        }
                    },
                    onShowHistoryDialog = {
                        showConnectDialog = true
                    }
                )
            }
            
            // 服务端模式
            item {
                ServerModeCard(
                    uiState = uiState,
                    onToggleServerMode = { isChecked ->
                        if (isChecked) {
                            viewModel.handleIntent(DeveloperIntent.StartServer)
                        } else {
                            viewModel.handleIntent(DeveloperIntent.StopServer)
                        }
                    }
                )
            }
        }
    }
    
    // 服务器地址对话框
    if (showConnectDialog) {
        ServerAddressDialog(
            history = uiState.clientState.addressHistory,
            prefill = uiState.clientState.serverAddress,
            onDismiss = { showConnectDialog = false },
            onConnect = { address ->
                viewModel.handleIntent(DeveloperIntent.ConnectToServer(address))
                showConnectDialog = false
            },
            onRemoveHistory = { address ->
                viewModel.handleIntent(DeveloperIntent.RemoveHistoryAddress(address))
            },
            onClearHistory = {
                viewModel.handleIntent(DeveloperIntent.ClearHistoryAddresses)
            }
        )
    }
}

/**
 * 客户端模式卡片
 */
@Composable
private fun ClientModeCard(
    uiState: DeveloperUiState,
    onToggleClientMode: (Boolean) -> Unit,
    onShowHistoryDialog: () -> Unit
) {
    val clientState = uiState.clientState
    val isConnected = clientState.connectionState == DeveloperUiState.ConnectionState.CONNECTED
    val isConnecting = clientState.connectionState == DeveloperUiState.ConnectionState.CONNECTING
    
    SettingsGroupCard(
        title = "客户端模式",
        items = buildList {
            // 连接开关
            add(
                SettingsSwitchItem(
                    title = "连接到电脑",
                    description = when {
                        isConnecting -> "正在连接..."
                        isConnected -> "已连接到 ${clientState.serverAddress}"
                        else -> "连接到电脑进行远程控制"
                    },
                    icon = Icons.Default.Computer,
                    isChecked = isConnected,
                    onCheckedChange = onToggleClientMode
                )
            )
            
            // 如果已连接，显示服务器地址
            if (isConnected) {
                add(
                    SettingsListItem(
                        title = "服务器地址",
                        description = "当前连接的服务器地址",
                        icon = Icons.Default.Dns,
                        value = clientState.serverAddress,
                        selectedValue = clientState.serverAddress,
                        options = clientState.addressHistory.map { address ->
                            org.autojs.autojs.ui.modern.activity.settings.model.SettingsListOption(
                                title = address,
                                value = address
                            )
                        },
                        onValueChange = { },
                        neutralButtonText = "历史记录",
                        onNeutralButtonClick = onShowHistoryDialog
                    )
                )
            }
        }
    )
}

/**
 * 服务端模式卡片
 */
@Composable
private fun ServerModeCard(
    uiState: DeveloperUiState,
    onToggleServerMode: (Boolean) -> Unit
) {
    val serverState = uiState.serverState
    val isConnected = serverState.connectionState == DeveloperUiState.ConnectionState.CONNECTED
    val isStarting = serverState.connectionState == DeveloperUiState.ConnectionState.CONNECTING
    
    val description = buildString {
        if (isStarting) {
            append("正在启动...")
        } else if (isConnected) {
            append(serverState.serverIp)
            if (serverState.connectionCount > 0) {
                append(" [已连接: ${serverState.connectionCount}]")
            }
        } else {
            append("启动服务端供电脑连接")
        }
    }
    
    SettingsGroupCard(
        title = "服务端模式",
        items = listOf(
            SettingsSwitchItem(
                title = "启动服务端",
                description = description,
                icon = Icons.Default.PhoneAndroid,
                isChecked = isConnected,
                onCheckedChange = onToggleServerMode
            )
        )
    )
}