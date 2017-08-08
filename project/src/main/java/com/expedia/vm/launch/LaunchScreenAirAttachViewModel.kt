package com.expedia.vm.launch

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class LaunchScreenAirAttachViewModel(val context: Context, val itemView: View, recentUpcomingFlightTrip: Trip, hotelSearchParams: HotelSearchParams, cityName: String) {

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

            val animOptions = AnimUtils.createActivityScaleBundle(itemView)
            if (hotelSearchParams == null) {
                HotelNavUtils.goToHotels(context, animOptions)
                OmnitureTracking.trackPhoneAirAttachLaunchScreenClick()

            } else {
                HotelNavUtils.goToHotels(context, hotelSearchParams)
                OmnitureTracking.trackPhoneAirAttachLaunchScreenClick()
            }
        }

        val isVariant1 = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen) == AbacusUtils.LaunchScreenAirAttachVariant.UP_TO_XX_PERCENT_OFF.ordinal

        if (isVariant1) {
            firstLineObserver.onNext(Phrase.from(context, R.string.air_attach_variant2_string1_TEMPLATE).put("location", cityName).format().toString())
            secondLineObserver.onNext(context.getString(R.string.air_attach_variant2_string2))
        } else {
            firstLineObserver.onNext(context.getString(R.string.air_attach_variant1_string))
            secondLineObserver.onNext(Phrase.from(context, R.string.air_attach_variant1_string1_TEMPLATE).put("location", cityName).format().toString())
        }


        var accessibilityAirAttachContentDesc = StringBuilder()
        if ((daysRemaining <= 0) && (hoursRemaining < 24)) {
            if (hoursRemaining < 1) {
                offerExpiresObserver.onNext(context.resources.getText(R.string.air_attach_expires_soon).toString())
            } else {
                var offerExpiryInHours = Phrase.from(context.resources.getQuantityString(R.plurals.hours_from_now, hoursRemaining))
                        .put("hours", hoursRemaining).format().toString()
                offerExpiresObserver.onNext(Phrase.from(context.resources.getString(R.string.air_attach_offer_expires_TEMPLATE))
                        .put("daysorhours", offerExpiryInHours).format().toString())
            }
        } else {
            var offerExpiryInDays = Phrase.from(context.resources.getQuantityString(R.plurals.days_from_now, daysRemaining))
                    .put("days", daysRemaining).format().toString()
            offerExpiresObserver.onNext(Phrase.from(context.resources.getString(R.string.air_attach_offer_expires_TEMPLATE))
                    .put("daysorhours", offerExpiryInDays).format().toString())
        }

        accessibilityAirAttachContentDesc.append(offerExpiresObserver.value).append(" ").append(firstLineObserver.value).append(" ").append(secondLineObserver.value).append(" ")

        AccessibilityUtil.appendRoleContDesc(itemView, accessibilityAirAttachContentDesc.toString(), R.string.accessibility_cont_desc_role_button).toString()
    }

}
