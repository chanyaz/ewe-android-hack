package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.UserAccountRefresher
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import java.text.NumberFormat

class ShopWithPointsViewModel(val context: Context) {

    val isShopWithPointsAvailableObservable = UserAccountRefresher.userAccountRefreshed
            .startWith(User.isLoggedIn(context))
            .map { isShopWithPointsAvailable(it) }

    val shopWithPointsToggleObservable = BehaviorSubject.create<Boolean>(true)

    val numberOfPointsObservable = isShopWithPointsAvailableObservable.map {
        Db.getUser()?.loyaltyMembershipInformation?.loyaltyPointsAvailable ?: 0.0
    }

    val pointsDetailStringObservable = numberOfPointsObservable.map {
        Phrase.from(context.resources, R.string.swp_widget_points_value_TEMPLATE).put("points", NumberFormat.getInstance().format(it.toInt())).format().toString()
    }

    private fun isShopWithPointsAvailable(isUserLoggedIn: Boolean): Boolean = isUserLoggedIn && PointOfSale.getPointOfSale().isSWPEnabledForHotels
            && Db.getUser().loyaltyMembershipInformation?.isAllowedToShopWithPoints ?: false

}
