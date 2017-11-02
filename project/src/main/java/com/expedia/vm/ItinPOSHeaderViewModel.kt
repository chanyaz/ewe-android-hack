package com.expedia.vm

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.model.PointOfSaleStateModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

// This object is injected. Part of TripModule
class ItinPOSHeaderViewModel(pointOfSaleStateModel: PointOfSaleStateModel) {

    val posTextViewSubject = BehaviorSubject.create<String>()
    val posImageViewSubject = BehaviorSubject.create<Int>()
    val posUrlSubject = BehaviorSubject.create<String>()

    init {
        pointOfSaleStateModel.pointOfSaleChangedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { selectedPos ->
                    posImageViewSubject.onNext(selectedPos.countryFlagResId)
                    posTextViewSubject.onNext(selectedPos.threeLetterCountryCode)
                    posUrlSubject.onNext(ProductFlavorFeatureConfiguration.getInstance().getPosURLToShow(selectedPos.url.capitalize()))
                }

        pointOfSaleStateModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
    }
}
