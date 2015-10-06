package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelFilterViewModel.Sort
import rx.Observer
import java.util.ArrayList
import kotlin.properties.Delegates


public class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterVipBadge: TextView by bindView(R.id.vip_badge)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterVipContainer: View by bindView(R.id.filter_vip_container)
    val filterStarOne: ImageView by bindView(R.id.filter_hotel_star_rating_one)
    val filterStarTwo: ImageView by bindView(R.id.filter_hotel_star_rating_two)
    val filterStarThree: ImageView by bindView(R.id.filter_hotel_star_rating_three)
    val filterStarFour: ImageView by bindView(R.id.filter_hotel_star_rating_four)
    val filterStarFive: ImageView by bindView(R.id.filter_hotel_star_rating_five)
    val ratingOneBackground: View by bindView(R.id.rating_one_background)
    val ratingTwoBackground: View by bindView(R.id.rating_two_background)
    val ratingThreeBackground: View by bindView(R.id.rating_three_background)
    val ratingFourBackground: View by bindView(R.id.rating_four_background)
    val ratingFiveBackground: View by bindView(R.id.rating_five_background)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: TextView by bindView(R.id.filter_hotel_name_edit_text)
    val clearNameButton: ImageView by bindView(R.id.clear_search_button)
    val dynamicFeedbackWidget : DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton : TextView by bindView(R.id.dynamic_feedback_clear_button)
    val filterContainer: ViewGroup by bindView(R.id.filter_container)
    val doneButton:Button by lazy {
        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val icon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        toolbar.getMenu().findItem(R.id.apply_check).setActionView(button)
        button
    }
    val toolbarDropshadow: View by bindView(R.id.toolbar_dropshadow)
    val priceRangeBar : ViewGroup by bindView(R.id.price_range_bar)
    val neighborhoodLabel: TextView by bindView(R.id.neighborhood_label)
    val neighborhoodContainer: LinearLayout by bindView(R.id.neighborhoods)
    val neighborhoodMoreLessLabel : TextView by bindView(R.id.show_more_less_text)
    val neighborhoodMoreLessIcon : ImageButton by bindView(R.id.show_more_less_icon)
    val neighborhoodMoreLessView : RelativeLayout by bindView(R.id.collapsed_container)

    val amenityLabel: TextView by bindView(R.id.amenity_label)
    val amenityContainer: GridLayout by bindView(R.id.amenities_container)

    var filterObserver: Observer<List<Hotel>> by Delegates.notNull()

    fun subscribe(filterObserver: Observer<List<Hotel>>) {
        this.filterObserver = filterObserver
    }

    val sortByObserver: Observer<Boolean> = endlessObserver{ isCurrentLocationSearch ->
        var sortOptions: ArrayList<String> = getResources().getStringArray(R.array.sort_options_material_hotels).toArrayList()

        if (isCurrentLocationSearch) {
            sortOptions.add(getContext().getString(R.string.distance))
        }

        val adapter = ArrayAdapter(getContext(), R.layout.spinner_sort_item, sortOptions)
        adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item)

        sortByButtonGroup.setAdapter(adapter)
    }

    var viewmodel: HotelFilterViewModel by notNullAndObservable { vm ->
        val min = 20
        val max = 1000
        val hotelPriceRange : RangeSeekBar<Int> = RangeSeekBar(min, max, context)
        hotelPriceRange.setOnRangeSeekBarChangeListener(object: RangeSeekBar.OnRangeSeekBarChangeListener<Int> {
            override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>, minValue: Int, maxValue: Int) {
                System.out.println("User selected new range values: MIN=" + minValue + ", MAX=" + maxValue)
            }
        })
        priceRangeBar.addView(hotelPriceRange);

        doneButton.subscribeOnClick(vm.doneObservable)
        filterVipContainer.setOnClickListener {
            filterHotelVip.setChecked(!filterHotelVip.isChecked())
            vm.vipFilteredObserver.onNext(filterHotelVip.isChecked())
        }

        ratingOneBackground.subscribeOnClick(vm.oneStarFilterObserver)
        ratingTwoBackground.subscribeOnClick(vm.twoStarFilterObserver)
        ratingThreeBackground.subscribeOnClick(vm.threeStarFilterObserver)
        ratingFourBackground.subscribeOnClick(vm.fourStarFilterObserver)
        ratingFiveBackground.subscribeOnClick(vm.fiveStarFilterObserver)

        dynamicFeedbackClearButton.subscribeOnClick(vm.clearObservable)

        vm.filterObservable.subscribe(filterObserver)

        vm.finishClear.subscribe {
            //check if filterHotelName is empty to avoid calling handleFiltering
            if (filterHotelName.text.length() > 0) filterHotelName.text = ""
            resetStars()

            filterHotelVip.setChecked(false)

            for (i in 0.. amenityContainer.childCount - 1) {
                val v = amenityContainer.getChildAt(i)
                if (v is HotelAmenityFilter && v.isSelected) {
                    v.isSelected = false
                    v.changeColor(getResources().getColor(R.color.hotelsv2_checkout_text_color))
                }
            }

            for (i in 0 .. neighborhoodContainer.getChildCount() -1){
                val v = neighborhoodContainer.getChildAt(i)
                if (v is HotelsNeighborhoodFilter) {
                    v.neighborhoodCheckBox.setChecked(false)
                }
            }

            dynamicFeedbackWidget.hideDynamicFeedback()
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
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            dynamicFeedbackWidget.showDynamicFeedback()
            dynamicFeedbackWidget.setDynamicCounterText(it)
        }

        filterHotelName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearNameButton.setVisibility(if (Strings.isEmpty(s)) View.GONE else View.VISIBLE)
                vm.filterHotelNameObserver.onNext(s)
            }
        })

        filterHotelName.setOnFocusChangeListener { view, isFocus ->
            if (!isFocus) {
                com.mobiata.android.util.Ui.hideKeyboard(this)
            }
        }

        clearNameButton.setOnClickListener { view ->
            filterHotelName.setText(null)
        }

        sortByButtonGroup.setOnItemSelectedListener(object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val sort = Sort.values()[position]
                vm.userFilterChoices.userSort = sort
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        })

        vm.neighborhoodListObservable.subscribe { list ->
            neighborhoodContainer.removeAllViews()
            if (list != null && list.size() > 1) {
                neighborhoodLabel.visibility = View.VISIBLE
                if (list.size() > 3) {
                    neighborhoodMoreLessView.visibility = View.VISIBLE
                }

                for (i in 1..list.size() - 1) {
                    val neighborhoodView = LayoutInflater.from(getContext()).inflate(R.layout.section_hotel_neighborhood_row, null) as HotelsNeighborhoodFilter
                    neighborhoodView.bind(list.get(i), vm)
                    neighborhoodView.subscribeOnClick(neighborhoodView.checkObserver)
                    neighborhoodContainer.addView(neighborhoodView)
                    if (i > 3 && i < list.size()){
                        neighborhoodView.visibility = View.GONE
                    }
                }
            } else {
                neighborhoodLabel.visibility = View.GONE
                neighborhoodMoreLessView.visibility = View.GONE
            }
        }

        neighborhoodMoreLessView.subscribeOnClick(vm.neighborhoodMoreLessObserverable)

        vm.neighborhoodExpandObserable.subscribe { isSectionExpanded ->
            if (isSectionExpanded) {
                AnimUtils.rotate(neighborhoodMoreLessIcon)
                neighborhoodMoreLessLabel.text = getContext().getString(R.string.show_less)
                for (i in 3..neighborhoodContainer.getChildCount() - 1) {
                    val v = neighborhoodContainer.getChildAt(i)
                    if (v is HotelsNeighborhoodFilter) {
                        v.visibility = View.VISIBLE
                    }
                }

            } else {
                AnimUtils.reverseRotate(neighborhoodMoreLessIcon)
                neighborhoodMoreLessLabel.text = getContext().getString(R.string.show_more)
                for (i in 3 .. neighborhoodContainer.getChildCount() -1){
                    val v = neighborhoodContainer.getChildAt(i)
                    if (v is HotelsNeighborhoodFilter) {
                        v.visibility = View.GONE
                    }
                }
            }
        }


        vm.amenityOptionsObservable.subscribe { map ->
            val amenityMap: Map<FilterAmenity, Int> = FilterAmenity.amenityFilterToShow(map)
            vm.amenityMapObservable.onNext(amenityMap)

        }

        vm.amenityMapObservable.subscribe { amenityMap ->
            if (!amenityMap.isEmpty()){
                amenityLabel.visibility = View.VISIBLE
                FilterAmenity.addAmenityFilters(amenityContainer, amenityMap, vm)
            }
        }

    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_filter, this)

        if(PointOfSale.getPointOfSale().supportsVipAccess()){
            filterVipContainer.visibility = View.VISIBLE
            filterVipBadge.visibility = View.VISIBLE
        }

        dynamicFeedbackWidget.hideDynamicFeedback()

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = getContext().getResources().getColor(R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, filterContainer, color)
            addView(statusBar)
        }

        toolbar.inflateMenu(R.menu.cars_lx_filter_menu)
        toolbar.setTitle(getResources().getString(R.string.Sort_and_Filter))
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(getResources().getColor(R.color.cars_actionbar_text_color))

        doneButton.setText(R.string.done)
        doneButton.setTextColor(getResources().getColor(R.color.cars_actionbar_text_color))

        filterContainer.getViewTreeObserver().addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val scrollY = filterContainer.getScrollY()
                val ratio = (scrollY).toFloat() / 100
                toolbarDropshadow.setAlpha(ratio)
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

    fun starSelection(star: ImageView, background : View, value : Int) {
        var starDrawable = star.drawable
        if (value < 0) {
            DrawableCompat.setTint(starDrawable, getResources().getColor(android.R.color.white))
            background.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        } else {
            DrawableCompat.setTint(starDrawable, getResources().getColor(R.color.hotels_primary_color))
            background.setBackgroundColor(getResources().getColor(android.R.color.white))
        }
    }
}
