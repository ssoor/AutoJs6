package org.autojs.autojs.ui.modern.log

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import org.autojs.autojs.inrt.launch.GlobalProjectLauncher
import org.autojs.autojs.ui.modern.activity.settings.ComposeSettingsActivity
import org.autojs.autojs.ui.modern.log.LogScreen
import org.autojs.autojs.ui.modern.log.LogViewModel
import org.autojs.autojs.ui.modern.log.ComposeLogActivity;
import org.autojs.autojs.ui.modern.theme.AutoJsTheme
import org.autojs.autojs6.R

/**
 * Compose 版本的日志 Activity
 */
class ComposeLogActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_LAUNCH_SCRIPT = "launch_script"
        
        fun launch(context: android.content.Context, launchScript: Boolean = false) {
            val intent = Intent(context, ComposeLogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) 
                putExtra(EXTRA_LAUNCH_SCRIPT, launchScript)
            }
            val options = android.app.ActivityOptions.makeCustomAnimation(
                context,
                org.autojs.autojs6.R.anim.no_anim_fade_in,
                org.autojs.autojs6.R.anim.no_anim_fade_out
            )
            context.startActivity(intent, options.toBundle())
        }
    }
    
    // ViewModel
    private val viewModel: LogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LogViewModel() as T
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AutoJsTheme {
                LogScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        // 简单导航：直接跳转到设置
                        navigateToSettings()
                    }
                )
            }
        }
        
        // 处理自动启动脚本
        if (intent.getBooleanExtra(EXTRA_LAUNCH_SCRIPT, false)) {
            GlobalProjectLauncher.launch(this)
        }
    }
    
    /**
     * 导航到设置页面
     * 简单导航直接在 Activity 中处理
     */
    private fun navigateToSettings() {
        ComposeSettingsActivity.launch(this)
    }
}