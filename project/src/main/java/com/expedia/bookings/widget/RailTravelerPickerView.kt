package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.TravelerCountSelector
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BaseTravelerPickerViewModel
import com.expedia.vm.RailTravelerPickerViewModel
import com.squareup.phrase.Phrase
import rx.Observer

class RailTravelerPickerView(context: Context, attrs: AttributeSet) : BaseTravelerPickerView(context, attrs) {
    val DEFAULT_CHILD_AGE = 10
    val DEFAULT_YOUTH_AGE = 16
    val DEFAULT_SENIOR_AGE = 60

    val child1: Spinner by bindView(R.id.child_spinner_1)
    val child2: Spinner by bindView(R.id.child_spinner_2)
    val child3: Spinner by bindView(R.id.child_spinner_3)
    val child4: Spinner by bindView(R.id.child_spinner_4)
    val child5: Spinner by bindView(R.id.child_spinner_5)
    val child6: Spinner by bindView(R.id.child_spinner_6)
    val child7: Spinner by bindView(R.id.child_spinner_7)
    val child8: Spinner by bindView(R.id.child_spinner_8)
    val youth1: Spinner by bindView(R.id.youth_spinner_1)
    val youth2: Spinner by bindView(R.id.youth_spinner_2)
    val youth3: Spinner by bindView(R.id.youth_spinner_3)
    val youth4: Spinner by bindView(R.id.youth_spinner_4)
    val youth5: Spinner by bindView(R.id.youth_spinner_5)
    val youth6: Spinner by bindView(R.id.youth_spinner_6)
    val youth7: Spinner by bindView(R.id.youth_spinner_7)
    val youth8: Spinner by bindView(R.id.youth_spinner_8)
    val senior1: Spinner by bindView(R.id.senior_spinner_1)
    val senior2: Spinner by bindView(R.id.senior_spinner_2)
    val senior3: Spinner by bindView(R.id.senior_spinner_3)
    val senior4: Spinner by bindView(R.id.senior_spinner_4)
    val senior5: Spinner by bindView(R.id.senior_spinner_5)
    val senior6: Spinner by bindView(R.id.senior_spinner_6)
    val senior7: Spinner by bindView(R.id.senior_spinner_7)
    val senior8: Spinner by bindView(R.id.senior_spinner_8)

    val childSpinners by lazy {
        listOf(child1, child2, child3, child4, child5, child6, child7, child8)
    }

    val youthSpinners by lazy {
        listOf(youth1, youth2, youth3, youth4, youth5, youth6, youth7, youth8)
    }

    val seniorSpinners by lazy {
        listOf(senior1, senior2, senior3, senior4, senior5, senior6, senior7, senior8)
    }

    val adultCountSelector: TravelerCountSelector by bindView(R.id.adult_count_selector)
    val childCountSelector: TravelerCountSelector by bindView(R.id.child_count_selector)
    val youthCountSelector: TravelerCountSelector by bindView(R.id.youth_count_selector)
    val seniorCountSelector: TravelerCountSelector by bindView(R.id.senior_count_selector)

    val childBottomContainer: View by bindView(R.id.children_ages_bottom_container)
    val childBottomContainer1: View by bindView(R.id.children_ages_bottom_container1)
    val childBottomContainer2: View by bindView(R.id.children_ages_bottom_container2)
    val childBottomContainer3: View by bindView(R.id.children_ages_bottom_container3)
    val youthBottomContainer: View by bindView(R.id.youth_ages_bottom_container)
    val youthBottomContainer1: View by bindView(R.id.youth_ages_bottom_container1)
    val youthBottomContainer2: View by bindView(R.id.youth_ages_bottom_container2)
    val youthBottomContainer3: View by bindView(R.id.youth_ages_bottom_container3)
    val seniorBottomContainer: View by bindView(R.id.senior_ages_bottom_container)
    val seniorBottomContainer1: View by bindView(R.id.senior_ages_bottom_container1)
    val seniorBottomContainer2: View by bindView(R.id.senior_ages_bottom_container2)
    val seniorBottomContainer3: View by bindView(R.id.senior_ages_bottom_container3)



    var viewModel : RailTravelerPickerViewModel by notNullAndObservable { vm ->
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
            adultCountSelector.enablePuls(it)
        }
        vm.adultMinusObservable.subscribe {
            adultCountSelector.enableMinus(it)
        }
        vm.childPlusObservable.subscribe {
            childCountSelector.enablePuls(it)
        }
        vm.childMinusObservable.subscribe {
            childCountSelector.enableMinus(it)
        }
        vm.youthPlusObservable.subscribe {
            youthCountSelector.enablePuls(it)
        }
        vm.youthMinusObservable.subscribe {
            youthCountSelector.enableMinus(it)
        }
        vm.seniorPlusObservable.subscribe {
            seniorCountSelector.enablePuls(it)
        }
        vm.seniorMinusObservable.subscribe {
            seniorCountSelector.enableMinus(it)
        }

        wireUpSpinners(childSpinners, RailChildAgeAgeSpinnerAdapter(), DEFAULT_CHILD_AGE, vm.childAgeSelectedObserver)
        wireUpSpinners(youthSpinners, RailYouthAgeSpinnerAdapter(), DEFAULT_YOUTH_AGE, vm.youthAgeSelectedObserver)
        wireUpSpinners(seniorSpinners, RailSeniorAgeSpinnerAdapter(), DEFAULT_SENIOR_AGE, vm.seniorAgeSelectedObserver)

        vm.travelerParamsObservable.subscribe { travelers ->

            if (travelers.childrenAges.size == 0) {
                childBottomContainer.visibility = View.GONE
            } else {
                childBottomContainer.visibility = if (travelers.childrenAges.size > 0) View.VISIBLE else View.GONE
                childBottomContainer1.visibility = if (travelers.childrenAges.size > 2) View.VISIBLE else View.GONE
                childBottomContainer2.visibility = if (travelers.childrenAges.size > 4) View.VISIBLE else View.GONE
                childBottomContainer3.visibility = if (travelers.childrenAges.size > 6) View.VISIBLE else View.GONE
            }

            if (travelers.youthAges.size == 0) {
                youthBottomContainer.visibility = View.GONE
            } else {
                youthBottomContainer.visibility = if (travelers.youthAges.size > 0) View.VISIBLE else View.GONE
                youthBottomContainer1.visibility = if (travelers.youthAges.size > 2) View.VISIBLE else View.GONE
                youthBottomContainer2.visibility = if (travelers.youthAges.size > 4) View.VISIBLE else View.GONE
                youthBottomContainer3.visibility = if (travelers.youthAges.size > 6) View.VISIBLE else View.GONE
            }

            if (travelers.seniorAges.size == 0) {
                seniorBottomContainer.visibility = View.GONE
            } else {
                seniorBottomContainer.visibility = if (travelers.seniorAges.size > 0) View.VISIBLE else View.GONE
                seniorBottomContainer1.visibility = if (travelers.seniorAges.size > 2) View.VISIBLE else View.GONE
                seniorBottomContainer2.visibility = if (travelers.seniorAges.size > 4) View.VISIBLE else View.GONE
                seniorBottomContainer3.visibility = if (travelers.seniorAges.size > 6) View.VISIBLE else View.GONE
            }

            for (i in childSpinners.indices) {
                val spinner = childSpinners[i]
                if (i >= travelers.childrenAges.size) {
                    spinner.visibility = View.INVISIBLE
                    val selectedListener = spinner.onItemSelectedListener
                    spinner.onItemSelectedListener = null
                    spinner.setSelection(DEFAULT_CHILD_AGE)
                    spinner.onItemSelectedListener = selectedListener
                } else {
                    spinner.visibility = View.VISIBLE
                    spinner.contentDescription = Phrase.from(context, R.string.search_child_drop_down_cont_desc_TEMPLATE).put("childnumber", i + 1).format().toString()
                }
            }

            for (i in youthSpinners.indices) {
                val spinner = youthSpinners[i]
                if (i >= travelers.youthAges.size) {
                    spinner.visibility = View.INVISIBLE
                    val selectedListener = spinner.onItemSelectedListener
                    spinner.onItemSelectedListener = null
                    spinner.setSelection(DEFAULT_YOUTH_AGE)
                    spinner.onItemSelectedListener = selectedListener
                } else {
                    spinner.visibility = View.VISIBLE
                    spinner.contentDescription = Phrase.from(context, R.string.search_youth_drop_down_cont_desc_TEMPLATE).put("youthnumber", i + 1).format().toString()
                }
            }

            for (i in seniorSpinners.indices) {
                val spinner = seniorSpinners[i]
                if (i >= travelers.seniorAges.size) {
                    spinner.visibility = View.INVISIBLE
                    val selectedListener = spinner.onItemSelectedListener
                    spinner.onItemSelectedListener = null
                    spinner.setSelection(DEFAULT_SENIOR_AGE)
                    spinner.onItemSelectedListener = selectedListener
                } else {
                    spinner.visibility = View.VISIBLE
                    spinner.contentDescription = Phrase.from(context, R.string.search_senior_drop_down_cont_desc_TEMPLATE).put("seniornumber", i + 1).format().toString()
                }
            }
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
                    selectedObserver.onNext(Pair(i, position))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }
}
