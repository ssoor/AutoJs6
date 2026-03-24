package org.autojs.autojs.ui.modern.activity.settings.model

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager
import org.autojs.autojs.theme.ThemeColor as OldThemeColor
import org.autojs.autojs.theme.ThemeColorManager

data class ThemeColor(
    val primary: Int = Color.parseColor("#00695C"),
    val primaryDark: Int = Color.parseColor("#004D40"),
    val accent: Int = Color.parseColor("#FFC107")
) {
    fun toOldThemeColor(): OldThemeColor = OldThemeColor(
        colorPrimary = primary,
        colorPrimaryDark = primaryDark,
        colorAccent = accent
    )
    
    companion object {
        fun fromOldThemeColor(old: OldThemeColor): ThemeColor = ThemeColor(
            primary = old.colorPrimary,
            primaryDark = old.colorPrimaryDark,
            accent = old.colorAccent
        )
        
        fun getPrefThemeColor(context: Context): ThemeColor? {
            val old = OldThemeColor.fromPreferences()
            return old?.let { fromOldThemeColor(it) }
        }
        
        fun getThemeColorSummary(context: Context): String {
            return org.autojs.autojs.theme.app.ColorSelectBaseActivity.getCurrentColorSummary(context)
        }
    }
}