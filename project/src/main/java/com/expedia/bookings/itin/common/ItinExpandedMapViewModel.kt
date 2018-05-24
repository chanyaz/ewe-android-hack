package com.expedia.bookings.itin.common

import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.extensions.getLatLng
import com.expedia.bookings.itin.tripstore.extensions.getNameLocationPair
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class ItinExpandedMapViewModel<S>(val scope: S) where S : HasItinRepo, S : HasActivityLauncher, S : HasLifecycleOwner, S : HasItinType, S : HasTripsTracking {
    var itinObserver: LiveDataObserver<Itin>
    val latLngSubject: BehaviorSubject<LatLng> = BehaviorSubject.create()
    val toolbarPairSubject: PublishSubject<Pair<String?, String?>> = PublishSubject.create()
    val directionButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val directionsReadySubject: PublishSubject<MapUri> = PublishSubject.create()
    val trackItinExpandedMapZoomInSubject: PublishSubject<Unit> = PublishSubject.create()
    val trackItinExpandedMapZoomOutSubject: PublishSubject<Unit> = PublishSubject.create()
    val trackItinExpandedMapZoomPanSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        itinObserver = LiveDataObserver {
            it?.let { itin ->
                val pair = getNamePair(itin)
                publishLatLong(itin, pair.first)
                toolbarPairSubject.onNext(pair)
            }
        }

        directionsReadySubject.subscribe { uri ->
            directionButtonClickSubject.subscribe {
                scope.tripsTracking.trackItinMapDirectionsButton()
                scope.activityLauncher.launchExternalMapActivity(uri)
            }
        }

        trackItinExpandedMapZoomInSubject.subscribe {
            scope.tripsTracking.trackItinExpandedMapZoomIn()
        }

        trackItinExpandedMapZoomOutSubject.subscribe {
            scope.tripsTracking.trackItinExpandedMapZoomOut()
        }

        trackItinExpandedMapZoomPanSubject.subscribe {
            scope.tripsTracking.trackItinExpandedMapZoomPan()
        }

        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

    fun getNamePair(itin: Itin): Pair<String?, String?> {
        when (scope.type) {
            TripProducts.ACTIVITY.name -> {
                itin.firstLx()?.getNameLocationPair()?.let { pair ->
                    return pair
                }
            }
        }
        return Pair("", "")
    }

    fun publishLatLong(itin: Itin, name: String?) {
        when (scope.type) {
            TripProducts.ACTIVITY.name -> {
                itin.firstLx()?.getLatLng()?.let { latLng ->
                    latLngSubject.onNext(latLng)
                    name?.let { title ->
                        directionsReadySubject.onNext(MapUri(latLng, title))
                    }
                }
            }
        }
    }

    data class MapUri(val latLng: LatLng, val title: String)
}
