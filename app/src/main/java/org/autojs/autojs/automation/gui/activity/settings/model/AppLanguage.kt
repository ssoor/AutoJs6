package org.autojs.autojs.ui.modern.activity.settings.model

import androidx.annotation.StringRes
import android.content.Context
import androidx.preference.PreferenceManager
import org.autojs.autojs6.R
import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
    @StringRes val titleRes: Int,
    val locale: Locale?
) {
    AUTO("", R.string.entry_app_language_auto, null),
    ZH_HANS("zh-Hans", R.string.entry_app_language_zh_hans, Locale.SIMPLIFIED_CHINESE),
    ZH_HANT_HK("zh-Hant-HK", R.string.entry_app_language_zh_hant_hk, Locale.TRADITIONAL_CHINESE),
    ZH_HANT_TW("zh-Hant-TW", R.string.entry_app_language_zh_hant_tw, Locale.TAIWAN),
    EN("en", R.string.entry_app_language_en, Locale.ENGLISH),
    FR("fr", R.string.entry_app_language_fr, Locale.FRENCH),
    ES("es", R.string.entry_app_language_es, java.util.Locale("es")),
    JA("ja", R.string.entry_app_language_ja, Locale.JAPANESE),
    KO("ko", R.string.entry_app_language_ko, Locale.KOREAN),
    RU("ru", R.string.entry_app_language_ru, java.util.Locale("ru")),
    AR("ar", R.string.entry_app_language_ar, java.util.Locale("ar"));
    
    val isAuto: Boolean
        get() = this == AUTO
    
    companion object {
        fun fromLanguageTag(tag: String?): AppLanguage? = 
            values().find { it.languageTag == tag }
        
        fun getPrefLanguage(context: Context): AppLanguage {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val tag = prefs.getString(context.getString(R.string.key_app_language), null)
            return fromLanguageTag(tag) ?: AUTO
        }
    }
}