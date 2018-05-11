package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.HotelAmenityGridItem
import com.expedia.bookings.hotel.widget.ServerNeighborhoodFilterView
import com.expedia.bookings.hotel.vm.BaseHotelFilterViewModel

class HotelServerFilterView(context: Context, attrs: AttributeSet?) : BaseHotelServerFilterView(context, attrs) {

    val amenityViews: ArrayList<HotelAmenityGridItem> = ArrayList()

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
            amenityViews.forEach { view -> view.deselect() }
        }

        amenityViews.forEach { amenityGridItem ->
            amenityGridItem.setOnHotelAmenityFilterChangedListener(vm.onHotelAmenityFilterChangedListener)
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

        vm.presetFilterOptionsUpdatedSubject.subscribe { newFilterOptions ->
            amenityViews.forEach { amenityView ->
                if (amenityView.isAmenityEnabled() && newFilterOptions.amenities.contains(Amenity.getSearchKey(amenityView.amenity))) {
                    amenityView.select()
                }
            }
            if (newFilterOptions.neighborhoods.isNotEmpty()) {
                (neighborhoodView as ServerNeighborhoodFilterView).selectNeighborhood(newFilterOptions.neighborhoods.first())
            }
        }
    }

    override fun inflateNeighborhoodView(stub: ViewStub): BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.server_neighborhood_filter_stub
        return stub.inflate() as ServerNeighborhoodFilterView
    }
}
