package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.BaseTravelerPickerViewModel
import com.expedia.vm.TravelerPickerViewModel
import com.squareup.phrase.Phrase

class TravelerPickerView(context: Context, attrs: AttributeSet) : BaseTravelerPickerView(context, attrs) {
    override fun getViewModel(): BaseTravelerPickerViewModel {
        return viewmodel
    }

    val adultText: TextView by bindView(R.id.adult)
    val childText: TextView by bindView(R.id.children)
    val childAgeLabel: TextView by bindView(R.id.child_age_label)

    val child1: Spinner by bindView(R.id.child_spinner_1)
    val child2: Spinner by bindView(R.id.child_spinner_2)
    val child3: Spinner by bindView(R.id.child_spinner_3)
    val child4: Spinner by bindView(R.id.child_spinner_4)
    val infantError: TextView by bindView(R.id.error_message_infants)
    val infantPreferenceSeatingSpinner: Spinner by bindView(R.id.infant_preference_seating)
    val infantSeatPreferenceOptions = listOf(resources.getString(R.string.in_lap), resources.getString(R.string.in_seat))
    val childSpinners by lazy {
        listOf(child1, child2, child3, child4)
    }

    val adultPlus: ImageButton by bindView(R.id.adults_plus)
    val adultMinus: ImageButton by bindView(R.id.adults_minus)
    val childPlus: ImageButton by bindView(R.id.children_plus)
    val childMinus: ImageButton by bindView(R.id.children_minus)

    val childBottomContainer: View by bindView(R.id.children_ages_bottom_container)


    val DEFAULT_CHILD_AGE = 10
    val enabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_enabled_color)
    val disabledColor = ContextCompat.getColor(context, R.color.hotel_guest_selector_disabled_color)


    var viewmodel: TravelerPickerViewModel by notNullAndObservable { vm ->
        vm.showInfantErrorMessage.subscribeTextAndVisibility(infantError)
        adultPlus.subscribeOnClick(vm.incrementAdultsObserver)
        adultMinus.subscribeOnClick(vm.decrementAdultsObserver)
        childPlus.subscribeOnClick(vm.incrementChildrenObserver)
        childMinus.subscribeOnClick(vm.decrementChildrenObserver)
        vm.infantPreferenceSeatingObservable.subscribe { hasInfants ->
            if (vm.showSeatingPreference) {
                infantPreferenceSeatingSpinner.visibility = if (hasInfants) View.VISIBLE else View.GONE
            }
        }
        vm.adultTextObservable.subscribeText(adultText)
        vm.childTextObservable.subscribeText(childText)

        vm.adultPlusObservable.subscribe {
            adultPlus.isEnabled = it
            adultPlus.setImageButtonColorFilter(it)
        }
        vm.adultMinusObservable.subscribe {
            adultMinus.isEnabled = it
            adultMinus.setImageButtonColorFilter(it)
        }
        vm.childPlusObservable.subscribe {
            childPlus.isEnabled = it
            childPlus.setImageButtonColorFilter(it)
        }
        vm.childMinusObservable.subscribe {
            childMinus.isEnabled = it
            childMinus.setImageButtonColorFilter(it)
        }
        vm.adultTravelerCountChangeObservable.subscribe {
            adultPlus.announceForAccessibility(adultText.text)
        }
        vm.childTravelerCountChangeObservable.subscribe {
            childPlus.announceForAccessibility(childText.text)
        }
        vm.infantInSeatObservable.filter { it }.subscribe {
            infantPreferenceSeatingSpinner.setSelection(1)
        }

        for (i in childSpinners.indices) {
            val spinner = childSpinners[i]
            spinner.adapter = ChildAgeSpinnerAdapter()
            spinner.setSelection(DEFAULT_CHILD_AGE)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    vm.childAgeSelectedObserver.onNext(Pair(i, position))
                    spinner.setAccessibilityHoverFocus()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        infantPreferenceSeatingSpinner.adapter = InfantSeatPreferenceAdapter(context, infantSeatPreferenceOptions)
        infantPreferenceSeatingSpinner.setSelection(0)
        infantPreferenceSeatingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView = parent?.selectedView
                if (textView != null) {
                    val selectedText = textView as android.widget.TextView
                    selectedText.text = resources.getString(R.string.infants_under_two_TEMPLATE, infantSeatPreferenceOptions[position])
                }
                vm.isInfantInLapObservable.onNext(position != 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        vm.travelerParamsObservable.subscribe { travelers ->
            if (travelers.childrenAges.size == 0) {
                childAgeLabel.visibility = View.GONE
            } else {
                childAgeLabel.visibility = View.VISIBLE
                childBottomContainer.visibility = if (travelers.childrenAges.size > 2) View.VISIBLE else View.GONE
            }
            for (i in childSpinners.indices) {
                val spinner = childSpinners[i]
                val selectedListener = spinner.onItemSelectedListener
                spinner.onItemSelectedListener = null
                if (i >= travelers.childrenAges.size) {
                    spinner.visibility = View.INVISIBLE
                    spinner.setSelection(DEFAULT_CHILD_AGE)
                } else {
                    spinner.setSelection(travelers.childrenAges[i])
                    val selectedText = StrUtils.getChildTravelerAgeText(resources, spinner.selectedItem as Int)
                    spinner.contentDescription = Phrase.from(context, R.string.search_child_age_drop_down_cont_desc_TEMPLATE).put("childnumber", i + 1).put("currentselection", selectedText).format().toString()
                    spinner.visibility = View.VISIBLE
                }
                spinner.onItemSelectedListener = selectedListener
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_traveler_picker, this)
        adultMinus.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        adultMinus.viewTreeObserver.removeOnPreDrawListener(this)
                        adultMinus.setAccessibilityHoverFocus()
                        return true
                    }
                }
        )
    }

    fun ImageButton.setImageButtonColorFilter(enabled: Boolean) {
        if (enabled)
            this.setColorFilter(enabledColor, PorterDuff.Mode.SRC_IN)
        else
            this.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
    }
}
