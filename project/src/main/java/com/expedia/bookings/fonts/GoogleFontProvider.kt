package com.expedia.bookings.fonts

import android.content.Context
import android.os.Handler
import android.support.v4.provider.FontRequest
import android.support.v4.provider.FontsContractCompat
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import android.os.HandlerThread

class GoogleFontProvider : FontProvider {

    private var mHandler: Handler? = null
    private val FONT_PROVIDER_AUTHORITY = "com.google.android.gms.fonts"
    private val FONT_PROVIDER_PACKAGE = "com.google.android.gms"

    override fun downloadFont(context: Context, font: FontCache.Font) {

        val request = FontRequest(FONT_PROVIDER_AUTHORITY, FONT_PROVIDER_PACKAGE,
                font.getQuery(), R.array.com_google_android_gms_fonts_certs)

        FontsContractCompat.requestFont(context, request, font.requestCallback, getFontHandler())
    }

    private fun getFontHandler(): Handler {
        if (mHandler == null) {
            val handlerThread = HandlerThread("fonts")
            handlerThread.start()
            mHandler = Handler(handlerThread.looper)
        }
        return mHandler as Handler
    }
}
