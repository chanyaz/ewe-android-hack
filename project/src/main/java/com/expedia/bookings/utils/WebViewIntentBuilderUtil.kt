package com.expedia.bookings.utils

import com.expedia.ui.LOBWebViewActivity

object WebViewIntentBuilderUtil {

    @JvmStatic
    fun setDefaultWebViewIntentProperties(intentBuilder: LOBWebViewActivity.IntentBuilder): LOBWebViewActivity.IntentBuilder {
        intentBuilder.setInjectExpediaCookies(true)
        intentBuilder.setAllowMobileRedirects(true)
        intentBuilder.setLoginEnabled(true)
        intentBuilder.setHandleBack(true)
        intentBuilder.setRetryOnFailure(true)
        return intentBuilder
    }
}
