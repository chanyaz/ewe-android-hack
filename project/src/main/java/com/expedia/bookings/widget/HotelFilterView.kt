package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.AdapterView
import com.expedia.bookings.widget.RangeSeekBar
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnChecked
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelFilterViewModel
import org.joda.time.DateTime
import rx.Observer
import java.util.ArrayList
import java.util.Arrays
import kotlin.properties.Delegates
import com.expedia.vm.HotelFilterViewModel.Sort


public class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(p0: SeekBar?) {
        throw UnsupportedOperationException()
    }

    override fun onStopTrackingTouch(p0: SeekBar) {
        throw UnsupportedOperationException()
    }

    override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
        throw UnsupportedOperationException()
    }

    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterStarOne: Button by bindView(R.id.filter_hotel_star_rating_one)
    val filterStarTwo: Button by bindView(R.id.filter_hotel_star_rating_two)
    val filterStarThree: Button by bindView(R.id.filter_hotel_star_rating_three)
    val filterStarFour: Button by bindView(R.id.filter_hotel_star_rating_four)
    val filterStarFive: Button by bindView(R.id.filter_hotel_star_rating_five)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: TextView by bindView(R.id.filter_hotel_name_edit_text)
    val dynamicFeedbackWidget : DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton : TextView by bindView(R.id.dynamic_feedback_clear_button)
    val filterContainer: ViewGroup by bindView(R.id.filter_container)
    val doneButton:Button by Delegates.lazy {
        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate()
        navIcon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        toolbar.getMenu().findItem(R.id.apply_check).setActionView(button)
        button
    }
    val toolbarDropshadow: View by bindView(R.id.toolbar_dropshadow)
    val background = android.R.attr.selectableItemBackground
    val priceRangeBar : ViewGroup by bindView(R.id.price_range_bar)

    var subject: Observer<List<Hotel>> by Delegates.notNull()

    fun subscribe(subject: Observer<List<Hotel>>) {
        this.subject = subject
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
        filterHotelVip.subscribeOnChecked(vm.vipFilteredObserver)
        filterStarOne.subscribeOnClick(vm.oneStarFilterObserver)
        filterStarTwo.subscribeOnClick(vm.twoStarFilterObserver)
        filterStarThree.subscribeOnClick(vm.threeStarFilterObserver)
        filterStarFour.subscribeOnClick(vm.fourStarFilterObserver)
        filterStarFive.subscribeOnClick(vm.fiveStarFilterObserver)

        dynamicFeedbackClearButton.subscribeOnClick(vm.clearObservable)

        vm.filterObservable.subscribe(subject)

        vm.finishClear.subscribe {
            resetStars()
        }

        vm.hotelStarRatingBar.subscribe {
            val highlightStar = getResources().getColor(android.R.color.darker_gray)
            if (it == 1) {
                filterStarOne.setBackgroundColor(highlightStar)
            } else if (it == 6) {
                filterStarOne.setBackgroundColor(background)
            }
            if (it == 2) {
                filterStarTwo.setBackgroundColor(highlightStar)
            } else if (it == 7) {
                filterStarTwo.setBackgroundColor(background)
            }
            if (it == 3) {
                filterStarThree.setBackgroundColor(highlightStar)
            } else if (it == 8) {
                filterStarThree.setBackgroundColor(background)
            }
            if (it == 4) {
                filterStarFour.setBackgroundColor(highlightStar)
            } else if (it == 9) {
                filterStarFour.setBackgroundColor(background)
            }
            if (it == 5) {
                filterStarFive.setBackgroundColor(highlightStar)
            } else if (it == 10) {
                filterStarFive.setBackgroundColor(background)
            }
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            dynamicFeedbackWidget.showDynamicFeedback()
            dynamicFeedbackWidget.animateDynamicFeedbackWidget()
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
                vm.sortObserver.onNext(sort)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        })

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

    }

    fun resetStars() {
        filterStarOne.setBackgroundColor(background)
        filterStarTwo.setBackgroundColor(background)
        filterStarThree.setBackgroundColor(background)
        filterStarFour.setBackgroundColor(background)
        filterStarFive.setBackgroundColor(background)
    }

}