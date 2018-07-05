package com.expedia.bookings.itin.lx.moreHelp

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import io.reactivex.subjects.PublishSubject

class LxItinMoreHelpToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val itinShareTextGeneratorSubject: PublishSubject<ItinShareTextGenerator> = PublishSubject.create()

    val itinLxObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        itin?.firstLx()?.let { itinLx ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_more_help_text))

            itinLx.activityTitle?.let { title ->
                toolbarSubTitleSubject.onNext(title)
            }
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
