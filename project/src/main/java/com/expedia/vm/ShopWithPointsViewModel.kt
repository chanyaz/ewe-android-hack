package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.UserAccountRefresher
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.text.NumberFormat
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import rx.Observable

class ShopWithPointsViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    val isShopWithPointsAvailableObservable = UserAccountRefresher.userAccountRefreshed
            .startWith(User.isLoggedIn(context))
            .map { isShopWithPointsAvailable(it) }

    val shopWithPointsToggleObservable = BehaviorSubject.create<Boolean>(true)

    val swpHeaderStringObservable = shopWithPointsToggleObservable.map { isToggleOn ->
        context.getString(if (isToggleOn) R.string.swp_on_widget_header else R.string.swp_off_widget_header)
    }

    val swpEffectiveAvailability = BehaviorSubject.create<Boolean>()

    val numberOfPointsObservable = isShopWithPointsAvailableObservable.map {
        Db.getUser()?.loyaltyMembershipInformation?.loyaltyPointsAvailable ?: 0.0
    }

    val pointsDetailStringObservable = numberOfPointsObservable.map {
        Phrase.from(context.resources, R.string.swp_widget_points_value_TEMPLATE).put("points", NumberFormat.getInstance().format(it.toInt())).format().toString()
    }

    private fun isShopWithPointsAvailable(isUserLoggedIn: Boolean): Boolean = isUserLoggedIn && PointOfSale.getPointOfSale().isSWPEnabledForHotels
            && Db.getUser().loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false

    init {
        shopWithPointsToggleObservable.subscribe(paymentModel.swpOpted)

        Observable.combineLatest(shopWithPointsToggleObservable, isShopWithPointsAvailableObservable, { swpToggleState, isSWPAvailable ->
            isSWPAvailable && swpToggleState
        }).subscribe(swpEffectiveAvailability)

        shopWithPointsToggleObservable.skip(1).subscribe {
            HotelV2Tracking().trackSwPToggle(it)
        }
    }
}
