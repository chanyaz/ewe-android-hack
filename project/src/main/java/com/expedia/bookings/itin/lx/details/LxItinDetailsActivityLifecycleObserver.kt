package com.expedia.bookings.itin.lx.details

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.lx.ItinLxRepo
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.lx.LxItinToolbarViewModel
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinId
import com.expedia.bookings.itin.scopes.HasJsonUtil
import com.expedia.bookings.itin.scopes.HasManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToolbarViewModelSetter
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.LxItinManageBookingWidgetScope
import com.expedia.bookings.itin.scopes.LxItinToolbarScope
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject

class LxItinDetailsActivityLifecycleObserver<S>(val scope: S) : DefaultLifecycleObserver where S : HasActivityLauncher, S : HasWebViewLauncher, S : HasStringProvider, S : HasJsonUtil, S : HasItinId, S : HasManageBookingWidgetViewModelSetter, S : HasToolbarViewModelSetter {

    val finishSubject = PublishSubject.create<Unit>()
    var repo: ItinLxRepoInterface = ItinLxRepo(scope.id, scope.jsonUtil, ItineraryManager.getInstance().syncFinishObservable)

    var toolbarViewModel: NewItinToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishSubject.onNext(Unit)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        repo.invalidDataSubject.subscribe {
            finishSubject.onNext(it)
        }
        val toolbarScope = LxItinToolbarScope(scope.strings, repo, owner)
        toolbarViewModel = LxItinToolbarViewModel(toolbarScope)
        scope.toolbar.setUpViewModel(toolbarViewModel)
        val itin = repo.liveDataItin.value
        itin?.let { trip ->
            trip.firstLx()?.let {
                val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(trip, ItinOmnitureUtils.LOB.LX)
                TripsTracking.trackItinLx(omnitureValues)
            }
        }

        val manageBookingScope = LxItinManageBookingWidgetScope(scope.strings, scope.webViewLauncher, scope.activityLauncher, repo)
        scope.manageBooking.setUpViewModel(LxItinManageBookingWidgetViewModel(manageBookingScope))
    }

    override fun onDestroy(owner: LifecycleOwner) {
        repo.dispose()
    }
}
