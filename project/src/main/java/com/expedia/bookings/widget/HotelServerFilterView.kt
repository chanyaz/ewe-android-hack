package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.ServerNeighborhoodFilterView
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelFilterViewModel

class HotelServerFilterView(context: Context, attrs: AttributeSet?) : BaseHotelFilterView(context, attrs) {
    val staticClearFilterButton: CardView by bindView(R.id.hotel_server_filter_clear_pill)

    init {
        staticClearFilterButton.setOnClickListener(clearFilterClickListener)
    }

    override fun inflate() {
        View.inflate(context, R.layout.hotel_server_filter_view, this)
    }

    override fun bindViewModel(vm: BaseHotelFilterViewModel) {
        super.bindViewModel(vm)
        vm.finishClear.subscribe {
            staticClearFilterButton.visibility = GONE
        }

        vm.filterCountObservable.subscribe { count ->
            if (count <= 0) {
            } else {
                staticClearFilterButton.visibility = VISIBLE
            }
        }
        (vm as HotelFilterViewModel).searchOptionsUpdatedObservable.subscribe { newFilterOptions ->
            updateWithSearchOptions(newFilterOptions)
        }
    }

    override fun inflateNeighborhoodView(stub: ViewStub): BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.server_neighborhood_filter_stub;
        return stub.inflate() as ServerNeighborhoodFilterView
    }

    fun updateWithSearchOptions(filterOptions: UserFilterChoices) {
        if (!filterOptions.name.isNullOrEmpty())  hotelNameFilterView.updateName(filterOptions.name)
        filterVipView.update(filterOptions.isVipOnlyAccess)
        starRatingView.update(filterOptions.hotelStarRating)
    }
}