package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import io.reactivex.subjects.PublishSubject

class CarItinMoreHelpToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val itinShareTextGeneratorSubject: PublishSubject<ItinShareTextGenerator> = PublishSubject.create()
    var itinCarObserver: LiveDataObserver<Itin>

    init {
        itinCarObserver = LiveDataObserver { itin ->
            itin?.firstCar()?.let { itinCar ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_car_more_info_heading))
            itinCar.pickupLocation?.cityName?.let { carCity ->
                val title = scope.strings.fetchWithPhrase(R.string.itin_car_toolbar_title_TEMPLATE, mapOf("location" to carCity))
                toolbarSubTitleSubject.onNext(title)
            }
        }
        }
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinCarObserver)
    }
}
