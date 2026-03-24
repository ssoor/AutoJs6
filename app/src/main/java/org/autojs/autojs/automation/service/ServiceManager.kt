package org.autojs.autojs.automation.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import org.autojs.autojs.automation.service.foreground.ScriptForegroundService
import org.autojs.autojs.util.ForegroundServiceUtils
import org.autojs.autojs6.R

class ServiceManager {
    private val LOG_TAG = ServiceManager::class.java.getSimpleName()
    private val foregroundClassName = ScriptForegroundService::class.java

    private val serviceKeyMap: Map<Class<*>, Int> =
            mapOf(
                    ScriptForegroundService::class.java to R.string.key_foreground_service,
            )

    fun setForegroundServiceEnabled(context: Context, enabled: Boolean): Boolean {
        return setEnable(context, foregroundClassName, enabled)
    }

    // 统一切换服务状态接口
    private fun <T> setEnable(context: Context, className: Class<T>, enabled: Boolean): Boolean {
        Log.d(LOG_TAG, "set service enabled: $className, $enabled")

        // 处理前台服务
        val success =
                if (enabled) {
                    start(context, className)
                } else {
                    stop(context, className)
                }

        Log.d(LOG_TAG, "set service enabled: $className, $enabled, success = $success")

        if (success) {
            var serviceKey = serviceKeyMap[className]
            if (serviceKey != null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.edit().putBoolean(context.getString(serviceKey), enabled).apply()
            }
        }

        return success
    }

    // 统一启动接口
    private fun <T> start(context: Context, className: Class<T>): Boolean {
        var success = false
        try {
            success =
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            context.startForegroundService(Intent(context, className)) != null
                        }
                        else -> context.startService(Intent(context, className)) != null
                    }
        } catch (e: Exception) {
            Log.d(LOG_TAG, "start service failed: $className", e)
            throw e
        }

        return success || isRunning(context, className)
    }

    // 统一停止接口
    private fun <T> stop(context: Context, className: Class<T>): Boolean {
        var success = false
        try {
            success = context.stopService(Intent(context, className))
        } catch (e: Exception) {
            Log.d(LOG_TAG, "stop service failed: $className", e)
            throw e
        }

        return success || isRunning(context, className) == false
    }

    // 检查是否正在运行
    private fun <T> isRunning(context: Context, className: Class<T>): Boolean =
            ForegroundServiceUtils.isRunning(context, className)
}
