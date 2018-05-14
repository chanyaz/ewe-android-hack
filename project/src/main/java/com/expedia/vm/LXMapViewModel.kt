package com.expedia.vm

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import javax.inject.Inject

class LXMapViewModel(val context: Context) {
    //Outputs for View
    val toolbarDetailText = PublishSubject.create<String>()
    val toolbarSubtitleText = PublishSubject.create<String>()
    val activityPrice = PublishSubject.create<CharSequence>()
    val eventLatLng = PublishSubject.create<LatLong>()
    val redemptionLocationsLatLng = PublishSubject.create<List<LatLong>>()

    lateinit var lxState: LXState
        @Inject set

    init {
        Ui.getApplication(context).lxComponent().inject(this)
    }
    //Setup the data I need to behave as a View Model for my View
    val offersObserver = endlessObserver<ActivityDetailsResponse> { response ->
        activityPrice.onNext(fromPriceStyledString(context, response))
        eventLatLng.onNext(ActivityDetailsResponse.LXLocation.getLocation(response.eventLocation.latLng))
        redemptionLocationsLatLng.onNext(getRedemptionLocationCoordinates(response.redemptionLocation))
        toolbarDetailText.onNext(lxState.activity.title ?: "")
        toolbarSubtitleText.onNext(LXDataUtils.getToolbarSearchDateText(context, lxState.searchParams, false))
    }

    companion object {

        fun getRedemptionLocationCoordinates(redemptionLocations: List<ActivityDetailsResponse.LXLocation>): List<LatLong> {
            val redemptionLocationCoordinates = ArrayList<LatLong>()
            redemptionLocations.forEach { it ->
                redemptionLocationCoordinates.add(ActivityDetailsResponse.LXLocation.getLocation(it.latLng))
            }
            return redemptionLocationCoordinates
        }

        fun fromPriceStyledString(context: Context, response: ActivityDetailsResponse): CharSequence {
            if (response.fromPrice == null) {
                return ""
            }
            val price = response.fromPrice
            val fromPriceString = context.getString(R.string.from_price_TEMPLATE, price)
            val fromPriceStyledString = SpannableString(fromPriceString)
            val startIndex = fromPriceString.indexOf(price)
            val endIndex = startIndex + price.length
            fromPriceStyledString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            fromPriceStyledString.setSpan(RelativeSizeSpan(1.4f), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return fromPriceStyledString
        }
    }
}
