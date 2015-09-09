package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import kotlin.properties.Delegates

public class SpecialNoticeWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val description: TextView by bindView(R.id.content_description)
    val container: ViewGroup by bindView(R.id.container)
    val statusBarHeight by Delegates.lazy { Ui.getStatusBarHeight(context) }
    val toolBarHeight by Delegates.lazy { Ui.getToolbarSize(context) }

    init {
        View.inflate(getContext(), R.layout.widget_special_notice, this)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        toolbar.setNavigationOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        container.setPadding(0, toolBarHeight + statusBarHeight, 0, 0)
    }

    fun setText(text: Pair<String, String>) {
        toolbar.setTitle(text.first)
        description.setText(Html.fromHtml(StrUtils.getFormattedContent(getContext(), text.second)))
    }
}