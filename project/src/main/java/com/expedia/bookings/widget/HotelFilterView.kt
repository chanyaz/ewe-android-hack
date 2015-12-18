package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.MotionEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ImageView
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Strings
import com.expedia.android.rangeseekbar.RangeSeekBar
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelFilterViewModel.Sort
import rx.Observer
import java.util.ArrayList

public class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterVipBadge: TextView by bindView(R.id.vip_badge)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterVipContainer: View by bindView(R.id.filter_vip_container)
    val filterStarOne: ImageButton by bindView(R.id.filter_hotel_star_rating_one)
    val filterStarTwo: ImageButton by bindView(R.id.filter_hotel_star_rating_two)
    val filterStarThree: ImageButton by bindView(R.id.filter_hotel_star_rating_three)
    val filterStarFour: ImageButton by bindView(R.id.filter_hotel_star_rating_four)
    val filterStarFive: ImageButton by bindView(R.id.filter_hotel_star_rating_five)
    val ratingOneBackground: View by bindView(R.id.rating_one_background)
    val ratingTwoBackground: View by bindView(R.id.rating_two_background)
    val ratingThreeBackground: View by bindView(R.id.rating_three_background)
    val ratingFourBackground: View by bindView(R.id.rating_four_background)
    val ratingFiveBackground: View by bindView(R.id.rating_five_background)
    val sortContainer: LinearLayout by bindView(R.id.sort_hotel)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: TextView by bindView(R.id.filter_hotel_name_edit_text)
    val clearNameButton: ImageView by bindView(R.id.clear_search_button)
    val dynamicFeedbackWidget: DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton: TextView by bindView(R.id.dynamic_feedback_clear_button)
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
    val priceRangeBar: FilterRangeSeekBar by bindView(R.id.price_range_bar)
    val priceRangeMinText: TextView by bindView(R.id.price_range_min_text)
    val priceRangeMaxText: TextView by bindView(R.id.price_range_max_text)
    val neighborhoodLabel: TextView by bindView(R.id.neighborhood_label)
    val neighborhoodContainer: LinearLayout by bindView(R.id.neighborhoods)
    val neighborhoodMoreLessLabel: TextView by bindView(R.id.show_more_less_text)
    val neighborhoodMoreLessIcon: ImageButton by bindView(R.id.show_more_less_icon)
    val neighborhoodMoreLessView: RelativeLayout by bindView(R.id.collapsed_container)

    val amenityLabel: TextView by bindView(R.id.amenity_label)
    val amenityContainer: GridLayout by bindView(R.id.amenities_container)

    val rowHeight = resources.getDimensionPixelSize(R.dimen.hotel_neighborhood_height)
    val ANIMATION_DURATION = 500L

    val sortByObserver: Observer<Boolean> = endlessObserver { isCurrentLocationSearch ->
        var sortOptions: ArrayList<String> = resources.getStringArray(R.array.sort_options_material_hotels).toArrayList()

        if (isCurrentLocationSearch) {
            sortOptions.add(getContext().getString(R.string.distance))
        }

        val adapter = ArrayAdapter(getContext(), R.layout.spinner_sort_item, sortOptions)
        adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item)

        sortByButtonGroup.adapter = adapter
    }

    var viewmodel: HotelFilterViewModel by notNullAndObservable { vm ->
        doneButton.subscribeOnClick(vm.doneObservable)
        filterVipContainer.setOnClickListener {
            clearHotelNameFocus()
            filterHotelVip.isChecked = !filterHotelVip.isChecked
            vm.vipFilteredObserver.onNext(filterHotelVip.isChecked)
        }

        filterStarOne.subscribeOnClick(vm.oneStarFilterObserver)
        filterStarTwo.subscribeOnClick(vm.twoStarFilterObserver)
        filterStarThree.subscribeOnClick(vm.threeStarFilterObserver)
        filterStarFour.subscribeOnClick(vm.fourStarFilterObserver)
        filterStarFive.subscribeOnClick(vm.fiveStarFilterObserver)

        dynamicFeedbackClearButton.setOnClickListener {
            vm.clearObservable.onNext(Unit)
            HotelV2Tracking().trackLinkHotelV2ClearFilter()
        }

        vm.finishClear.subscribe {
            //check if filterHotelName is empty to avoid calling handleFiltering
            if (filterHotelName.text.length > 0) filterHotelName.text = ""
            resetStars()

            filterHotelVip.isChecked = false

            for (i in 0..amenityContainer.childCount - 1) {
                val v = amenityContainer.getChildAt(i)
                if (v is HotelAmenityFilter && v.amenitySelected) {
                    v.amenitySelected = false
                    v.changeColor(ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color))
                }
            }

            for (i in 0..neighborhoodContainer.childCount - 1) {
                val v = neighborhoodContainer.getChildAt(i)
                if (v is HotelsNeighborhoodFilter) {
                    v.neighborhoodCheckBox.isChecked = false
                }
            }

            dynamicFeedbackWidget.hideDynamicFeedback()
        }

        vm.newPriceRangeObservable.subscribe { priceRange ->
            priceRangeBar.setUpperLimit(priceRange.notches)
            priceRangeMinText.text = priceRange.defaultMinPriceText
            priceRangeMaxText.text = priceRange.defaultMaxPriceTest

            priceRangeBar.setOnRangeSeekBarChangeListener(object : RangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: RangeSeekBar?, minValue: Int, maxValue: Int) {
                    priceRangeMinText.text = priceRange.formatValue(minValue)
                    priceRangeMaxText.text = priceRange.formatValue(maxValue)
                }

                override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar?, minValue: Int, maxValue: Int) {
                    priceRangeMinText.text = priceRange.formatValue(minValue)
                    priceRangeMaxText.text = priceRange.formatValue(maxValue)
                    vm.priceRangeChangedObserver.onNext(priceRange.update(minValue, maxValue))
                }
            })
        }

        vm.hotelStarRatingBar.subscribe {
            if (it == 1) {
                starSelection(filterStarOne, ratingOneBackground, -1)
            } else if (it == 6) {
                starSelection(filterStarOne, ratingOneBackground, 1)
            }
            if (it == 2) {
                starSelection(filterStarTwo, ratingTwoBackground, -1)
            } else if (it == 7) {
                starSelection(filterStarTwo, ratingTwoBackground, 2)
            }
            if (it == 3) {
                starSelection(filterStarThree, ratingThreeBackground, -1)
            } else if (it == 8) {
                starSelection(filterStarThree, ratingThreeBackground, 3)
            }
            if (it == 4) {
                starSelection(filterStarFour, ratingFourBackground, -1)
            } else if (it == 9) {
                starSelection(filterStarFour, ratingFourBackground, 4)
            }
            if (it == 5) {
                starSelection(filterStarFive, ratingFiveBackground, -1)
            } else if (it == 10) {
                starSelection(filterStarFive, ratingFiveBackground, 5)
            }

            if (it <= 5) {
                HotelV2Tracking().trackLinkHotelV2RefineRating(it.toString())
            }
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            if (it < 0) {
                dynamicFeedbackWidget.hideDynamicFeedback()
            } else {
                dynamicFeedbackWidget.showDynamicFeedback()
                dynamicFeedbackWidget.setDynamicCounterText(it)
            }
        }

        vm.filteredZeroResultObservable.subscribe {
            dynamicFeedbackWidget.animateDynamicFeedbackWidget()
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
                com.mobiata.android.util.Ui.hideKeyboard(this)
            }
        }

        clearNameButton.setOnClickListener { view ->
            filterHotelName.text = null
        }

        sortByButtonGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val sort = Sort.values()[position]
                vm.userFilterChoices.userSort = sort
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
            neighborhoodContainer.removeAllViews()
            if (list != null && list.size > 1) {
                neighborhoodLabel.visibility = View.VISIBLE
                if (list.size > 4) {
                    neighborhoodMoreLessView.visibility = View.VISIBLE
                }

                for (i in 1..list.size - 1) {
                    val neighborhoodView = LayoutInflater.from(getContext()).inflate(R.layout.section_hotel_neighborhood_row, neighborhoodContainer, false) as HotelsNeighborhoodFilter
                    neighborhoodView.bind(list.get(i), vm)
                    neighborhoodView.subscribeOnClick(neighborhoodView.checkObserver)
                    neighborhoodContainer.addView(neighborhoodView)
                }

                setupNeighBourhoodView()
            } else {
                neighborhoodLabel.visibility = View.GONE
                neighborhoodMoreLessView.visibility = View.GONE
            }
        }

        neighborhoodMoreLessView.subscribeOnClick(vm.neighborhoodMoreLessObservable)

        vm.sortContainerObservable.subscribe { showSort ->
            sortContainer.visibility = if (showSort) View.VISIBLE else View.GONE
        }

        vm.neighborhoodExpandObservable.subscribe { isSectionExpanded ->
            if (isSectionExpanded) {
                AnimUtils.rotate(neighborhoodMoreLessIcon)
                neighborhoodMoreLessLabel.text = resources.getString(R.string.show_less)

                for (i in 3..neighborhoodContainer.childCount - 1) {
                    val v = neighborhoodContainer.getChildAt(i)
                    if (v is HotelsNeighborhoodFilter) {
                        v.visibility = View.VISIBLE
                    }
                }

                val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
                resizeAnimator.addViewSpec(neighborhoodContainer, rowHeight * neighborhoodContainer.childCount)
                resizeAnimator.start()

            } else {
                setupNeighBourhoodView()
            }
        }


        vm.amenityOptionsObservable.subscribe { map ->
            val amenityMap: Map<FilterAmenity, Int> = FilterAmenity.amenityFilterToShow(map)
            vm.amenityMapObservable.onNext(amenityMap)

        }

        vm.amenityMapObservable.subscribe { amenityMap ->
            if (!amenityMap.isEmpty()) {
                amenityLabel.visibility = View.VISIBLE
                FilterAmenity.addAmenityFilters(amenityContainer, amenityMap, vm)
            }
        }

    }

    private fun setupNeighBourhoodView() {
        AnimUtils.reverseRotate(neighborhoodMoreLessIcon)
        neighborhoodMoreLessLabel.text = resources.getString(R.string.show_more)

        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.addViewSpec(neighborhoodContainer, rowHeight * 3)
        resizeAnimator.start()

        for (i in 3..neighborhoodContainer.childCount - 1) {
            val v = neighborhoodContainer.getChildAt(i)
            if (v is HotelsNeighborhoodFilter) {
                v.visibility = View.GONE
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_filter, this)

        if (PointOfSale.getPointOfSale().supportsVipAccess()) {
            filterVipContainer.visibility = View.VISIBLE
            filterVipBadge.visibility = View.VISIBLE
        }

        if (shouldShowCircleForRatings()) {
            setCircleDrawableForRatingBtnBackground()
        }

        dynamicFeedbackWidget.hideDynamicFeedback()

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, filterContainer, color)
            addView(statusBar)
        }

        toolbar.inflateMenu(R.menu.cars_lx_filter_menu)
        toolbar.title = resources.getString(R.string.Sort_and_Filter)
        toolbar.setTitleTextAppearance(context, R.style.CarsToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))

        toolbar.menu.findItem(R.id.apply_check).setActionView(doneButton)

        filterContainer.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val scrollY = filterContainer.scrollY
                val ratio = (scrollY).toFloat() / 100
                toolbarDropshadow.alpha = ratio
            }
        })

        resetStars()
    }

    fun resetStars() {
        starSelection(filterStarOne, ratingOneBackground, 1)
        starSelection(filterStarTwo, ratingTwoBackground, 2)
        starSelection(filterStarThree, ratingThreeBackground, 3)
        starSelection(filterStarFour, ratingFourBackground, 4)
        starSelection(filterStarFive, ratingFiveBackground, 5)
    }

    fun starSelection(star: ImageButton, background: View, value: Int) {
        clearHotelNameFocus()
        if (value < 0) {
            star.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            background.setBackgroundColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
        } else {
            star.setColorFilter(ContextCompat.getColor(context, R.color.hotels_primary_color))
            background.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }

    private fun clearHotelNameFocus() {
        filterHotelName.clearFocus()
        com.mobiata.android.util.Ui.hideKeyboard(this)
    }

    private fun setCircleDrawableForRatingBtnBackground() {
        filterStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_one_circle));
        filterStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_two_circle));
        filterStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_three_circle));
        filterStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_four_circle));
        filterStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_five_circle));
    }
}
