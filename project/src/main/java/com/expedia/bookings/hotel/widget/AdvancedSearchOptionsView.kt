package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelStarRatingFilterView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.AdvancedSearchOptionsViewModel

class AdvancedSearchOptionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val toolbar: Toolbar by bindView(R.id.hotel_advanced_options_toolbar)
    private val hotelSortOptionsView: HotelSortOptionsView by bindView(R.id.hotel_sort_options)
    private val hotelNameFilterView: HotelNameFilterView by bindView(R.id.hotel_filter_name_view)
    private val starRatingView: HotelStarRatingFilterView by bindView(R.id.star_rating_container)

    private val filterVipView: HotelFilterVipView by bindView(R.id.filter_vip_view)
    private val vipTextLabel: TextView by bindView(R.id.vip_text_label)

    private val clearButton: CardView by bindView(R.id.advanced_options_clear_pill)
    private val doneButton: Button by bindView(R.id.advanced_options_done_button)

    var viewModel: AdvancedSearchOptionsViewModel by notNullAndObservable { vm ->
        bindViewModel(vm)
    }

    init {
        View.inflate(context, R.layout.hotel_search_advanced_options, this)

        starRatingView.reset()

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        if (PointOfSale.getPointOfSale().supportsVipAccess()) {
            filterVipView.visibility = View.VISIBLE
            vipTextLabel.visibility = View.VISIBLE
        }
    }

    override fun onFinishInflate() {
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }

        val sortList = DisplaySort.values().toMutableList()
        sortList.remove(DisplaySort.DISTANCE)
        sortList.remove(DisplaySort.PACKAGE_DISCOUNT)
        hotelSortOptionsView.updateSortItems(sortList)
    }

    private fun bindViewModel(vm: AdvancedSearchOptionsViewModel) {
        doneButton.subscribeOnClick(vm.doneObservable)
        clearButton.subscribeOnClick(vm.clearObservable)

        hotelSortOptionsView.sortSelectedSubject.subscribe { selectedSort ->
            vm.selectSortOption(selectedSort)
        }

        hotelNameFilterView.filterNameChangedSubject.subscribe { text ->
            vm.selectHotelName(text.toString())
        }

        starRatingView.starRatingsSubject.subscribe { starRating ->
            vm.updateStarRating(starRating)
        }

        filterVipView.vipCheckedSubject.subscribe { vipChecked ->
            vm.isVipAccess(vipChecked)
        }

        vm.resetViewsSubject.subscribe { searchOptions ->
            resetViews(searchOptions)
        }

        vm.showClearButtonSubject.subscribeVisibility(clearButton)
    }

    private fun resetViews(searchOptions: UserFilterChoices) {
        hotelSortOptionsView.setSort(searchOptions.userSort)
        hotelNameFilterView.reset()
        starRatingView.reset()
        filterVipView.reset()
    }
}