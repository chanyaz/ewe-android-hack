package com.expedia.util

import android.content.Context
import android.text.TextUtils
import com.expedia.bookings.R
import com.mobiata.android.util.SettingUtils
import java.util.Locale

object LanguageHelper {

    @JvmStatic
    fun initLangSetup(context: Context) {
        if (SettingUtils.contains(context, context.getString(R.string.preference_which_lang_to_use_key))) {
            val lang = SettingUtils
                    .get(context, context.getString(R.string.preference_which_lang_to_use_key), "")
            if (lang.contains("_")) {
                val arr = lang.split("_")
                load(context, arr[0], arr[1])
            } else {
                load(context, lang)
            }
        }
    }

    @JvmStatic
    fun revertToDefault(context: Context) {
        SettingUtils.remove(context, context.getString(R.string.preference_which_lang_to_use_key))
    }

    @JvmStatic
    fun setAppLocale(context: Context, lang: String, region: String? = "") {
        if (TextUtils.isEmpty(lang)) return
        val newLang = if (TextUtils.isEmpty(region)) lang else lang + "_" + region
        SettingUtils.save(context, context.getString(R.string.preference_which_lang_to_use_key), newLang)
        load(context, lang, region)
    }

    private fun load(context: Context, lang: String, region: String? = "") {
        val resources = context.resources
        val config = resources.configuration

        if (!TextUtils.isEmpty(region)) {
            Locale.setDefault(Locale(lang, region))
        } else {
            Locale.setDefault(Locale(lang))
        }
        config.setLocale(Locale.getDefault())
        @Suppress("DEPRECATION")
        // createConfigurationContext() is not working as expected. Need to check
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
