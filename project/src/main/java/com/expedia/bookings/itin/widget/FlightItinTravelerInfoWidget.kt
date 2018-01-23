package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightTravelerInfoViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class FlightItinTravelerInfoWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    @VisibleForTesting val travelerName: TextView by bindView(R.id.traveler_name_text_view)
    @VisibleForTesting val travelerTicketNumber: TextView by bindView(R.id.traveler_ticket_number)
    @VisibleForTesting val travelerEmail: TextView by bindView(R.id.traveler_email)
    @VisibleForTesting val travelerPhone: TextView by bindView(R.id.traveler_phone)
    @VisibleForTesting val infantText: TextView by bindView(R.id.traveler_infant_text_view)
    @VisibleForTesting val divider: View by bindView(R.id.traveler_info_divider)
    @VisibleForTesting val emailContainer: View by bindView(R.id.traveler_email_container)
    @VisibleForTesting val phoneContainer: View by bindView(R.id.traveler_phone_container)
    private val phoneEmailContainer: View by bindView(R.id.email_phone_container)

    var viewModel: FlightTravelerInfoViewModel by notNullAndObservable { vm ->
        vm.travelerNameSubject.subscribe {
            travelerName.text = it
        }
        vm.ticketNumberSubject.subscribe {
            if (!it.isNullOrEmpty()) {
                travelerTicketNumber.visibility = View.VISIBLE
                travelerTicketNumber.text = it
            }
        }
        vm.travelerEmailSubject.subscribe {
            if (!it.isNullOrEmpty()) {
                emailContainer.visibility = View.VISIBLE
                travelerEmail.text = it
                divider.visibility = View.VISIBLE
                phoneEmailContainer.visibility = View.VISIBLE
            }
        }
        vm.infantInLapSubject.subscribe {
            infantText.visibility = View.VISIBLE
            infantText.text = it
        }
        vm.travelerPhoneSubject.subscribe {
            if (!it.isNullOrEmpty()) {
                phoneContainer.visibility = View.VISIBLE
                travelerPhone.text = it
                divider.visibility = View.VISIBLE
                phoneEmailContainer.visibility = View.VISIBLE
            }
        }
    }

    fun resetWidget() {
        divider.visibility = View.GONE
        phoneContainer.visibility = View.GONE
        emailContainer.visibility = View.GONE
        travelerTicketNumber.visibility = View.GONE
        infantText.visibility = View.GONE
        phoneEmailContainer.visibility = View.GONE
    }
    init {
        View.inflate(context, R.layout.itin_traveler_info_widget, this)
    }
}
