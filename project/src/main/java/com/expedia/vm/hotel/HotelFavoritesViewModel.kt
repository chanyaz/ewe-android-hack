package com.expedia.vm.hotel

import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.services.HotelShortlistServices
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.ReplaySubject

class HotelFavoritesViewModel(userStateManager: UserStateManager, hotelShortlistServices: HotelShortlistServices) {
    var receivedResponseSubject = ReplaySubject.create<Unit>()

    var response: HotelShortlistResponse<HotelShortlistItem>? = null
        @VisibleForTesting set(value) {
            field = value
            value?.results?.forEach { result -> favoritesList.addAll(result.items) }
        }

    var favoritesList: ArrayList<HotelShortlistItem> = arrayListOf()
        private set

    @VisibleForTesting val compositeDisposable = CompositeDisposable()

    private val favoritesObserver = object : Observer<HotelShortlistResponse<HotelShortlistItem>> {
        override fun onError(e: Throwable) {
            // TODO
        }

        override fun onNext(fetchResponse: HotelShortlistResponse<HotelShortlistItem>) {
            response = fetchResponse
            receivedResponseSubject.onNext(Unit)
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
        }

        override fun onComplete() {
            // Not needed
        }
    }

    init {
        if (userStateManager.isUserAuthenticated()) {
            hotelShortlistServices.fetchFavoriteHotels(favoritesObserver)
        }
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    fun shouldShowList(): Boolean = favoritesList.isNotEmpty()
}
