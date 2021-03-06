package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeRating
import com.expedia.util.subscribeStarColor
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelDetailViewModel
import kotlin.properties.Delegates

public class HotelDetailsToolbar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    var toolBarRating: StarRatingBar by Delegates.notNull()
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val toolBarBackground: View by bindView(R.id.toolbar_background)
    val toolBarGradient: View by bindView(R.id.hotel_details_gradient)
    var viewModel: HotelDetailViewModel by Delegates.notNull()
    var navIcon: ArrowXDrawable by Delegates.notNull()

    init {
        View.inflate(getContext(), R.layout.hotel_details_toolbar, this)

        if (shouldShowCircleForRatings()) {
            toolBarRating = findViewById(R.id.hotel_circle_rating_bar) as StarRatingBar
        } else {
            toolBarRating = findViewById(R.id.hotel_star_rating_bar) as StarRatingBar
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon

        toolbar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance)
        toolBarRating.visibility = View.VISIBLE

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarBackground.layoutParams.height += statusBarHeight
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    fun hideGradient() {
        toolBarGradient.visibility = View.GONE
    }

    fun setHotelDetailViewModel(vm: HotelDetailViewModel) {
        viewModel = vm
        vm.toolBarRatingColor.subscribeStarColor(toolBarRating)
        vm.hotelNameObservable.subscribeText(toolbarTitle)
        vm.hotelRatingObservable.subscribeRating(toolBarRating)
        vm.hotelRatingObservableVisibility.subscribeVisibility(toolBarRating)
    }
}
