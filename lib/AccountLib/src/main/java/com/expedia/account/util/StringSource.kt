package com.expedia.account.util

import android.support.annotation.StringRes

interface StringSource {
    fun getString(@StringRes resId: Int): String
    fun getBrandedString(@StringRes resId: Int, brand: String): String
}
