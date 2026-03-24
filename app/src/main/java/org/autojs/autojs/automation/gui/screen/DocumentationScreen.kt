package org.autojs.autojs.ui.modern.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.activity.settings.ComposeSettingsActivity
import org.autojs.autojs.ui.modern.log.ComposeLogActivity

/**
 * 文档页面
 */
@Composable
fun DocumentationScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AutoJs6 文档",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "欢迎使用 AutoJs6 文档中心",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Text(
                text = "快速访问",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            DocItem(
                icon = Icons.Default.Terminal,
                title = "日志控制台",
                description = "查看脚本运行日志",
                onClick = { openLogActivity(context) }
            )
        }
        
        item {
            DocItem(
                icon = Icons.Default.Folder,
                title = "文件管理",
                description = "管理脚本文件",
                onClick = { /* 待实现 */ }
            )
        }
        
        item {
            DocItem(
                icon = Icons.Default.Settings,
                title = "设置",
                description = "应用设置",
                onClick = { openSettingsActivity(context) }
            )
        }
        
        item {
            DocItem(
                icon = Icons.Default.Info,
                title = "关于",
                description = "关于 AutoJs6",
                onClick = { /* 待实现 */ }
            )
        }
    }
}

/**
 * 文档项
 */
@Composable
private fun DocItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 打开日志页面
 */
private fun openLogActivity(context: Context) {
    ComposeLogActivity.launch(context)
}

/**
 * 打开设置页面
 */
private fun openSettingsActivity(context: Context) {
    ComposeSettingsActivity.launch(context)
}