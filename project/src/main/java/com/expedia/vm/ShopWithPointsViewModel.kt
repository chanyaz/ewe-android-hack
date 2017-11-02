package com.expedia.vm

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.util.LoyaltyUtil
import com.squareup.phrase.Phrase
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.text.NumberFormat

class ShopWithPointsViewModel(val context: Context, val paymentModel: PaymentModel<HotelCreateTripResponse>, userLoginChangedModel: UserLoginStateChangedModel) {

    private var userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    var subscription: Disposable
    val isShopWithPointsAvailableObservable = BehaviorSubject.create<Boolean>()
    val isShopWithPointsAvailableObservableIntermediateStream =
            Observable.concat(
                    Observable.just(userStateManager.isUserAuthenticated()),
                    userLoginChangedModel.userLoginStateChanged).map { LoyaltyUtil.isShopWithPointsAvailable(userStateManager) }

    val shopWithPointsToggleObservable = BehaviorSubject.createDefault<Boolean>(true)

    val swpHeaderStringObservable = shopWithPointsToggleObservable.map { isToggleOn ->
        context.getString(if (isToggleOn) R.string.swp_on_widget_header else R.string.swp_off_widget_header)
    }

    val swpEffectiveAvailability = BehaviorSubject.create<Boolean>()

    val pointsDetailStringObservable = isShopWithPointsAvailableObservable.map {
        val value: String?
        val user = userStateManager.userSource.user

        if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType) {
            val pointsAvailable = user?.loyaltyMembershipInformation?.loyaltyPointsAvailable?.toInt()
            value = if (pointsAvailable != null) NumberFormat.getInstance().format(pointsAvailable) else null
        } else {
            value = user?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.formattedMoneyFromAmountAndCurrencyCode
        }
        if (value != null) Phrase.from(context.resources, R.string.swp_widget_points_value_TEMPLATE).put("points_or_amount", value).format().toString() else ""
    }

    init {
        shopWithPointsToggleObservable.subscribe(paymentModel.swpOpted)

        ObservableOld.combineLatest(shopWithPointsToggleObservable, isShopWithPointsAvailableObservable, { swpToggleState, isSWPAvailable ->
            isSWPAvailable && swpToggleState
        }).subscribe(swpEffectiveAvailability)

        shopWithPointsToggleObservable.skip(1).subscribe {
            HotelTracking.trackSwPToggle(it)
        }
        subscription = isShopWithPointsAvailableObservableIntermediateStream.subscribeObserver(isShopWithPointsAvailableObservable)
    }
}
