package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.ClientNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.HotelFilterVipView
import com.expedia.bookings.hotel.widget.HotelNameFilterView
import com.expedia.bookings.hotel.widget.HotelPriceFilterView
import com.expedia.bookings.hotel.widget.HotelSortOptionsView
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import io.reactivex.Observer

open class BaseHotelFilterView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)

    val filterVipView: HotelFilterVipView by bindView(R.id.filter_vip_view)
    val optionLabel: TextView by bindView(R.id.option_label)
    val hotelSortOptionsView: HotelSortOptionsView by bindView(R.id.hotel_sort_options)
    val starRatingView: HotelStarRatingFilterView by bindView(R.id.star_rating_container)
    val guestRatingView: HotelGuestRatingFilterView by bindView(R.id.guest_rating_container)
    val guestRatingLabelView: com.expedia.bookings.widget.TextView by bindView(R.id.guest_rating_label)
    val amenitiesLabelView: com.expedia.bookings.widget.TextView by bindView(R.id.filter_amenity_label)
    val amenitiesGridView: GridLayout by bindView(R.id.filter_amenities_grid_layout)
    val hotelNameFilterView: HotelNameFilterView by bindView(R.id.hotel_filter_name_view)

    protected val priceRangeView: HotelPriceFilterView by bindView(R.id.price_range_filter_view)
    private val priceHeader: View by bindView(R.id.price)
    private val filterContainer: ViewGroup by bindView(R.id.filter_container)

    val doneButton: Button by lazy {
        val button = LayoutInflater.from(context).inflate(R.layout.toolbar_checkmark_item, null) as Button
        button.setTextColor(ContextCompat.getColor(context, R.color.actionbar_text_color_inverse))
        button.setText(R.string.done)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.actionbar_text_color_inverse), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        button
    }
    val toolbarDropshadow: View by bindView(R.id.toolbar_dropshadow)
    val neighborhoodLabel: TextView by bindView(R.id.neighborhood_label)

    val neighborhoodView: BaseNeighborhoodFilterView by lazy {
        inflateNeighborhoodView(neighborhoodViewStub)
    }

    private val neighborhoodViewStub: ViewStub by bindView(R.id.neighborhood_view_stub)

    val ANIMATION_DURATION = 500L

    var shopWithPointsViewModel: ShopWithPointsViewModel? = null

    val sortByObserver: Observer<Boolean> = endlessObserver { isCurrentLocationSearch ->
        val sortList = DisplaySort.values().toMutableList()

        sortList.remove(viewModel.sortItemToRemove())

        if (!isCurrentLocationSearch) {
            sortList.remove(DisplaySort.DISTANCE)
        }

        // Remove Sort by Deals in case of SWP.
        if (shopWithPointsViewModel?.swpEffectiveAvailability?.value ?: false) {
            sortList.remove(DisplaySort.DEALS)
        }

        hotelSortOptionsView.updateSortItems(sortList)
    }

    protected lateinit var viewModel: BaseHotelFilterViewModel

    init {
        inflate()
        if (PointOfSale.getPointOfSale().supportsVipAccess()) {
            filterVipView.visibility = View.VISIBLE
            optionLabel.visibility = View.VISIBLE
        }

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color))
            val statusBar = Ui.setUpStatusBar(context, toolbar, filterContainer, color)
            addView(statusBar)
        }

        toolbar.inflateMenu(R.menu.cars_lx_filter_menu)
        toolbar.title = resources.getString(R.string.sort_and_filter)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.actionbar_text_color_inverse))

        toolbar.menu.findItem(R.id.apply_check).actionView = doneButton

        filterContainer.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = filterContainer.scrollY
            val ratio = (scrollY).toFloat() / 100
            toolbarDropshadow.alpha = ratio
        }

        resetStars()
        resetGuestRating()
    }

    fun initViewModel(viewModel: BaseHotelFilterViewModel) {
        this.viewModel = viewModel
        bindViewModel(viewModel)
    }

    open fun shakeForError() {
    }

    protected val clearFilterClickListener = View.OnClickListener { view ->
        view?.announceForAccessibility(context.getString(R.string.filters_cleared))
        viewModel.clearObservable.onNext(Unit)
        viewModel.trackClearFilter()
    }

    protected open fun bindViewModel(vm: BaseHotelFilterViewModel) {
        doneButton.subscribeOnClick(vm.doneObservable)
        vm.priceRangeContainerVisibility.subscribeVisibility(priceRangeView)
        vm.priceRangeContainerVisibility.subscribeVisibility(priceHeader)

        filterVipView.vipCheckedSubject.subscribe { vipChecked ->
            clearHotelNameFocus()
            vm.vipFilteredObserver.onNext(vipChecked)
        }

        neighborhoodView.neighborhoodOnSubject.subscribe(vm.selectNeighborhood)
        neighborhoodView.neighborhoodOffSubject.subscribe(vm.deselectNeighborhood)

        bindStarRating(vm)
        bindGuestRating(vm)

        hotelNameFilterView.filterNameChangedSubject.subscribe(vm.filterHotelNameObserver)

        vm.finishClear.subscribe {
            hotelNameFilterView.reset()
            resetStars()
            resetGuestRating()

            filterVipView.reset()
            neighborhoodView.clear()
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
            doneButton.isEnabled = enable
        }

        vm.sortSpinnerObservable.subscribe { sortType ->
            hotelSortOptionsView.setSort(sortType)
        }

        hotelSortOptionsView.sortSelectedSubject.subscribe { selectedSort ->
            vm.userFilterChoices.userSort = selectedSort

            val sortByString: String
            if (selectedSort == DisplaySort.PACKAGE_DISCOUNT) {
                sortByString = "Discounts"
            } else {
                sortByString = Strings.capitalizeFirstLetter(selectedSort.toString())
            }
            vm.trackHotelSortBy(sortByString)
        }

        hotelSortOptionsView.downEventSubject.subscribe {
            clearHotelNameFocus()
        }

        vm.neighborhoodListObservable.subscribe { list ->
            if (list != null && list.size > 0) {
                neighborhoodLabel.visibility = View.VISIBLE
                neighborhoodView.visibility = View.VISIBLE
                neighborhoodView.updateNeighborhoods(list)
            } else {
                neighborhoodLabel.visibility = View.GONE
                neighborhoodView.visibility = View.GONE
                neighborhoodView.updateNeighborhoods(emptyList())
            }
        }

        vm.sortContainerVisibilityObservable.subscribeVisibility(hotelSortOptionsView)

        priceRangeView.viewModel = vm
    }

    private fun bindStarRating(vm: BaseHotelFilterViewModel) {
        starRatingView.oneStarSubject.subscribe(vm.oneStarFilterObserver)
        starRatingView.twoStarSubject.subscribe(vm.twoStarFilterObserver)
        starRatingView.threeStarSubject.subscribe(vm.threeStarFilterObserver)
        starRatingView.fourStarSubject.subscribe(vm.fourStarFilterObserver)
        starRatingView.fiveStarSubject.subscribe(vm.fiveStarFilterObserver)
    }

    private fun bindGuestRating(vm: BaseHotelFilterViewModel) {
        guestRatingView.threeGuestRatingSubject.subscribe(vm.guestRatingThreeFilterObserver)
        guestRatingView.fourGuestRatingSubject.subscribe(vm.guestRatingFourFilterObserver)
        guestRatingView.fiveGuestRatingSubject.subscribe(vm.guestRatingFiveFilterObserver)
    }

    fun resetStars() {
        starRatingView.reset()
    }

    fun resetGuestRating() {
        guestRatingView.reset()
    }

    fun show() {
        visibility = View.VISIBLE
        if (viewModel.neighborhoodsExist) {
            neighborhoodView.post {
                //http://stackoverflow.com/questions/3602026/linearlayout-height-in-oncreate-is-0/3602144#3602144
                neighborhoodView.collapse()
            }
        }
    }

    protected open fun inflate() {
    }

    protected open fun inflateNeighborhoodView(stub: ViewStub): BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.client_neighborhood_filter_stub
        return stub.inflate() as ClientNeighborhoodFilterView
    }

    private fun clearHotelNameFocus() {
        hotelNameFilterView.resetFocus()
        com.mobiata.android.util.Ui.hideKeyboard(this)
    }
}
