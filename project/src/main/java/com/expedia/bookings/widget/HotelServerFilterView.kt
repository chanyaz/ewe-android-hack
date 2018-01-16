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
import com.expedia.bookings.hotel.util.HotelAmenityHelper
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.HotelAmenityGridItem
import com.expedia.bookings.hotel.widget.ServerNeighborhoodFilterView
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.expedia.vm.hotel.HotelFilterViewModel
import rx.subjects.PublishSubject

class HotelServerFilterView(context: Context, attrs: AttributeSet?) : BaseHotelFilterView(context, attrs) {
    val staticClearFilterButton: CardView by bindView(R.id.hotel_server_filter_clear_pill)

    private val amenityToggledSubject = PublishSubject.create<String>()

    var amenityViews: ArrayList<HotelAmenityGridItem> = ArrayList()

    init {
        staticClearFilterButton.setOnClickListener(clearFilterClickListener)
    }

    override fun inflate() {
        View.inflate(context, R.layout.hotel_server_filter_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.HotelAmenityFilter)) {
            amenitiesLabelView.visibility = View.VISIBLE
            amenitiesGridView.visibility = View.VISIBLE
            HotelAmenityHelper.getFilterAmenities().forEach { amenity ->
                val gridItem = HotelAmenityGridItem(context, amenity)
                gridItem.amenitySelected.subscribe(amenityToggledSubject)
                gridItem.amenityDeselected.subscribe(amenityToggledSubject)
                amenitiesGridView.addView(gridItem)
                amenityViews.add(gridItem)
            }
        }
    }

    override fun bindViewModel(vm: BaseHotelFilterViewModel) {
        super.bindViewModel(vm)
        amenityToggledSubject.subscribe(vm.toggleAmenity)

        vm.finishClear.subscribe {
            staticClearFilterButton.visibility = GONE
            amenityViews.forEach { view -> view.deselect() }
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
        (vm as HotelFilterViewModel).searchOptionsUpdatedObservable.subscribe { newFilterOptions ->
            updateWithSearchOptions(newFilterOptions)
        }
    }

    override fun inflateNeighborhoodView(stub: ViewStub): BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.server_neighborhood_filter_stub
        return stub.inflate() as ServerNeighborhoodFilterView
    }

    fun updateWithSearchOptions(filterOptions: UserFilterChoices) {
        if (!filterOptions.name.isNullOrEmpty())  hotelNameFilterView.updateName(filterOptions.name)
        filterVipView.update(filterOptions.isVipOnlyAccess)
        starRatingView.update(filterOptions.hotelStarRating)
    }
}
