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
import com.expedia.bookings.itin.scopes.HasItinImageViewModelSetter
import com.expedia.bookings.itin.scopes.HasItinTimingsViewModelSetter
import com.expedia.bookings.itin.scopes.HasJsonUtil
import com.expedia.bookings.itin.scopes.HasManageBookingWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasMapWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasRedeemVoucherViewModelSetter
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasToolbarViewModelSetter
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.scopes.LxItinImageViewModelScope
import com.expedia.bookings.itin.scopes.LxItinManageBookingWidgetScope
import com.expedia.bookings.itin.scopes.LxItinMapWidgetViewModelScope
import com.expedia.bookings.itin.scopes.LxItinRedeemVoucherViewModelScope
import com.expedia.bookings.itin.scopes.LxItinTimingsScope
import com.expedia.bookings.itin.scopes.LxItinToolbarScope
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject

class LxItinDetailsActivityLifecycleObserver<S>(val scope: S) : DefaultLifecycleObserver where S : HasActivityLauncher, S : HasWebViewLauncher, S : HasStringProvider, S : HasJsonUtil, S : HasItinId, S : HasManageBookingWidgetViewModelSetter, S : HasToolbarViewModelSetter, S : HasTripsTracking, S : HasMapWidgetViewModelSetter, S : HasRedeemVoucherViewModelSetter, S : HasToaster, S : HasPhoneHandler, S : HasItinImageViewModelSetter, S : HasItinTimingsViewModelSetter<ItinLx> {

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
                scope.tripsTracking.trackItinLx(omnitureValues)
            }
        }

        val manageBookingScope = LxItinManageBookingWidgetScope(scope.strings, scope.webViewLauncher, scope.activityLauncher, repo, scope.tripsTracking)
        scope.manageBooking.setUpViewModel(LxItinManageBookingWidgetViewModel(manageBookingScope))

        val mapWidgetScope = LxItinMapWidgetViewModelScope(repo, owner, scope.tripsTracking, scope.toaster, scope.strings, scope.phoneHandler)
        scope.map.setUpViewModel(LxItinMapWidgetViewModel(mapWidgetScope))

        val redeemVoucherScope = LxItinRedeemVoucherViewModelScope(scope.strings, scope.webViewLauncher, repo, owner, scope.tripsTracking)
        scope.redeemVoucher.setUpViewModel(LxItinRedeemVoucherViewModel(redeemVoucherScope))

        val imageScope = LxItinImageViewModelScope(owner, repo)
        scope.itinImage.setupViewModel(LxItinImageViewModel(imageScope))

        val timingsScope = LxItinTimingsScope(owner, repo, scope.strings)
        scope.itinTimings.setupViewModel(LxItinTimingsWidgetViewModel(timingsScope))
    }

    override fun onDestroy(owner: LifecycleOwner) {
        repo.dispose()
    }
}
