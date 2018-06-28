package com.expedia.bookings.itin.cruise.toolbar

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import io.reactivex.subjects.PublishSubject

class CruiseItinToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasItinRepo, S : HasStringProvider, S : HasLifecycleOwner, S : HasTripsTracking {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val itinShareTextGeneratorSubject: PublishSubject<ItinShareTextGenerator> = PublishSubject.create()

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        itin?.cruises?.first()?.let { itinCruise ->
            itinCruise.destination?.let { destination ->
                val toolbarTitle = scope.strings.fetchWithPhrase(R.string.itin_cruise_toolbar_title_TEMPLATE,
                        mapOf("destination" to destination))
                toolbarTitleSubject.onNext(toolbarTitle)
            }
            shareIconVisibleSubject.onNext(true)
            val shareTextGenerator = CruiseItinShareTextGenerator(itin.title ?: "", itin.tripNumber ?: "", itinCruise, scope.strings)
            itinShareTextGeneratorSubject.onNext(shareTextGenerator)
            shareIconClickedSubject.subscribe {
                scope.tripsTracking.trackItinCruiseShareIconClicked()
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
