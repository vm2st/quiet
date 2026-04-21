package com.vm2st.quiet.utils

import android.content.Context
import java.util.Locale

object LocaleHelper {
    fun getLocale(context: Context): Locale {
        val config = context.resources.configuration
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
    }

    fun isRussian(context: Context): Boolean {
        return getLocale(context).language == "ru"
    }
}