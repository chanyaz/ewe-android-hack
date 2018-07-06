package com.expedia.bookings.hotel.util

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import com.expedia.bookings.services.HotelShortlistServicesInterface
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import java.util.LinkedList

class HotelFavoritesManager(private val shortlistService: HotelShortlistServicesInterface) {

    private data class RequestData(val hotelId: String, val metadata: ShortlistItemMetadata?)

    val fetchSuccessSubject = PublishSubject.create<HotelShortlistResponse<HotelShortlistItem>>()
    @VisibleForTesting
    val saveSuccessSubject = PublishSubject.create<Unit>()
    @VisibleForTesting
    val removeSuccessSubject = PublishSubject.create<Unit>()

    private var saveDeleteRequestQueue = HashMap<String, LinkedList<RequestData>>()
    private var fetchRequestSubscription: Disposable? = null

    fun saveFavorite(context: Context, hotelId: String, searchParams: HotelSearchParams) {
        val roomConfiguration = formatRoomConfig(searchParams.adults, searchParams.children)
        val metadata = ShortlistItemMetadata().apply {
            this.hotelId = hotelId
            this.chkIn = searchParams.checkIn.toString("yyyyMMdd")
            this.chkOut = searchParams.checkOut.toString("yyyyMMdd")
            this.roomConfiguration = roomConfiguration
        }

        handleSaveFavoriteQueue(context, hotelId, metadata)
    }

    fun saveFavorite(context: Context, hotelShortlistItem: HotelShortlistItem) {
        val metadata = hotelShortlistItem.shortlistItem?.metaData
        val hotelId = hotelShortlistItem.getHotelId()
        if (hotelId != null && metadata != null) {
            handleSaveFavoriteQueue(context, hotelId, metadata)
        }
    }

    fun removeFavorite(context: Context, hotelId: String) {
        if (saveDeleteRequestQueue[hotelId] == null) {
            saveDeleteRequestQueue[hotelId] = LinkedList()
        }

        saveDeleteRequestQueue[hotelId]?.let { requestDataList ->
            requestDataList.add(RequestData(hotelId, null))
            if (requestDataList.size == 1) {
                callRemoveFavoriteService(context, hotelId)
            }
        }
    }

    fun fetchFavorites(context: Context) {
        fetchRequestSubscription?.dispose()
        fetchRequestSubscription = shortlistService.fetchFavoriteHotels(createFetchFavoritesObserver(context))
    }

    @VisibleForTesting
    fun formatRoomConfig(adults: Int, children: List<Int>): String {
        if (children.isEmpty()) {
            return adults.toString()
        }

        val childrenString = children.joinToString("-")
        return listOf(adults.toString(), childrenString).joinToString("|")
    }

    private fun createFetchFavoritesObserver(context: Context): Observer<HotelShortlistResponse<HotelShortlistItem>> {
        return object : DisposableObserver<HotelShortlistResponse<HotelShortlistItem>>() {
            override fun onNext(response: HotelShortlistResponse<HotelShortlistItem>) {
                saveToCache(response)
                fetchSuccessSubject.onNext(response)
            }

            //TODO Unhappy path
            override fun onError(e: Throwable) {}

            override fun onComplete() {}

            private fun saveToCache(response: HotelShortlistResponse<HotelShortlistItem>) {
                val favoriteIds = hashSetOf<String>()
                response.results.forEach { result ->
                    favoriteIds.addAll(result.items.mapNotNull { item -> item.getHotelId() })
                }
                HotelFavoritesCache.saveFavorites(context, favoriteIds)
            }
        }
    }

    private fun createSaveFavoriteObserver(context: Context, hotelId: String): Observer<HotelShortlistResponse<ShortlistItem>> {
        val successBlock = {
            HotelFavoritesCache.saveFavoriteId(context, hotelId)
            saveSuccessSubject.onNext(Unit)
        }

        return object : DisposableObserver<HotelShortlistResponse<ShortlistItem>>() {
            override fun onNext(response: HotelShortlistResponse<ShortlistItem>) {
                handleSaveOrRemoveSuccess(context, hotelId, successBlock)
            }

            //TODO Unhappy path
            override fun onError(e: Throwable) {
                handleSaveOrRemoveError(context, hotelId)
            }

            override fun onComplete() {}
        }
    }

    private fun createRemoveFavoriteObserver(context: Context, hotelId: String): Observer<ResponseBody> {
        val successBlock = {
            HotelFavoritesCache.removeFavoriteId(context, hotelId)
            removeSuccessSubject.onNext(Unit)
        }

        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(response: ResponseBody) {
                handleSaveOrRemoveSuccess(context, hotelId, successBlock)
            }

            //TODO Unhappy path
            override fun onError(e: Throwable) {
                handleSaveOrRemoveError(context, hotelId)
            }

            override fun onComplete() {}
        }
    }

    private fun handleSaveOrRemoveSuccess(context: Context, hotelId: String, successBlock: () -> Unit) {
        val requestDataList = saveDeleteRequestQueue[hotelId]
        if (requestDataList != null) {
            requestDataList.removeFirst()
            if (requestDataList.isEmpty()) {
                successBlock()
            } else {
                executeNextOperationOnTheQueue(context, hotelId)
            }
        } else {
            saveDeleteRequestQueue[hotelId] = LinkedList()
            successBlock()
        }
    }

    private fun handleSaveOrRemoveError(context: Context, hotelId: String) {
        saveDeleteRequestQueue[hotelId]?.let { requestDataList ->
            requestDataList.removeFirst()
            if (requestDataList.isNotEmpty()) {
                executeNextOperationOnTheQueue(context, hotelId)
            }
            // TODO: currently if last operation fail it'll not update cache and don't signal
            // should somehow cache last success request, handle error and use last success data to update cache if queue is empty
        }
    }

    private fun handleSaveFavoriteQueue(context: Context, hotelId: String, metadata: ShortlistItemMetadata) {
        if (saveDeleteRequestQueue[hotelId] == null) {
            saveDeleteRequestQueue[hotelId] = LinkedList()
        }

        saveDeleteRequestQueue[hotelId]?.let { requestDataList ->
            requestDataList.add(RequestData(hotelId, metadata))
            if (requestDataList.size == 1) {
                callSaveFavoriteService(context, hotelId, metadata)
            }
        }
    }

    private fun callSaveFavoriteService(context: Context, hotelId: String, metadata: ShortlistItemMetadata) {
        shortlistService.saveFavoriteHotel(hotelId, metadata, createSaveFavoriteObserver(context, hotelId))
    }

    private fun callRemoveFavoriteService(context: Context, hotelId: String) {
        shortlistService.removeFavoriteHotel(hotelId, createRemoveFavoriteObserver(context, hotelId))
    }

    private fun executeNextOperationOnTheQueue(context: Context, hotelId: String) {
        saveDeleteRequestQueue[hotelId]?.let { requestDataList ->
            if (requestDataList.isNotEmpty()) {
                val nextRequestData = requestDataList.first()
                if (nextRequestData.metadata == null) {
                    callRemoveFavoriteService(context, nextRequestData.hotelId)
                } else {
                    callSaveFavoriteService(context, nextRequestData.hotelId, nextRequestData.metadata)
                }
            }
        }
    }
}
