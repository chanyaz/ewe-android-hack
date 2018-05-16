package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.HotelAmenityGridItem
import com.expedia.bookings.hotel.widget.ServerNeighborhoodFilterView
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelFilterViewModel

class HotelServerFilterView(context: Context, attrs: AttributeSet?) : BaseHotelFilterView(context, attrs) {
    val staticClearFilterButton: CardView by bindView(R.id.hotel_server_filter_clear_pill)

    val amenityViews: ArrayList<HotelAmenityGridItem> = ArrayList()

    init {
        staticClearFilterButton.setOnClickListener(clearFilterClickListener)
    }

    override fun inflate() {
        View.inflate(context, R.layout.hotel_server_filter_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelAmenityFilter)) {
            amenitiesLabelView.visibility = View.VISIBLE
            amenitiesGridView.visibility = View.VISIBLE
            Amenity.getFilterAmenities().forEach { amenity ->
                val gridItem = HotelAmenityGridItem(context, amenity)
                amenitiesGridView.addView(gridItem)
                amenityViews.add(gridItem)
            }
        }

        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelGuestRatingFilter)) {
            guestRatingView.visibility = View.VISIBLE
            guestRatingLabelView.visibility = View.VISIBLE
        }
    }

    override fun bindViewModel(vm: BaseHotelFilterViewModel) {
        super.bindViewModel(vm)
        vm.finishClear.subscribe {
            staticClearFilterButton.visibility = GONE
            amenityViews.forEach { view -> view.deselect() }
        }

        amenityViews.forEach { amenityGridItem ->
            amenityGridItem.setOnHotelAmenityFilterChangedListener(vm.onHotelAmenityFilterChangedListener)
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
        (vm as HotelFilterViewModel).presetFilterOptionsUpdatedSubject.subscribe { newFilterOptions ->
            updatePresetFilterChoices(newFilterOptions)
        }

        vm.availableAmenityOptionsObservable.subscribe { filterOptions ->
            amenityViews.forEach { view ->
                val key = Amenity.getSearchKey(view.amenity).toString()
                if (filterOptions.contains(key)) {
                    view.enable()
                } else {
                    view.disable()
                }
            }
        }
    }

    override fun inflateNeighborhoodView(stub: ViewStub): BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.server_neighborhood_filter_stub
        return stub.inflate() as ServerNeighborhoodFilterView
    }

    private fun updatePresetFilterChoices(filterOptions: UserFilterChoices) {
        hotelNameFilterView.updateName(filterOptions.name)
        hotelSortOptionsView.setSort(filterOptions.userSort)
        filterVipView.update(filterOptions.isVipOnlyAccess)
        starRatingView.update(filterOptions.hotelStarRating)
        guestRatingView.update(filterOptions.hotelGuestRating)
        priceRangeView.setMinMaxPrice(filterOptions.minPrice, filterOptions.maxPrice)
        amenityViews.forEach { amenityView ->
            if (amenityView.isAmenityEnabled() && filterOptions.amenities.contains(Amenity.getSearchKey(amenityView.amenity))) {
                amenityView.select()
            }
        }
        if (filterOptions.neighborhoods.isNotEmpty()) {
            (neighborhoodView as ServerNeighborhoodFilterView).selectNeighborhood(filterOptions.neighborhoods.first())
        }

        (viewModel as? HotelFilterViewModel)?.let { vm ->
            vm.setPreviousFilterChoices(vm.userFilterChoices.copy())
        }
    }
}
