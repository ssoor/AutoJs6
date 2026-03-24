package org.autojs.autojs.ui.modern.activity.explorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.autojs.autojs.model.explorer.ExplorerFileItem
import org.autojs.autojs.pio.PFile
import org.autojs.autojs.pio.PFiles

/**
 * 文件项组件
 */
@Composable
fun FileItem(
    item: ExplorerFileItem,
    isRunning: Boolean,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit
) {
    val type = item.getType()
    val isExecutable = type.isExecutable()
    val isTextEditable = type.isTextEditable()
    val isInstallable = type.isInstallable()
    val isMediaPlayable = type.isMediaPlayable()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标（根据类型显示不同图标）
            Icon(
                imageVector = when {
                    isRunning -> Icons.Default.Circle
                    isExecutable -> Icons.Default.PlayArrow
                    isTextEditable -> Icons.Default.Edit
                    isInstallable -> Icons.Default.Android
                    isMediaPlayable -> Icons.Default.PlayCircle
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = when {
                    isRunning -> MaterialTheme.colorScheme.primary
                    isExecutable -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )
            
            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isRunning) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${PFile.getFullDateString(item.lastModified())} · ${PFiles.getHumanReadableSize(item.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 显示文件类型标签
                if (isExecutable || isTextEditable || isInstallable || isMediaPlayable) {
                    Text(
                        text = when {
                            isExecutable -> "可执行"
                            isTextEditable -> "可编辑"
                            isInstallable -> "可安装"
                            isMediaPlayable -> "可播放"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 操作按钮
            if (isExecutable) {
                if (isRunning) {
                    IconButton(onClick = onStop) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(onClick = onRun) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "运行",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            if (isTextEditable) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}