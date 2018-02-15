package com.expedia.bookings.hotel.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.shouldShowCircleForRatings
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.StarRatingBar
import com.expedia.bookings.widget.TextView
import com.expedia.vm.HotelInfoToolbarViewModel
import io.reactivex.subjects.PublishSubject

class HotelGalleryToolbar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val navClickedSubject = PublishSubject.create<Unit>()
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val toolBarBackground: View by bindView(R.id.toolbar_background)
    private val toolbarTitle: TextView by bindView(R.id.hotel_name_text)

    private var navIcon: ArrowXDrawable
    private lateinit var toolBarRating: StarRatingBar
    private lateinit var viewmodel: HotelInfoToolbarViewModel

    init {
        View.inflate(getContext(), R.layout.hotel_details_toolbar, this)

        if (!isInEditMode) {
            if (shouldShowCircleForRatings()) {
                toolBarRating = findViewById<StarRatingBar>(R.id.hotel_circle_rating_bar)
            } else {
                toolBarRating = findViewById<StarRatingBar>(R.id.hotel_star_rating_bar)
            }
            toolBarRating.visibility = View.VISIBLE
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        toolbar.navigationIcon = navIcon
        toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_gallery_cont_desc)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance)

        toolBarBackground.alpha = 0f

        toolbar.setNavigationOnClickListener {
            navClickedSubject.onNext(Unit)
        }
    }

    fun setViewModel(vm: HotelInfoToolbarViewModel) {
        viewmodel = vm

        vm.hotelNameObservable.subscribeText(toolbarTitle)
        vm.hotelRatingObservable.subscribe {
            toolBarRating.setRating(it)
        }
        vm.hotelRatingContentDescriptionObservable.subscribeContentDescription(toolBarRating)
        vm.hotelRatingObservableVisibility.subscribeVisibility(toolBarRating)
    }
}
