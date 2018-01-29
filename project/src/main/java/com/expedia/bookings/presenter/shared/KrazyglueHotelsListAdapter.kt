package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class KrazyglueHotelsListAdapter(hotelsObservable: PublishSubject<List<KrazyglueResponse.KrazyglueHotel>>, val hotelSearchParams: BehaviorSubject<HotelSearchParams>, val regionId: BehaviorSubject<String>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class KrazyglueViewHolderType {
        LOADING_VIEW,
        HOTEL_VIEW_HOLDER,
        SEE_MORE_VIEW_HOLDER;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewType = getKrazyGlueViewHolderTypeFromInt(holder.itemViewType)
        when (viewType) {
            KrazyglueViewHolderType.HOTEL_VIEW_HOLDER -> {
                val hotelPosition = getHotelPositionBasedOnABTest(position)
                (holder as KrazyglueHotelViewHolder).viewModel.hotelObservable.onNext(hotels[hotelPosition])
                holder.trackingPosition = hotelPosition + 1
            }
        }
    }

    var hotels = arrayListOf<KrazyglueResponse.KrazyglueHotel>()
    val abacusVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsKrazyglue)
    var loading = true

    init {
        hotelsObservable.subscribe { newHotels ->
            loading = false
            hotels.clear()
            hotels.addAll(newHotels)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        if (loading) {
            return 3
        } else {
            return hotels.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loading) {
            return KrazyglueViewHolderType.LOADING_VIEW.ordinal
        } else {
            if (abacusVariant == AbacusVariant.ONE.value) {
                if (position == 0) return KrazyglueViewHolderType.SEE_MORE_VIEW_HOLDER.ordinal else return KrazyglueViewHolderType.HOTEL_VIEW_HOLDER.ordinal
            } else if (abacusVariant == AbacusVariant.TWO.value) {
                if (position == hotels.size) return KrazyglueViewHolderType.SEE_MORE_VIEW_HOLDER.ordinal else return KrazyglueViewHolderType.HOTEL_VIEW_HOLDER.ordinal
            }
            return KrazyglueViewHolderType.HOTEL_VIEW_HOLDER.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (getKrazyGlueViewHolderTypeFromInt(viewType)) {
            KrazyglueViewHolderType.HOTEL_VIEW_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_hotel_view, parent, false)
                view.layoutParams = getUpdatedLayoutParams(parent.width)
                return KrazyglueHotelViewHolder(view as ViewGroup, hotelSearchParams, regionId)
            }
            KrazyglueViewHolderType.SEE_MORE_VIEW_HOLDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_see_more_hotel_view, parent, false)
                view.layoutParams = getUpdatedLayoutParams(parent.width)
                return KrazyglueSeeMoreViewHolder(view as ViewGroup, context, hotelSearchParams, regionId)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.krazyglue_placeholder_hotel_cell, parent, false)
                return KrazyglueLoadingViewHolder(view)
            }
        }
    }

    fun getUpdatedLayoutParams(parentWidth: Int): ViewGroup.MarginLayoutParams {
        val displayDensity = context.resources.displayMetrics.density
        val newMargins = (9 * displayDensity).toInt()
        val newLayoutParams = ViewGroup.LayoutParams((parentWidth * 0.8).toInt(), (displayDensity * 90).toInt())
        val marginParams = ViewGroup.MarginLayoutParams(newLayoutParams)
        marginParams.setMargins(0, newMargins, newMargins, newMargins)
        return marginParams
    }

    private fun getHotelPositionBasedOnABTest(position: Int): Int {
        var hotelRetrievePosition = position
        if (abacusVariant == AbacusVariant.ONE.value) {
            hotelRetrievePosition --
        }
        return hotelRetrievePosition
    }

    fun getKrazyGlueViewHolderTypeFromInt(viewType: Int): KrazyglueViewHolderType {
        return KrazyglueViewHolderType.values()[viewType]
    }
}
