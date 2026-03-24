package org.autojs.autojs.ui.modern.activity.settings.model

import android.content.Context
import androidx.preference.PreferenceManager
import org.autojs.autojs6.R

enum class KeepScreenOnMode(
    @androidx.annotation.StringRes val titleRes: Int,
    @androidx.annotation.StringRes val keyRes: Int
) {
    DISABLED(
        R.string.entry_keep_screen_on_when_in_foreground_disabled,
        R.string.key_keep_screen_on_when_in_foreground_disabled
    ),
    ALL_PAGES(
        R.string.entry_keep_screen_on_when_in_foreground_all_pages,
        R.string.key_keep_screen_on_when_in_foreground_all_pages
    ),
    HOMEPAGE_ONLY(
        R.string.entry_keep_screen_on_when_in_foreground_homepage_only,
        R.string.key_keep_screen_on_when_in_foreground_homepage_only
    );
    
    val isEnabled: Boolean
        get() = this != DISABLED
    
    fun getKey(context: Context): String = context.getString(keyRes)
    
    companion object {
        fun fromKey(context: Context, key: String?): KeepScreenOnMode? =
            values().find { it.getKey(context) == key }
        
        fun getPrefMode(context: Context): KeepScreenOnMode {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val storedKey = prefs.getString(
                context.getString(R.string.key_keep_screen_on_when_in_foreground),
                context.getString(R.string.key_keep_screen_on_when_in_foreground_disabled)
            )
            return fromKey(context, storedKey) ?: DISABLED
        }
    }
}