package com.expedia.bookings.widget;

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.HotelReviewsSummaryViewModel

public class HotelReviewsSummaryWidget(context: Context) : LinearLayout(context) {

    val overallRating: TextView  by bindView(R.id.overall_rating)
    val roomCleanliness: TextView  by bindView(R.id.room_cleanliness)
    val roomComfort: TextView  by bindView(R.id.room_comfort)
    val serviceStaff: TextView  by bindView(R.id.service_staff)
    val hotelCondition: TextView  by bindView(R.id.hotel_condition)

    init {
        View.inflate(context, R.layout.hotel_reviews_summary_widget, this)
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        var paddingBottom = resources.getDimensionPixelOffset(R.dimen.hotel_reviews_summary_padding_bottom)
        var paddingLeft = resources.getDimensionPixelOffset(R.dimen.hotel_reviews_padding_left)
        var paddingRight = resources.getDimensionPixelOffset(R.dimen.hotel_reviews_padding_right)
        var paddingTop = resources.getDimensionPixelOffset(R.dimen.hotel_review_row_padding_top)
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        setBackgroundColor(ContextCompat.getColor(context, R.color.hotel_reviews_summary_background_color))
    }

    fun bindData(vm: HotelReviewsSummaryViewModel){
        vm.overallRatingObservable.subscribeText(overallRating)
        vm.roomCleanlinessObservable.subscribeText(roomCleanliness)
        vm.roomComfortObservable.subscribeText(roomComfort)
        vm.serviceStaffObservable.subscribeText(serviceStaff)
        vm.hotelConditionObservable.subscribeText(hotelCondition)
    }
}
