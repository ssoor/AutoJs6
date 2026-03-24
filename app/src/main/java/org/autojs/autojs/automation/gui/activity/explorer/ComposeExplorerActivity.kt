package org.autojs.autojs.ui.modern.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.autojs.autojs.ui.modern.component.EmptyState
import org.autojs.autojs.ui.modern.theme.AutoJsTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

/**
 * Compose版本的Explorer页面（占位，后续会实现）
 */
class ComposeExplorerActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoJsTheme {
                ExplorerPlaceholderScreen(onBackClick = { finish() })
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun launch(context: Context) {
            context.startActivity(Intent(context, ComposeExplorerActivity::class.java))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerPlaceholderScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件浏览器（Compose版本）") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(message = "Compose版本的文件浏览器正在开发中...")
        }
    }
}
