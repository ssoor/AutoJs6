@file:OptIn(ExperimentalMaterial3Api::class)

package org.autojs.autojs.ui.modern.activity.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.theme.AutoJsTheme
import org.autojs.autojs.ui.modern.activity.settings.ui.AppearanceScreen
import org.autojs.autojs.ui.modern.activity.settings.ui.DeveloperScreen
import org.autojs.autojs.ui.modern.activity.settings.ui.ServiceScreen

/**
 * Compose 版本的设置页面
 */
class ComposeSettingsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoJsTheme {
                SettingsScreen(onBackClick = { finish() })
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, ComposeSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}

/**
 * 设置页面入口
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }
    var showNotImplementedDialog by remember { mutableStateOf(false) }
    var pendingPage by remember { mutableStateOf<SettingsPage?>(null) }
    
    when (currentPage) {
        SettingsPage.MAIN -> {
            SettingsMainScreen(
                onPageClick = { page ->
                    when (page) {
                        SettingsPage.APPEARANCE -> currentPage = SettingsPage.APPEARANCE
                        SettingsPage.SERVICE -> currentPage = SettingsPage.SERVICE
                        SettingsPage.DEVELOPER -> currentPage = SettingsPage.DEVELOPER
                        else -> {
                            pendingPage = page
                            showNotImplementedDialog = true
                        }
                    }
                },
                onBackClick = onBackClick
            )
        }
        SettingsPage.APPEARANCE -> {
            AppearanceScreen(onBackClick = { currentPage = SettingsPage.MAIN })
        }
        SettingsPage.SERVICE -> {
            ServiceScreen(onBackClick = { currentPage = SettingsPage.MAIN })
        }
        SettingsPage.DEVELOPER -> {
            DeveloperScreen(onBackClick = { currentPage = SettingsPage.MAIN })
        }
        else -> {
            // 其他页面暂未实现
            SettingsMainScreen(
                onPageClick = { page ->
                    pendingPage = page
                    showNotImplementedDialog = true
                },
                onBackClick = onBackClick
            )
        }
    }
    
    // 暂未实现提示对话框
    if (showNotImplementedDialog) {
        NotImplementedDialog(
            pageName = pendingPage?.displayName ?: "",
            onDismiss = {
                showNotImplementedDialog = false
                pendingPage = null
            }
        )
    }
}

/**
 * 设置主页面
 */
@Composable
private fun SettingsMainScreen(
    onPageClick: (SettingsPage) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SettingsPage.entries) { page ->
                SettingsPageItem(
                    page = page,
                    onClick = { onPageClick(page) }
                )
            }
        }
    }
}

/**
 * 设置页面项
 */
@Composable
private fun SettingsPageItem(
    page: SettingsPage,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = page.displayName,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = page.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (page.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 暂未实现对话框
 */
@Composable
private fun NotImplementedDialog(
    pageName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("暂未实现") },
        text = { Text("【$pageName】功能暂未实现，敬请期待") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

/**
 * 设置页面枚举
 */
enum class SettingsPage(
    val displayName: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    MAIN("", "", Icons.Default.ArrowBack),
    APPEARANCE("常规", "应用语言、主题颜色、夜间模式等", Icons.Default.Palette),
    TOOLS("工具", "悬浮按钮、启动器快捷方式", Icons.Default.Build),
    SERVICE("服务", "前台服务、无障碍服务", Icons.Default.Phone),
    PERMISSION("权限", "推荐权限、系统权限、高级权限", Icons.Default.Security),
    SCRIPT("脚本", "文档来源、脚本运行、脚本编写", Icons.Default.Code),
    DEVELOPER("开发者选项", "客户端模式、服务端模式", Icons.Default.DeveloperMode),
    ABOUT("关于", "检查更新、关于应用和开发者", Icons.Default.Info)
}