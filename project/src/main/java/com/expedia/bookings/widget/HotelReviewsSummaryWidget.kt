package com.expedia.bookings.widget;

import android.app.ActionBar
import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.HotelReviewRowViewModel
import com.expedia.vm.HotelReviewsSummaryViewModel

public class HotelReviewsSummaryWidget(context: Context, vm: HotelReviewsSummaryViewModel) : LinearLayout(context) {

    val overallRating: TextView  by bindView(R.id.overall_rating)
    val roomCleanliness: TextView  by bindView(R.id.room_cleanliness)
    val roomComfort: TextView  by bindView(R.id.room_comfort)
    val serviceStaff: TextView  by bindView(R.id.service_staff)
    val hotelCondition: TextView  by bindView(R.id.hotel_condition)

    init {
        View.inflate(context, R.layout.hotel_reviews_summary_widget, this)
        setOrientation(LinearLayout.VERTICAL)
        setLayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        vm.overallRatingObservable.subscribeText(overallRating)
        vm.roomCleanlinessObservable.subscribeText(roomCleanliness)
        vm.roomComfortObservable.subscribeText(roomComfort)
        vm.serviceStaffObservable.subscribeText(serviceStaff)
        vm.hotelConditionObservable.subscribeText(hotelCondition)
    }
}
