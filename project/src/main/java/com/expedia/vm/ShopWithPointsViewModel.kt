package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.model.UserLoginStateChangedModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Subscription
import rx.subjects.BehaviorSubject
import java.text.NumberFormat

class ShopWithPointsViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>, userLoginChangedModel: UserLoginStateChangedModel) {

    lateinit var subscription: Subscription
    val isShopWithPointsAvailableObservable = BehaviorSubject.create<Boolean>()
    val isShopWithPointsAvailableObservableIntermediateStream = Observable.concat(Observable.just(User.isLoggedIn(context)),userLoginChangedModel.userLoginStateChanged)
            .map { isShopWithPointsAvailable(it) }

    val shopWithPointsToggleObservable = BehaviorSubject.create<Boolean>(true)

    val swpHeaderStringObservable = shopWithPointsToggleObservable.map { isToggleOn ->
        context.getString(if (isToggleOn) R.string.swp_on_widget_header else R.string.swp_off_widget_header)
    }

    val swpEffectiveAvailability = BehaviorSubject.create<Boolean>()

    val pointsDetailStringObservable = isShopWithPointsAvailableObservable.map {
        var value: String?;
        if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType) {
            val pointsAvailable = Db.getUser()?.loyaltyMembershipInformation?.loyaltyPointsAvailable?.toInt() ?: null
            value = if (pointsAvailable != null) NumberFormat.getInstance().format(pointsAvailable) else null
        } else {
            value = Db.getUser()?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.formattedMoneyFromAmountAndCurrencyCode ?: null
        }
        if (value != null) Phrase.from(context.resources, R.string.swp_widget_points_value_TEMPLATE).put("points_or_amount", value).format().toString() else ""
    }

    private fun isShopWithPointsAvailable(isUserLoggedIn: Boolean): Boolean = isUserLoggedIn && PointOfSale.getPointOfSale().isSWPEnabledForHotels
            && Db.getUser()?.loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false

    init {
        shopWithPointsToggleObservable.subscribe(paymentModel.swpOpted)

        Observable.combineLatest(shopWithPointsToggleObservable, isShopWithPointsAvailableObservable, { swpToggleState, isSWPAvailable ->
            println("malcolm: " +  isSWPAvailable + ","  + swpToggleState)
            isSWPAvailable && swpToggleState
        }).subscribe(swpEffectiveAvailability)

        shopWithPointsToggleObservable.skip(1).subscribe {
            HotelTracking().trackSwPToggle(it)
        }
        subscription = isShopWithPointsAvailableObservableIntermediateStream.subscribe(isShopWithPointsAvailableObservable)
    }
}
