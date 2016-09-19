package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class RailInboundPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val container: ViewGroup by bindView(R.id.container)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        View.inflate(context, R.layout.widget_rail_inbound, this)
        val statusBarHeight = Ui.getStatusBarHeight(context)

        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, container, color)
            addView(statusBar)
        }
    }

}
