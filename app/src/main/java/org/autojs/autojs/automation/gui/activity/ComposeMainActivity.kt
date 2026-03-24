package org.autojs.autojs.ui.modern.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.autojs.autojs.ui.modern.screen.ExplorerScreen
import org.autojs.autojs.ui.modern.screen.DocumentationScreen
import org.autojs.autojs.ui.modern.theme.AutoJsTheme
import org.autojs.autojs.ui.modern.activity.task.TaskScreen

/**
 * Compose版本的主页面
 */
class ComposeMainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoJsTheme {
                ComposeMainScreen()
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, ComposeMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }
}

/**
 * 导航项
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Explorer : BottomNavItem("explorer", Icons.Default.Folder, "文件")
    object TaskManager : BottomNavItem("task_manager", Icons.Default.List, "任务")
    object Documentation : BottomNavItem("documentation", Icons.Default.Info, "文档")
    
    companion object {
        val items by lazy {
            listOf(Explorer, TaskManager, Documentation)
        }
    }
}

/**
 * 主页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMainScreen() {
    var selectedTab: BottomNavItem by remember { mutableStateOf(BottomNavItem.Explorer) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = { selectedTab = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
        ) {
            when (selectedTab) {
                BottomNavItem.Explorer -> ExplorerScreen()
                BottomNavItem.TaskManager -> TaskScreen()
                BottomNavItem.Documentation -> DocumentationScreen()
            }
        }
    }
}