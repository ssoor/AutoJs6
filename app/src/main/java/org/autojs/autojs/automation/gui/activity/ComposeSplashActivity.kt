package org.autojs.autojs.ui.modern.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.autojs.autojs.ui.modern.theme.AutoJsTheme
import org.autojs.autojs6.R
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.vector.ImageVector

// 基础动画库
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic

// 资源与图片库
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha

// 基础组件与布局（如果 Box 报错也请加上）
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import org.autojs.autojs.ui.floating.FloatyWindowManger
import org.autojs.autojs.ui.main.MainActivity
import org.autojs.autojs.ui.modern.activity.ComposeMainActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState

/**
 * Compose版本的Splash页面（占位，后续会实现）
 */
class ComposeSplashActivity : ComponentActivity() {
    private var mAlreadyEnterNextActivity = false
    private var mPaused = false
    private var jumpJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoJsTheme {
                SplashScreenContent(onEnterNewVersion = {
                    enterNextActivity()
                })
            }
        }

        // 隐藏圆形菜单
        FloatyWindowManger.hideCircularMenuIfNeeded()
    
        // 延迟后跳转到老版本
        jumpJob = lifecycleScope.launch {
            delay(INIT_TIMEOUT)
            if (!isFinishing && !isDestroyed) {
                enterComposeMain()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mPaused = true
    }

    override fun onResume() {
        super.onResume()
        if (mPaused) {
            mPaused = false
            enterComposeMain()
        }
    }   

    override fun onDestroy() {
        super.onDestroy()
        // 取消跳转协程，防止内存泄漏
        jumpJob?.cancel()
    }

    private fun enterNextActivity() {
        if (!mAlreadyEnterNextActivity && !mPaused && !isFinishing && !isDestroyed) {
            mAlreadyEnterNextActivity = true
            MainActivity.launch(this)
            finish()
        }
    }

    private fun enterComposeMain() {
        if (!mAlreadyEnterNextActivity && !mPaused && !isFinishing && !isDestroyed) {
            mAlreadyEnterNextActivity = true
            ComposeMainActivity.launch(this)
            finish()
        }
    }

    companion object {
        const val INIT_TIMEOUT = 3000L

        @JvmStatic
        fun launch(context: Context) {
            val intent = Intent(context, ComposeSplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

@Composable
fun SplashScreenContent(
    onEnterNewVersion: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Logo淡入动画
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, easing = EaseOutCubic)
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Logo图标
                Image(
                    painter = painterResource(id = R.drawable.autojs6_material),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .alpha(logoAlpha.value),
                    contentScale = ContentScale.Fit
                )
                
                // 应用名称
                Image(
                    painter = painterResource(id = R.drawable.autojs6),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp, 32.dp)
                        .padding(top = 4.dp)
                        .alpha(logoAlpha.value),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 进入新版本按钮
            Button(
                onClick = onEnterNewVersion,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "进入旧版本",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}