package com.expedia.bookings.activity

import android.content.Context

class DeepLinkWebViewActivity: WebViewActivity() {

    class IntentBuilder(context: Context) : WebViewActivity.IntentBuilder(context) {
        init {
            intent.setClass(context, DeepLinkWebViewActivity::class.java)
        }
    }
}