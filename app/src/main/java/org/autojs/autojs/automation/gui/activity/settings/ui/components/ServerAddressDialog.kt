package org.autojs.autojs.ui.modern.activity.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.autojs.autojs.core.pref.Pref

/**
 * 服务器地址输入对话框
 * 
 * @param history 历史记录列表
 * @param prefill 预填充的地址
 * @param onDismiss 关闭对话框回调
 * @param onConnect 连接回调
 * @param onRemoveHistory 移除历史记录回调
 * @param onClearHistory 清空历史记录回调
 */
@Composable
fun ServerAddressDialog(
    history: List<String>,
    prefill: String = Pref.getServerAddress(),
    onDismiss: () -> Unit,
    onConnect: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var inputValue by remember { mutableStateOf(TextFieldValue(prefill)) }
    var showHistory by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 显示历史记录对话框
    if (showHistory) {
        HistoryListDialog(
            history = history,
            onDismiss = { showHistory = false },
            onSelect = { address ->
                inputValue = TextFieldValue(address)
                showHistory = false
            },
            onRemove = onRemoveHistory,
            onClear = {
                showHistory = false
                showClearConfirmDialog = true
            }
        )
    }
    
    // 显示清空确认对话框
    if (showClearConfirmDialog) {
        ClearHistoryConfirmDialog(
            onDismiss = { showClearConfirmDialog = false },
            onConfirm = {
                onClearHistory()
                showClearConfirmDialog = false
            }
        )
    }
    
    // 主输入对话框
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("服务器地址") },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { 
                        inputValue = it
                        errorMessage = null
                    },
                    label = { Text("IP地址或域名") },
                    placeholder = { Text("例如: 192.168.1.100") },
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.End) {
                // 历史记录按钮
                if (history.isNotEmpty()) {
                    TextButton(onClick = { showHistory = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "历史记录",
                            modifier = Modifier.width(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("历史")
                    }
                }
                
                // 连接按钮
                TextButton(onClick = {
                    val address = inputValue.text.trim()
                    if (address.isEmpty()) {
                        errorMessage = "服务器地址不能为空"
                        return@TextButton
                    }
                    
                    val validation = validateAddress(address)
                    if (validation.isValid) {
                        onConnect(address)
                    } else {
                        errorMessage = validation.errorMessage
                    }
                }) {
                    Text("连接")
                }
            }
        }
    )
}

/**
 * 历史记录列表对话框
 */
@Composable
private fun HistoryListDialog(
    history: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("历史记录") },
        text = {
            if (history.isEmpty()) {
                Text(
                    text = "暂无历史记录",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(history) { address ->
                        HistoryListItem(
                            address = address,
                            onClick = { onSelect(address) },
                            onRemove = { onRemove(address) }
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("返回")
            }
        },
        confirmButton = {
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空",
                        modifier = Modifier.width(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空")
                }
            }
        }
    )
}

/**
 * 历史记录列表项
 */
@Composable
private fun HistoryListItem(
    address: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = address,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 清空历史记录确认对话框
 */
@Composable
private fun ClearHistoryConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认清空") },
        text = { Text("确定要清空所有历史记录吗？") },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认")
            }
        }
    )
}

/**
 * 验证地址格式
 */
private fun validateAddress(address: String): ValidationResult {
    // IPv4格式: 192.168.1.100 或 192.168.1.100:6347
    val ipv4Pattern = Regex("""^(\d{1,3}\.){3}\d{1,3}(:\d{1,5})?$""")
    
    // IPv6格式: [::1] 或 [::1]:6347 或 fe80::1
    val ipv6Pattern = Regex("""^(\[?[0-9a-fA-F:]+\]?)(:\d{1,5})?$""")
    
    // 域名格式: example.com 或 example.com:8080
    val domainPattern = Regex("""^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)*[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(:\d{1,5})?$""")
    
    return when {
        ipv4Pattern.matches(address) -> ValidationResult(true, null)
        ipv6Pattern.matches(address) -> ValidationResult(true, null)
        domainPattern.matches(address) -> ValidationResult(true, null)
        else -> ValidationResult(false, "地址格式无效，请输入有效的IP地址或域名")
    }
}

private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)