package com.expedia.account.util

import android.content.Context

class AndroidStringSource(private val context: Context) : StringSource {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }

    override fun getBrandedString(resId: Int, brand: String): String {
        return Utils.obtainBrandedPhrase(context, resId, brand).format().toString()
    }
}
