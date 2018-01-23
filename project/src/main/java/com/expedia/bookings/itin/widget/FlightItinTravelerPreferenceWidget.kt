package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinTravelerPreferenceViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class FlightItinTravelerPreferenceWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    @VisibleForTesting val knownTravelerNumber: TextView by bindView(R.id.known_traveler_number)
    @VisibleForTesting val redressNumber: TextView by bindView(R.id.traveler_redress_number)
    @VisibleForTesting val specialRequest: TextView by bindView(R.id.special_request)
    @VisibleForTesting val frequentFlyerPlan: TextView by bindView(R.id.itin_frequent_flyer_plan)
    @VisibleForTesting val frequentFlyerNumber: TextView by bindView(R.id.itin_frequent_flyer_number)
    @VisibleForTesting val knownTravelerContainer: View by bindView(R.id.known_traveler_container)
    @VisibleForTesting val redressContainer: View by bindView(R.id.redress_number_container)

    var viewModel: FlightItinTravelerPreferenceViewModel by notNullAndObservable { vm ->
        vm.frequentFlyerSubject.subscribe {
            if (!it.isEmpty()) {
                frequentFlyerNumber.visibility = View.VISIBLE
                frequentFlyerPlan.text = ""
                frequentFlyerNumber.text = ""
                for (entry in it.entries) {
                    if (!frequentFlyerPlan.text.isEmpty()) {
                        frequentFlyerPlan.append(System.lineSeparator())
                        frequentFlyerNumber.append(System.lineSeparator())
                    }
                    frequentFlyerPlan.append(entry.value.programName)
                    frequentFlyerNumber.append(entry.value.membershipNumber)
                }
            }
        }
        vm.specialRequestSubject.subscribe {
            specialRequest.text = it
        }
        vm.redressNumberSubject.subscribe {
            if (!it.isNullOrEmpty()) {
                redressContainer.visibility = View.VISIBLE
                redressNumber.text = it
            }
        }
        vm.knownTravelerNumberSubject.subscribe {
            if (!it.isNullOrEmpty()) {
                knownTravelerContainer.visibility = View.VISIBLE
                knownTravelerNumber.text = it
            }
        }
    }

    fun resetWidget() {
        knownTravelerContainer.visibility = View.GONE
        redressContainer.visibility = View.GONE
        frequentFlyerNumber.visibility = View.GONE
        specialRequest.text = context.getString(R.string.none)
        frequentFlyerPlan.text = context.getString(R.string.none)
        frequentFlyerNumber.text = ""
    }

    init {
        View.inflate(context, R.layout.itin_traveler_preferences_widget, this)
    }
}
