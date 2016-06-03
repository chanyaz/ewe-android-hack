package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.NavigationHelper
import com.expedia.bookings.utils.bindView

class NewLaunchLobWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val viewModel: NewLaunchLobViewModel by lazy {
        NewLaunchLobViewModel(context, NavigationHelper(context))
    }

    private val backGroundView: View by bindView(R.id.background)
    private val cardView: CardView by bindView(R.id.card_view)

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewModel.bind(this, PointOfSale.getPointOfSale())
        adjustBackgroundView()
    }

    fun onPOSChange() {
        viewModel.bind(this, PointOfSale.getPointOfSale())
        adjustBackgroundView()
    }

    private fun adjustBackgroundView() {
        cardView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                val layoutParams = backGroundView.layoutParams
                val topMargin = resources.getDimensionPixelSize(R.dimen.new_launch_lob_top_margin)
                layoutParams.height = (cardView.height / 2) + topMargin
                backGroundView.requestLayout()
                return false
            }
        })
    }
}
