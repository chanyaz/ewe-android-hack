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
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.HotelFilterViewModel.Sort
import rx.Observer
import java.util.ArrayList
import java.util.Arrays
import kotlin.properties.Delegates


public class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterVipContainer: View by bindView(R.id.filter_vip_container)
    val filterStarOne: RatingBar by bindView(R.id.filter_hotel_star_rating_one)
    val filterStarTwo: RatingBar by bindView(R.id.filter_hotel_star_rating_two)
    val filterStarThree: RatingBar by bindView(R.id.filter_hotel_star_rating_three)
    val filterStarFour: RatingBar by bindView(R.id.filter_hotel_star_rating_four)
    val filterStarFive: RatingBar by bindView(R.id.filter_hotel_star_rating_five)
    val ratingOneBackground: View by bindView(R.id.rating_one_background)
    val ratingTwoBackground: View by bindView(R.id.rating_two_background)
    val ratingThreeBackground: View by bindView(R.id.rating_three_background)
    val ratingFourBackground: View by bindView(R.id.rating_four_background)
    val ratingFiveBackground: View by bindView(R.id.rating_five_background)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: TextView by bindView(R.id.filter_hotel_name_edit_text)
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
    var isSectionExpanded = false

    val amenityLabel: TextView by bindView(R.id.amenity_label)
    val amenityContainer: GridLayout by bindView(R.id.amenities_container)

    var filterObserver: Observer<List<Hotel>> by Delegates.notNull()

    fun subscribe(filterObserver: Observer<List<Hotel>>) {
        this.filterObserver = filterObserver
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
            filterHotelName.setText(null)
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

        vm.updateDynamicFeedbackWidget.subscribe {
            dynamicFeedbackWidget.showDynamicFeedback()
            if (it == 0) dynamicFeedbackWidget.animateDynamicFeedbackWidget()
            dynamicFeedbackWidget.setDynamicCounterText(it)
        }

        filterHotelName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                vm.filterHotelNameObserver.onNext(s)
            }
        })

        filterHotelName.setOnFocusChangeListener { view, isFocus ->
            if (!isFocus) {
                com.mobiata.android.util.Ui.hideKeyboard(this)
            }
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

        neighborhoodMoreLessView.setOnClickListener {
            if (!isSectionExpanded) {
                isSectionExpanded = true
                AnimUtils.rotate(neighborhoodMoreLessIcon)
                neighborhoodMoreLessLabel.text = getContext().getString(R.string.show_less)
                for (i in 3..neighborhoodContainer.getChildCount() - 1) {
                    val v = neighborhoodContainer.getChildAt(i)
                    if (v is HotelsNeighborhoodFilter) {
                        v.visibility = View.VISIBLE
                    }
                }

            } else {
                isSectionExpanded = false
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

        dynamicFeedbackWidget.hideDynamicFeedback()

        val sortOptions = ArrayList<String>()
        sortOptions.addAll(Arrays.asList(*getResources().getStringArray(R.array.sort_options_material_hotels)))
        sortOptions.add(getContext().getString(R.string.distance))

        val adapter = ArrayAdapter(getContext(), R.layout.spinner_sort_item, sortOptions)
        adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item)

        sortByButtonGroup.setAdapter(adapter)

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

    fun starSelection(star: RatingBar, background : View, value : Int) {
        if (value < 0) {
            var starDrawable = star.getProgressDrawable()
            DrawableCompat.setTint(starDrawable, getResources().getColor(android.R.color.white))
            background.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        } else {
            star.setRating(value.toFloat())
            star.setNumStars(value)
            var starDrawable = star.getProgressDrawable()
            DrawableCompat.setTint(starDrawable, getResources().getColor(R.color.hotels_primary_color))
            background.setBackgroundColor(getResources().getColor(android.R.color.white))
        }
    }
}
