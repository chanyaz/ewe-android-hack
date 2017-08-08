package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.shared.TravelerCountSelector
import com.expedia.util.*
import com.expedia.vm.BaseTravelerPickerViewModel
import com.expedia.vm.FlightTravelerPickerViewModel
import rx.Observer


class FlightTravelerPickerView(context: Context, attrs: AttributeSet) : BaseTravelerPickerView(context, attrs) {

    override fun getViewModel(): BaseTravelerPickerViewModel {
        return viewmodel
    }

    val adultCountSelector: TravelerCountSelector by bindView(R.id.adult_count_selector)
    val childCountSelector: TravelerCountSelector by bindView(R.id.child_count_selector)
    val youthCountSelector: TravelerCountSelector by bindView(R.id.youth_count_selector)
    val infantCountSelector: TravelerCountSelector by bindView(R.id.infant_count_selector)

    val infantPreferenceRadioGroup: RadioGroup by bindView(R.id.flight_traveler_radio_group)
    val infantInLap: RadioButton by bindView(R.id.inLap)
    val infantInSeat: RadioButton by bindView(R.id.inSeat)

    val infantPreferenceSeatingView: LinearLayout by bindView(R.id.infant_preference_seating)
    val infantError: TextView by bindView(R.id.error_message_infants)

    var viewmodel: FlightTravelerPickerViewModel by notNullAndObservable { vm ->
        vm.showInfantErrorMessage.subscribeTextAndVisibility(infantError)

        adultCountSelector.minusClickedSubject.subscribe(vm.decrementAdultsObserver)
        adultCountSelector.plusClickedSubject.subscribe(vm.incrementAdultsObserver)
        childCountSelector.minusClickedSubject.subscribe(vm.decrementChildrenObserver)
        childCountSelector.plusClickedSubject.subscribe(vm.incrementChildrenObserver)
        youthCountSelector.minusClickedSubject.subscribe(vm.decrementYouthObserver)
        youthCountSelector.plusClickedSubject.subscribe(vm.incrementYouthObserver)
        infantCountSelector.minusClickedSubject.subscribe(vm.decrementInfantObserver)
        infantCountSelector.plusClickedSubject.subscribe(vm.incrementInfantObserver)

        vm.infantPreferenceSeatingObservable.subscribe { hasInfants ->
            if (vm.showSeatingPreference && hasInfants) {
                if (infantPreferenceSeatingView.visibility != View.VISIBLE) {
                    infantPreferenceSeatingView.animate()
                            .alpha(1f)
                            .setDuration(500)
                    infantPreferenceSeatingView.visibility = View.VISIBLE
                }
            } else {
                if (infantPreferenceSeatingView.visibility != View.GONE) {
                    infantPreferenceSeatingView.animate()
                            .alpha(0f)
                            .setDuration(500)
                    infantPreferenceSeatingView.visibility = View.GONE
                }
            }
        }

        vm.adultTextObservable.subscribeText(adultCountSelector.travelerText)
        vm.childTextObservable.subscribeText(childCountSelector.travelerText)
        vm.youthTextObservable.subscribeText(youthCountSelector.travelerText)
        vm.infantTextObservable.subscribeText(infantCountSelector.travelerText)

        vm.adultPlusObservable.subscribe {
            adultCountSelector.enablePlus(it)
        }
        vm.adultMinusObservable.subscribe {
            adultCountSelector.enableMinus(it)
        }
        vm.childPlusObservable.subscribe {
            childCountSelector.enablePlus(it)
        }
        vm.childMinusObservable.subscribe {
            childCountSelector.enableMinus(it)
        }
        vm.youthPlusObservable.subscribe {
            youthCountSelector.enablePlus(it)
        }
        vm.youthMinusObservable.subscribe {
            youthCountSelector.enableMinus(it)
        }
        vm.infantPlusObservable.subscribe {
            infantCountSelector.enablePlus(it)
        }
        vm.infantMinusObservable.subscribe {
            infantCountSelector.enableMinus(it)
        }

        vm.adultTravelerCountChangeObservable.subscribe {
            adultCountSelector.travelerPlus.announceForAccessibility(adultCountSelector.travelerText.text)
        }
        vm.youthTravelerCountChangeObservable.subscribe {
            youthCountSelector.travelerPlus.announceForAccessibility(youthCountSelector.travelerText.text)
        }
        vm.childTravelerCountChangeObservable.subscribe {
            childCountSelector.travelerPlus.announceForAccessibility(childCountSelector.travelerText.text)
        }
        vm.infantTravelerCountChangeObservable.subscribe {
            infantCountSelector.travelerPlus.announceForAccessibility(infantCountSelector.travelerText.text)
        }

        val changeInfantPreferenceSitting: Observer<Int> = endlessObserver { checkedId ->
            when (checkedId) {
                infantInLap.id -> {
                    vm.isInfantInLapObservable.onNext(true)
                }
                infantInSeat.id -> {
                    vm.isInfantInLapObservable.onNext(false)
                }
            }
        }

        infantPreferenceRadioGroup.subscribeOnCheckChanged(changeInfantPreferenceSitting)

        vm.infantInSeatObservable.subscribe { it ->
            if (it) {
                infantInSeat.setChecked(true)
            } else {
                infantInLap.setChecked(true)
            }
        }

    }

    init {
        View.inflate(context, R.layout.widget_flight_traveler_picker, this)
        adultCountSelector.travelerMinus.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        infantPreferenceSeatingView.visibility = View.GONE
                        adultCountSelector.travelerMinus.viewTreeObserver.removeOnPreDrawListener(this)
                        adultCountSelector.travelerMinus.setAccessibilityHoverFocus()
                        return true
                    }
                }
        )
    }


}
