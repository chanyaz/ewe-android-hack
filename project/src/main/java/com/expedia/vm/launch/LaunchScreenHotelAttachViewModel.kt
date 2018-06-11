package com.expedia.vm.launch

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.DeprecatedHotelSearchParams
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LaunchScreenHotelAttachViewModel(val context: Context, val itemView: View, recentUpcomingFlightTrip: Trip, hotelSearchParams: DeprecatedHotelSearchParams, cityName: String) {

    //Inputs
    var onClickObserver = PublishSubject.create<Unit>()

    //Outputs
    var firstLineObserver = BehaviorSubject.create<String>()
    var secondLineObserver = BehaviorSubject.create<String>()
    var offerExpiresObserver = BehaviorSubject.create<String>()

    private var hoursRemaining: Int
    private var daysRemaining: Int

    init {
        hoursRemaining = recentUpcomingFlightTrip.airAttach.hoursRemaining
        daysRemaining = recentUpcomingFlightTrip.airAttach.daysRemaining

        onClickObserver.subscribe {
            HotelNavUtils.goToHotels(context, hotelSearchParams)
            OmnitureTracking.trackPhoneAirAttachLaunchScreenClick()
        }

        firstLineObserver.onNext(context.getString(R.string.hotel_attach_string))
        secondLineObserver.onNext(Phrase.from(context, R.string.hotel_attach_string1_TEMPLATE).put("location", cityName).format().toString())

        if (hoursRemaining < 1) {
            offerExpiresObserver.onNext(context.resources.getText(R.string.air_attach_expires_soon).toString())
        } else if (hoursRemaining < 24) {
            val offerExpiryInHours = Phrase.from(context.resources.getQuantityString(R.plurals.hours_from_now, hoursRemaining))
                    .put("hours", hoursRemaining).format().toString()
            offerExpiresObserver.onNext(Phrase.from(context.resources.getString(R.string.air_attach_offer_expires_TEMPLATE))
                    .put("daysorhours", offerExpiryInHours).format().toString())
        } else {
            val offerExpiryInDays = Phrase.from(context.resources.getQuantityString(R.plurals.days_from_now, daysRemaining))
                    .put("days", daysRemaining).format().toString()
            offerExpiresObserver.onNext(Phrase.from(context.resources.getString(R.string.air_attach_offer_expires_TEMPLATE))
                    .put("daysorhours", offerExpiryInDays).format().toString())
        }

        itemView.contentDescription =
                Phrase.from(itemView, R.string.air_attach_card_cont_desc_TEMPLATE)
                        .put("expiry", offerExpiresObserver.value)
                        .put("title", firstLineObserver.value)
                        .put("content", secondLineObserver.value)
                        .format()
    }
}
