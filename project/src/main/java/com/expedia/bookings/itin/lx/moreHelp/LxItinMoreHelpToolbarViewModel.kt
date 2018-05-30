package com.expedia.bookings.itin.lx.moreHelp

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

class LxItinMoreHelpToolbarViewModel<S>(val scope: S) : NewItinToolbarViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasLxRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    var itinLxObserver: LiveDataObserver<ItinLx>

    init {
        itinLxObserver = LiveDataObserver { itinLx ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_lx_more_info_heading))
            itinLx?.activityTitle?.let { title ->
                toolbarSubTitleSubject.onNext(title)
            }
        }
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
    }
}
