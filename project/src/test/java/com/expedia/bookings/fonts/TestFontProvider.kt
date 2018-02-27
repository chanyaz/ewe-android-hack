package com.expedia.bookings.fonts

import android.content.Context
import android.graphics.Typeface
import com.expedia.bookings.utils.FontCache
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class TestFontProvider : FontProvider {

    override fun downloadFont(context: Context, font: FontCache.Font) {
        if (font == FontCache.Font.ROBOTO_BOLD) {
            font.failureLogger.setEndTime()
            font.requestCallback.onTypefaceRequestFailed(2)
        } else {
            val typeface = Mockito.mock(Typeface::class.java)
            `when`(typeface.style).thenReturn(1011)
            font.successLogger.setEndTime()
            font.requestCallback.onTypefaceRetrieved(typeface)
        }
    }
}
