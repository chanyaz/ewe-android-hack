package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.LayoutUtils
import com.expedia.bookings.widget.packages.HotelCrossSellViewModel
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.larvalabs.svgandroid.widget.SVGView


class HotelCrossSellView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    val airAttachContainer: LinearLayout by bindView(R.id.hotel_cross_sell_body)
    val airattachExpirationDaysRemainingTextView: TextView by bindView(R.id.itin_air_attach_expiration_date_text_view)
    val airAttachExpirationTodayTextView: TextView by bindView(R.id.air_attach_expires_today_text_view)
    val airAttachCountDownView: LinearLayout by bindView(R.id.air_attach_countdown_view)
    val isNewConfirmationScreenEnabled = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_additional_content_flight_confirmation)
    val airAttachSVG: SVGView by bindView(R.id.air_attach_curve)

    var viewModel: HotelCrossSellViewModel by notNullAndObservable { vm ->
        vm.expiresTodayVisibility.subscribeVisibility(airAttachExpirationTodayTextView)
        vm.daysRemainingVisibility.subscribeVisibility(airAttachCountDownView)
        vm.daysRemainingText.subscribeText(airattachExpirationDaysRemainingTextView)
    }

    init {
        View.inflate(context, if (isNewConfirmationScreenEnabled) R.layout.widget_crystal_hotel_cross_sell else R.layout.widget_hotel_cross_sell, this)
        LayoutUtils.setSVG(airAttachSVG, R.raw.itin_orange_air_attach_curve)
        viewModel = HotelCrossSellViewModel(context)
        airAttachContainer.setOnClickListener {
            FlightsV2Tracking.trackAirAttachClicked()
            val flightLegs = viewModel.confirmationObservable.value.getFirstFlightTripDetails().getLegs()
            val sp = HotelsV2DataUtil.getHotelV2ParamsFromFlightV2Params(flightLegs, viewModel.searchParamsObservable.value)
            NavUtils.goToHotelsV2(context, sp, null, NavUtils.FLAG_DEEPLINK)
            val activity = context as AppCompatActivity
            activity.finish()
        }
    }
}
