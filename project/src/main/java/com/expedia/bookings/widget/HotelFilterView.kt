package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import butterknife.InjectView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnChecked
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelFilterViewModel
import rx.Observer
import java.util.ArrayList
import java.util.Arrays
import kotlin.properties.Delegates

public class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val filterDone: Button by bindView(R.id.filter_done)
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

    var subject: Observer<List<Hotel>> by Delegates.notNull()

    fun subscribe(subject: Observer<List<Hotel>>) {
        this.subject = subject
    }
    var viewmodel: HotelFilterViewModel by notNullAndObservable { vm ->
        filterDone.subscribeOnClick(vm.doneObservable)
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
            resetStars()

            val highlightStar = getResources().getColor(android.R.color.darker_gray)
            if (it == 1) {
                filterStarOne.setBackgroundColor(highlightStar)
            } else if (it == 2) {
                filterStarTwo.setBackgroundColor(highlightStar)
            } else if (it == 3) {
                filterStarThree.setBackgroundColor(highlightStar)
            } else if (it == 4) {
                filterStarFour.setBackgroundColor(highlightStar)
            } else if (it == 5) {
                filterStarFive.setBackgroundColor(highlightStar)
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
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_filter, this)

        dynamicFeedbackWidget.hideDynamicFeedback()

        val sortOptions = ArrayList<String>()
        sortOptions.addAll(Arrays.asList(*getResources().getStringArray(R.array.sort_options_hotels)))
        sortOptions.add(getContext().getString(R.string.distance))

        val adapter = ArrayAdapter(getContext(), R.layout.spinner_sort_item, sortOptions)
        adapter.setDropDownViewResource(R.layout.spinner_sort_dropdown_item)

        sortByButtonGroup.setAdapter(adapter)
    }

    fun resetStars() {
        val background = android.R.attr.selectableItemBackground
        filterStarOne.setBackgroundColor(background)
        filterStarTwo.setBackgroundColor(background)
        filterStarThree.setBackgroundColor(background)
        filterStarFour.setBackgroundColor(background)
        filterStarFive.setBackgroundColor(background)
    }

}