package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.vm.ItinWebviewInfoButtonViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable


class ItinWebviewInfoWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val buttonText: TextView by bindView(R.id.itin_webview_button_text)

    var viewModel: ItinWebviewInfoButtonViewModel by notNullAndObservable { vm ->
        vm.createWebviewButtonWidgetSubject.subscribe { (text, drawable, color, url) ->
            if (!url.isNullOrEmpty()) {
                if (!text.isNullOrEmpty())
                    buttonText.text = text
                if (drawable != null) {
                    buttonText.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
                }
                if (color != null) {
                    buttonText.setTextColor(ContextCompat.getColor(context, color))
                }
                if (url != null) {
                    buttonText.setOnClickListener {
                        context.startActivity(buildWebViewIntent(text, url).intent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0).toBundle())
                        OmnitureTracking.trackItinFlightBaggageInfoClicked()
                    }
                }
            } else {
                this.visibility = View.GONE
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_itin_webview_button, this)
    }

    private fun buildWebViewIntent(title: String?, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        return builder
    }

}