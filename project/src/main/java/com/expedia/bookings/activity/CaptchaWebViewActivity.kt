package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import com.expedia.bookings.utils.Constants

class CaptchaWebViewActivity : WebViewActivity() {

    class IntentBuilder(context: Context, originalUrl: String?, htmlString: String?, baseUrl: String?) : WebViewActivity.IntentBuilder(context) {
        init {
            intent.setClass(context, CaptchaWebViewActivity::class.java)
            intent.putExtra(Constants.ARG_HTML_DATA, htmlString)
            intent.putExtra(Constants.ARG_ORIGINAL_URL, originalUrl)
            intent.putExtra(Constants.ARG_USE_WEB_VIEW_TITLE, true)
            intent.putExtra(Constants.ARG_BASE_URL, baseUrl)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun newUrlLoaded(url: String?) {
        if (url == intent.getStringExtra(Constants.ARG_ORIGINAL_URL)) {
            finish()
        }
    }
}
