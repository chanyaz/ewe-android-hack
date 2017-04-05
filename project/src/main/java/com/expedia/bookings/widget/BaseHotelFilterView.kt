package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.hotel.widget.BaseNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.ClientNeighborhoodFilterView
import com.expedia.bookings.hotel.widget.HotelPriceFilterView
import com.expedia.bookings.hotel.widget.ServerNeighborhoodFilterView
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeVisibility
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import rx.Observer

open class BaseHotelFilterView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterVipContainer: View by bindView(R.id.filter_vip_container)
    val optionLabel: TextView by bindView(R.id.option_label)
    val priceHeader: View by bindView(R.id.price)
    val priceRangeView: HotelPriceFilterView by bindView(R.id.price_range_filter_view)
    val starRatingView: HotelStarRatingFilterView by bindView(R.id.star_rating_container)
    val sortContainer: LinearLayout by bindView(R.id.sort_hotel)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: AccessibleEditText by bindView(R.id.filter_hotel_name_edit_text)
    val clearNameButton: ImageView by bindView(R.id.clear_search_button)
    val filterContainer: ViewGroup by bindView(R.id.filter_container)

    val doneButton: Button by lazy {
        val button = LayoutInflater.from(context).inflate(R.layout.toolbar_checkmark_item, null) as Button
        button.setTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))
        button.setText(R.string.done)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN)
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

    val sortByAdapter = object : ArrayAdapter<Sort>(getContext(), R.layout.spinner_sort_dropdown_item) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView: TextView = super.getView(position, convertView, parent) as TextView
            textView.text = resources.getString(getItem(position).resId)
            return textView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return getView(position, convertView, parent)
        }
    }

    var shopWithPointsViewModel: ShopWithPointsViewModel? = null

    val sortByObserver: Observer<Boolean> = endlessObserver { isCurrentLocationSearch ->
        sortByAdapter.clear()
        val sortList = Sort.values().toMutableList()
        sortList.remove(viewModel.sortItemToRemove())
        sortByAdapter.addAll(sortList)

        if (!isCurrentLocationSearch) {
            sortByAdapter.remove(sortList.first { it == Sort.DISTANCE })
        }

        // Remove Sort by Deals in case of SWP.
        if (shopWithPointsViewModel?.swpEffectiveAvailability?.value ?: false) {
            sortByAdapter.remove(sortList.first { it == Sort.DEALS })
        }

        sortByAdapter.notifyDataSetChanged()
        sortByButtonGroup.setSelection(0, false)
    }

    var viewModel: BaseHotelFilterViewModel by notNullAndObservable { vm ->
        bindViewModel(vm)
    }

    init {
        inflate()
        if (PointOfSale.getPointOfSale().supportsVipAccess()) {
            filterVipContainer.visibility = View.VISIBLE
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
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))

        toolbar.menu.findItem(R.id.apply_check).actionView = doneButton

        filterContainer.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = filterContainer.scrollY
            val ratio = (scrollY).toFloat() / 100
            toolbarDropshadow.alpha = ratio
        }

        sortByAdapter.setNotifyOnChange(false)
        sortByButtonGroup.adapter = sortByAdapter
        resetStars()
    }

    open fun shakeForError() {}

    protected val clearFilterClickListener = View.OnClickListener { view ->
        view?.announceForAccessibility(context.getString(R.string.filters_cleared))
        viewModel.clearObservable.onNext(Unit)
        viewModel.trackClearFilter()
    }

    open protected fun bindViewModel(vm: BaseHotelFilterViewModel) {
        doneButton.subscribeOnClick(vm.doneObservable)
        vm.priceRangeContainerVisibility.subscribeVisibility(priceRangeView)
        vm.priceRangeContainerVisibility.subscribeVisibility(priceHeader)
        filterVipContainer.setOnClickListener {
            clearHotelNameFocus()
            filterHotelVip.isChecked = !filterHotelVip.isChecked
            vm.vipFilteredObserver.onNext(filterHotelVip.isChecked)
        }

        neighborhoodView.neighborhoodOnSubject.subscribe(vm.selectNeighborhood)
        neighborhoodView.neighborhoodOffSubject.subscribe(vm.deselectNeighborhood)

        vm.finishClear.subscribe {
            //check if filterHotelName is empty to avoid calling handleFiltering
            if (filterHotelName.text.length > 0) filterHotelName.text.clear()
            resetStars()

            filterHotelVip.isChecked = false
            neighborhoodView.clear()
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
            doneButton.isEnabled = enable
        }

        filterHotelName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearNameButton.visibility = if (Strings.isEmpty(s)) View.GONE else View.VISIBLE
                vm.filterHotelNameObserver.onNext(s)
            }
        })

        filterHotelName.setOnFocusChangeListener { view, isFocus ->
            if (!isFocus) {
                Ui.hideKeyboard(this)
            }
        }

        clearNameButton.setOnClickListener { view ->
            filterHotelName.text = null
        }

        vm.sortSpinnerObservable.subscribe { sortType ->
            val position = sortByAdapter.getPosition(sortType)
            sortByButtonGroup.setSelection(position, false)
        }

        sortByButtonGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                vm.userFilterChoices.userSort = sortByAdapter.getItem(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        sortByButtonGroup.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearHotelNameFocus()
            }
            false
        }

        vm.neighborhoodListObservable.subscribe { list ->
            if (list != null && list.size > 1) {
                neighborhoodLabel.visibility = View.VISIBLE
                neighborhoodView.visibility = View.VISIBLE
                neighborhoodView.updateNeighborhoods(list)
            } else {
                neighborhoodLabel.visibility = View.GONE
                neighborhoodView.visibility = View.GONE
            }
        }

        vm.sortContainerVisibilityObservable.subscribeVisibility(sortContainer)

        priceRangeView.viewModel = viewModel
        starRatingView.viewModel = viewModel
    }

    fun resetStars() {
        starRatingView.reset()
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

    open protected fun inflate() {
    }

    open protected fun inflateNeighborhoodView(stub: ViewStub) : BaseNeighborhoodFilterView {
        stub.layoutResource = R.layout.client_neighborhood_filter_stub;
        return stub.inflate() as ClientNeighborhoodFilterView
    }

    private fun clearHotelNameFocus() {
        filterHotelName.clearFocus()
        com.mobiata.android.util.Ui.hideKeyboard(this)
    }
}
