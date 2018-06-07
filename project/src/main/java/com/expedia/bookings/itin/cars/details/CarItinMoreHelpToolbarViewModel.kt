package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinCar
import io.reactivex.subjects.PublishSubject

class CarItinMoreHelpToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasCarRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    var itinCarObserver: LiveDataObserver<ItinCar>

    init {
        itinCarObserver = LiveDataObserver { itinCar ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_car_more_info_heading))
            itinCar?.pickupLocation?.cityName?.let { carCity ->
                val title = scope.strings.fetchWithPhrase(R.string.itin_car_toolbar_title_TEMPLATE, mapOf("location" to carCity))
                toolbarSubTitleSubject.onNext(title)
            }
        }
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinCarObserver)
    }
}
