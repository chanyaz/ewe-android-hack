package com.expedia.bookings.itin.lx.toolbar

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasPOSProvider
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import io.reactivex.subjects.PublishSubject

class LxItinToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasLxRepo, S : HasPOSProvider {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val itinShareTextGeneratorSubject: PublishSubject<ItinShareTextGenerator> = PublishSubject.create()

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        if (itin != null) {
            val startTime = itin.startTime?.localizedMediumDate
            if (!startTime.isNullOrEmpty()) {
                toolbarSubTitleSubject.onNext(startTime!!)
            }
        }
    }

    val itinLxObserver: LiveDataObserver<ItinLx> = LiveDataObserver { itinLx ->
        if (itinLx != null) {
            val stringProvider = scope.strings
            val lxCity = itinLx.activityLocation?.city
            if (lxCity != null) {
                val title = stringProvider.fetchWithPhrase(R.string.itin_lx_toolbar_title_TEMPLATE, mapOf("location" to lxCity))
                toolbarTitleSubject.onNext(title)
            }
            val title = itinLx.activityTitle ?: ""
            val startDate = itinLx.startTime?.localizedFullDate ?: ""
            val endDate = itinLx.endTime?.localizedFullDate ?: ""
            val travelers = itinLx.travelers
            shareIconVisibleSubject.onNext(true)
            val shareItinTextCreator = LxItinShareTextGenerator(title, startDate, endDate, travelers, scope.strings, scope.posInfoProvider)
            itinShareTextGeneratorSubject.onNext(shareItinTextCreator)
        }
    }

    init {
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
