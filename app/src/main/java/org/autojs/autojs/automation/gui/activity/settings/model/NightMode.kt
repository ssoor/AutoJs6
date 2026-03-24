package org.autojs.autojs.ui.modern.activity.settings.model

import androidx.annotation.StringRes
import android.content.Context
import androidx.preference.PreferenceManager
import org.autojs.autojs6.R
import androidx.appcompat.app.AppCompatDelegate

enum class NightMode(
    @StringRes val titleRes: Int,
    val appCompatMode: Int
) {
    FOLLOW_SYSTEM(R.string.entry_night_mode_follow_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    ALWAYS_ON(R.string.entry_night_mode_always_on, AppCompatDelegate.MODE_NIGHT_YES),
    ALWAYS_OFF(R.string.entry_night_mode_always_off, AppCompatDelegate.MODE_NIGHT_NO);
    
    companion object {
        fun fromAppCompatMode(mode: Int): NightMode? = 
            values().find { it.appCompatMode == mode }
        
        fun getPrefNightMode(context: Context): NightMode {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val isAutoNight = prefs.getBoolean(
                context.getString(R.string.key_auto_night_mode_enabled), 
                false
            )
            
            if (isAutoNight) {
                return FOLLOW_SYSTEM
            }
            
            val isNightEnabled = prefs.getBoolean(
                context.getString(R.string.key_night_mode_enabled), 
                false
            )
            
            return if (isNightEnabled) ALWAYS_ON else ALWAYS_OFF
        }
    }
}