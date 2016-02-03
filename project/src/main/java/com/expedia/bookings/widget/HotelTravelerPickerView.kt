package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.HotelTravelerPickerViewModel

public class HotelTravelerPickerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val adultText: TextView by bindView(R.id.adult)
    val childText: TextView by bindView(R.id.children)
    val childAgeLabel: TextView by bindView(R.id.child_age_label)

    val spinner1: Spinner by bindView(R.id.child_spinner_1)
    val spinner2: Spinner by bindView(R.id.child_spinner_2)
    val spinner3: Spinner by bindView(R.id.child_spinner_3)
    val spinner4: Spinner by bindView(R.id.child_spinner_4)
    val infantPreferenceSeatingView: LinearLayout by bindView(R.id.infant_preference_seating_layout)
    val infantPreferenceSeatingSpinner: Spinner by bindView(R.id.infant_preference_seating)
    val infantSeatPreferenceOptions = listOf(resources.getString(R.string.in_seat), resources.getString(R.string.in_lap))
    val childSpinners by lazy {
        listOf(spinner1, spinner2, spinner3, spinner4)
    }

    val adultPlus: ImageButton by bindView(R.id.adults_plus)
    val adultMinus: ImageButton by bindView(R.id.adults_minus)
    val childPlus: ImageButton by bindView(R.id.children_plus)
    val childMinus: ImageButton by bindView(R.id.children_minus)

    val DEFAULT_CHILD_AGE = 10
    val enabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_enabled_color)
    val disabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_disabled_color)

    var viewmodel: HotelTravelerPickerViewModel by notNullAndObservable { vm ->
        adultPlus.subscribeOnClick(vm.incrementAdultsObserver)
        adultMinus.subscribeOnClick(vm.decrementAdultsObserver)

        childPlus.subscribeOnClick(vm.incrementChildrenObserver)
        childMinus.subscribeOnClick(vm.decrementChildrenObserver)
        if (vm.showSeatingPreference) {
            vm.infantPreferenceSeatingObservable.subscribe { hasInfants ->
                infantPreferenceSeatingView.visibility = if (hasInfants) View.VISIBLE else View.GONE
            }
        }
        vm.adultTextObservable.subscribeText(adultText)
        vm.childTextObservable.subscribeText(childText)

        vm.adultPlusObservable.subscribe {
            adultPlus.setEnabled(it)
            adultPlus.setImageButtonColorFilter(it)
        }
        vm.adultMinusObservable.subscribe {
            adultMinus.setEnabled(it)
            adultMinus.setImageButtonColorFilter(it)
        }
        vm.childPlusObservable.subscribe {
            childPlus.setEnabled(it)
            childPlus.setImageButtonColorFilter(it)
        }
        vm.childMinusObservable.subscribe {
            childMinus.setEnabled(it)
            childMinus.setImageButtonColorFilter(it)
        }

        for (i in childSpinners.indices) {
            val spinner = childSpinners[i]
            spinner.adapter = ChildAgeSpinnerAdapter()
            spinner.setSelection(DEFAULT_CHILD_AGE)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    vm.childAgeSelectedObserver.onNext(Pair(i, position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        infantPreferenceSeatingSpinner.adapter = InfantSeatPreferenceAdapter(context, infantSeatPreferenceOptions)
        infantPreferenceSeatingSpinner.setSelection(0)
        infantPreferenceSeatingSpinner.background.setColorFilter(resources.getColor(R.color.itin_white_text), PorterDuff.Mode.SRC_ATOP);
        infantPreferenceSeatingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedText = parent?.selectedView as android.widget.TextView
                selectedText.text = resources.getString(R.string.infants_under_two_TEMPLATE, infantSeatPreferenceOptions[position])
                selectedText.setTextColor(Color.WHITE)
                //TODO with the selected infant's seat preference

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


        vm.travelerParamsObservable.subscribe { travelers ->
            if (travelers.children.size == 0) {
                childAgeLabel.setVisibility(View.GONE)
            } else {
                childAgeLabel.setVisibility(View.VISIBLE)
            }
            for (i in childSpinners.indices) {
                val spinner = childSpinners[i]
                if (i >= travelers.children.size) {
                    spinner.setVisibility(View.INVISIBLE)
                } else {
                    spinner.setVisibility(View.VISIBLE)
                }
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_traveler_picker, this)
    }

    fun ImageButton.setImageButtonColorFilter(enabled: Boolean) {
        if (enabled)
            this.setColorFilter(enabledColor, PorterDuff.Mode.SRC_IN)
        else
            this.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
    }
}
