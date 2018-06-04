package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.hotel.vm.BaseHotelFilterViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseHotelFilterView

open class BaseHotelServerFilterView(context: Context, attrs: AttributeSet?) : BaseHotelFilterView(context, attrs) {
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
                staticClearFilterButton.visibility = GONE
            } else {
                val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
                if (AccessibilityUtil.isTalkBackEnabled(context)) {
                    event?.contentDescription = context.resources.getString(R.string.search_filter_clear_button_alert_cont_desc)
                    staticClearFilterButton.requestSendAccessibilityEvent(staticClearFilterButton, event)
                }
                staticClearFilterButton.visibility = VISIBLE
            }
        }
        vm.presetFilterOptionsUpdatedSubject.subscribe { newFilterOptions ->
            updatePresetFilterChoices(newFilterOptions)
            vm.previousFilterChoices = vm.userFilterChoices.copy()
        }
    }

    private fun updatePresetFilterChoices(filterOptions: UserFilterChoices) {
        hotelNameFilterView.updateName(filterOptions.name)
        hotelSortOptionsView.setSort(filterOptions.userSort)
        filterVipView.update(filterOptions.isVipOnlyAccess)
        starRatingView.update(filterOptions.hotelStarRating)
        guestRatingView.update(filterOptions.hotelGuestRating)
        priceRangeView.setMinMaxPrice(filterOptions.minPrice, filterOptions.maxPrice)
    }
}
