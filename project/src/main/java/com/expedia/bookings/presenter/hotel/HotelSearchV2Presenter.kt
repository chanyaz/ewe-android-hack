package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

public class HotelSearchV2Presenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val searchContainer: ViewGroup by bindView(R.id.search_v2_container)
    var navIcon: ArrowXDrawable

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_v2_params, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationOnClickListener {
            com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchV2Presenter)
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.cars_search_menu)
    }


    override fun back(): Boolean {
        return super.back()
    }
}
