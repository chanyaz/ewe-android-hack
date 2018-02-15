package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseTravelerPickerView
import com.expedia.bookings.widget.shared.TravelerCountSelector
import com.expedia.bookings.widget.shared.TravelerPickerSpinnersContainer
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseTravelerPickerViewModel
import com.expedia.vm.RailTravelerPickerViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class RailTravelerPickerView(context: Context, attrs: AttributeSet) : BaseTravelerPickerView(context, attrs) {
    val DEFAULT_CHILD_AGE = 10
    val DEFAULT_YOUTH_AGE = 16
    val DEFAULT_SENIOR_AGE = 60
    val CHILDREN_NUMBER = "childnumber"
    val YOUTH_NUMBER = "youthnumber"
    val SENIOR_NUMBER = "seniornumber"

    val adultCountSelector: TravelerCountSelector by bindView(R.id.adult_count_selector)
    val childCountSelector: TravelerCountSelector by bindView(R.id.child_count_selector)
    val youthCountSelector: TravelerCountSelector by bindView(R.id.youth_count_selector)
    val seniorCountSelector: TravelerCountSelector by bindView(R.id.senior_count_selector)

    val childTravelerPickerSpinnersContainer: TravelerPickerSpinnersContainer by bindView(R.id.child_spinners_container)
    val youthTravelerPickerSpinnersContainer: TravelerPickerSpinnersContainer by bindView(R.id.youth_spinners_container)
    val seniorTravelerPickerSpinnersContainer: TravelerPickerSpinnersContainer by bindView(R.id.senior_spinners_container)

    var viewModel: RailTravelerPickerViewModel by notNullAndObservable { vm ->
        adultCountSelector.minusClickedSubject.subscribe(vm.decrementAdultsObserver)
        adultCountSelector.plusClickedSubject.subscribe(vm.incrementAdultsObserver)
        childCountSelector.minusClickedSubject.subscribe(vm.decrementChildrenObserver)
        childCountSelector.plusClickedSubject.subscribe(vm.incrementChildrenObserver)
        youthCountSelector.plusClickedSubject.subscribe(vm.incrementYouthObserver)
        youthCountSelector.minusClickedSubject.subscribe(vm.decrementYouthObserver)
        seniorCountSelector.minusClickedSubject.subscribe(vm.decrementSeniorObserver)
        seniorCountSelector.plusClickedSubject.subscribe(vm.incrementSeniorObserver)

        vm.adultTextObservable.subscribeText(adultCountSelector.travelerText)
        vm.childTextObservable.subscribeText(childCountSelector.travelerText)
        vm.youthTextObservable.subscribeText(youthCountSelector.travelerText)
        vm.seniorTextObservable.subscribeText(seniorCountSelector.travelerText)

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
        vm.seniorPlusObservable.subscribe {
            seniorCountSelector.enablePlus(it)
        }
        vm.seniorMinusObservable.subscribe {
            seniorCountSelector.enableMinus(it)
        }

        wireUpSpinners(childTravelerPickerSpinnersContainer.ageSpinners, RailChildAgeSpinnerAdapter(), DEFAULT_CHILD_AGE, vm.childAgeSelectedObserver)
        wireUpSpinners(youthTravelerPickerSpinnersContainer.ageSpinners, RailYouthAgeSpinnerAdapter(), DEFAULT_YOUTH_AGE, vm.youthAgeSelectedObserver)
        wireUpSpinners(seniorTravelerPickerSpinnersContainer.ageSpinners, RailSeniorAgeSpinnerAdapter(), DEFAULT_SENIOR_AGE, vm.seniorAgeSelectedObserver)

        vm.travelerParamsObservable.subscribe { travelers ->
            findBottomContainerVisibility(childTravelerPickerSpinnersContainer, travelers.childrenAges.size)
            findBottomContainerVisibility(youthTravelerPickerSpinnersContainer, travelers.youthAges.size)
            findBottomContainerVisibility(seniorTravelerPickerSpinnersContainer, travelers.seniorAges.size)

            showAgesSpinner(childTravelerPickerSpinnersContainer, DEFAULT_CHILD_AGE, travelers.childrenAges.size, CHILDREN_NUMBER)
            showAgesSpinner(youthTravelerPickerSpinnersContainer, DEFAULT_YOUTH_AGE, travelers.youthAges.size, YOUTH_NUMBER)
            showAgesSpinner(seniorTravelerPickerSpinnersContainer, DEFAULT_SENIOR_AGE, travelers.seniorAges.size, SENIOR_NUMBER)
        }
    }

    init {
        View.inflate(context, R.layout.widget_rail_traveler_picker, this)
    }

    override fun getViewModel(): BaseTravelerPickerViewModel {
        return viewModel
    }

    fun wireUpSpinners(spinners: List<Spinner>, adapter: BaseAdapter, defaultAge: Int,
                       selectedObserver: Observer<Pair<Int, Int>>) {
        for (i in spinners.indices) {
            val spinner = spinners[i]
            spinner.adapter = adapter
            spinner.setSelection(defaultAge)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (defaultAge) {
                        position, DEFAULT_CHILD_AGE -> selectedObserver.onNext(Pair(i, position))
                        else -> selectedObserver.onNext(Pair(i, position + defaultAge))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    fun findBottomContainerVisibility(travelerPickerSpinnersContainer: TravelerPickerSpinnersContainer, numberOfTravelers: Int) {
        if (numberOfTravelers in 1..8) {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer1.visibility = View.VISIBLE
        } else {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer1.visibility = View.GONE
        }
        if (numberOfTravelers in 3..8) {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer2.visibility = View.VISIBLE
        } else {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer2.visibility = View.GONE
        }
        if (numberOfTravelers in 5..8) {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer3.visibility = View.VISIBLE
        } else {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer3.visibility = View.GONE
        }
        if (numberOfTravelers in 7..8) {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer4.visibility = View.VISIBLE
        } else {
            travelerPickerSpinnersContainer.travelerAgesBottomContainer4.visibility = View.GONE
        }
    }

    fun showAgesSpinner(travelerPickerSpinnersContainer: TravelerPickerSpinnersContainer, defaultAge: Int, numberOfTravelers: Int, ageNumber: String) {
        for (i in travelerPickerSpinnersContainer.ageSpinners.indices) {
            val spinner = travelerPickerSpinnersContainer.ageSpinners[i]
            if (i >= numberOfTravelers) {
                spinner.visibility = View.INVISIBLE
                val selectedListener = spinner.onItemSelectedListener
                spinner.onItemSelectedListener = null
                spinner.setSelection(defaultAge)
                spinner.onItemSelectedListener = selectedListener
            } else {
                when (ageNumber) {
                    CHILDREN_NUMBER -> spinner.contentDescription = Phrase.from(context, R.string.search_child_drop_down_cont_desc_TEMPLATE).put("childnumber", i + 1).format().toString()
                    YOUTH_NUMBER -> spinner.contentDescription = Phrase.from(context, R.string.search_youth_drop_down_cont_desc_TEMPLATE).put("youthnumber", i + 1).format().toString()
                    SENIOR_NUMBER -> spinner.contentDescription = Phrase.from(context, R.string.search_senior_drop_down_cont_desc_TEMPLATE).put("seniornumber", i + 1).format().toString()
                }
                spinner.visibility = View.VISIBLE
            }
        }
    }
}
