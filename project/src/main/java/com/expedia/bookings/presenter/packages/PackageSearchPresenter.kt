package com.expedia.bookings.presenter.packages

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

public class PackageSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        View.inflate(context, R.layout.widget_package_search_params, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

    }
}
