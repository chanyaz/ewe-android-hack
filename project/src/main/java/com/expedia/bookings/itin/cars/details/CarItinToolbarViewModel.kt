package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinCar
import io.reactivex.subjects.PublishSubject

class CarItinToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasCarRepo {
    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()

    var itinLxObserver: LiveDataObserver<ItinCar>

    init {
        itinLxObserver = LiveDataObserver<ItinCar> {
            it?.let { itinCar ->
                val stringProvider = scope.strings
                itinCar.pickupLocation?.cityName?.let { carCity ->
                    val title = stringProvider.fetchWithPhrase(R.string.itin_car_toolbar_title_TEMPLATE, mapOf("location" to carCity))
                    toolbarTitleSubject.onNext(title)
                }
                val pickupDate = itinCar.pickupTime?.localizedMediumDate
                val dropOffDate = itinCar.dropOffTime?.localizedMediumDate
                if (!pickupDate.isNullOrEmpty() && !dropOffDate.isNullOrEmpty()) {
                    val subTitle = stringProvider.fetchWithPhrase(R.string.itin_car_toolbar_subtitle_date_to_date_TEMPLATE, mapOf("startdate" to pickupDate!!, "enddate" to dropOffDate!!))
                    toolbarSubTitleSubject.onNext(subTitle)
                }
            }
        }
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
