package com.expedia.bookings.fonts

import android.content.Context
import com.expedia.bookings.utils.FontCache

interface FontProvider {
    fun downloadFont(context: Context, font: FontCache.Font)
}
