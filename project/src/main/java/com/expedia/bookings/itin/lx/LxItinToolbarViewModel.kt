package com.expedia.bookings.itin.lx

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

class LxItinToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasLxRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    var itinObserver: LiveDataObserver<Itin>
    var itinLxObserver: LiveDataObserver<ItinLx>

    init {
        itinLxObserver = LiveDataObserver<ItinLx> { itinLx ->
            if (itinLx != null) {
                val stringProvider = scope.strings
                val lxCity = itinLx.activityLocation?.city
                if (lxCity != null) {
                    val title = stringProvider.fetchWithPhrase(R.string.itin_lx_toolbar_title_TEMPLATE, mapOf("location" to lxCity))
                    toolbarTitleSubject.onNext(title)
                }
            }
        }

        itinObserver = LiveDataObserver<Itin> { itin ->
            if (itin != null) {
                val startTime = itin.startTime?.localizedMediumDate
                if (!startTime.isNullOrEmpty()) {
                    toolbarSubTitleSubject.onNext(startTime!!)
                }
            }
        }
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
