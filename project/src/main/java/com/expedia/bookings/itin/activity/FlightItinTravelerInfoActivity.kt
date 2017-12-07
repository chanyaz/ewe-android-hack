package com.expedia.bookings.itin.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.itin.vm.FlightItinTravelerPreferenceViewModel
import com.expedia.bookings.itin.vm.FlightItinTravelerViewModel
import com.expedia.bookings.itin.vm.FlightTravelerInfoViewModel
import com.expedia.bookings.itin.vm.TravelerItinToolBarViewModel
import com.expedia.bookings.itin.widget.FlightItinTravelerInfoWidget
import com.expedia.bookings.itin.widget.FlightItinTravelerPreferenceWidget
import com.expedia.bookings.itin.widget.ItinToolbar
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase

open class FlightItinTravelerInfoActivity : AppCompatActivity() {

    companion object {
        private const val FLIGHT_ITIN_ID = "FLIGHT_ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, FlightItinTravelerInfoActivity::class.java)
            i.putExtra(FlightItinTravelerInfoActivity.FLIGHT_ITIN_ID, id)
            return i
        }
    }

    var viewModel: FlightItinTravelerViewModel by notNullAndObservable { vm ->
        vm.itinCardDataNotValidSubject.subscribe {
            finish()
        }
        vm.updateToolbarSubject.subscribe {
            travelerToolbar.viewModel.updateWidget(it)
        }
        vm.updateTravelerListSubject.subscribe {
            if (it.size > 1) {
                travelerTabWidget.removeAllTabs()
                travelerTabWidget.visibility = View.VISIBLE
                for (traveler: Traveler in it) {
                    val newTab = travelerTabWidget.newTab()
                    newTab.text = traveler.fullName
                    travelerTabWidget.addTab(newTab)
                }
            } else {
                vm.updateCurrentTravelerSubject.onNext(it[0])
            }
        }
        vm.updateCurrentTravelerSubject.subscribe { traveler ->
            travelerInfoWidget.resetWidget()
            travelerPreferencesWidget.resetWidget()
            travelerInfoViewModel.travelerObservable.onNext(traveler)
            travelerPreferenceViewModel.travelerObservable.onNext(traveler)
        }
    }

    private val travelerToolbar: ItinToolbar by bindView(R.id.widget_traveler_itin_toolbar)

    private val travelerTabWidget: TabLayout by bindView(R.id.widget_traveler_itin_tab_layout)

    private val travelerInfoWidget: FlightItinTravelerInfoWidget by bindView(R.id.traveler_info_card_view)

    private val travelerPreferencesWidget: FlightItinTravelerPreferenceWidget by bindView(R.id.preferences_card_view)

    var toolbarViewModel: TravelerItinToolBarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishActivity()
        }
    }

    @VisibleForTesting
    var travelerInfoViewModel: FlightTravelerInfoViewModel by notNullAndObservable { vm ->
        vm.travelerObservable.subscribe {
            vm.travelerNameSubject.onNext(it.fullName)
            vm.travelerEmailSubject.onNext(it.email)
            if (it.passengerCategory == PassengerCategory.INFANT_IN_LAP)
                vm.infantInLapSubject.onNext(baseContext.getString(R.string.itin_traveler_infant_in_seat_text))
            val phone = StringBuilder()
            if (!it.phoneCountryCode.isNullOrEmpty() && !it.phoneNumber.isNullOrEmpty()) {
                phone.append("+")
                phone.append(it.phoneCountryCode)
                phone.append(" ")
                phone.append(it.phoneNumber)
                vm.travelerPhoneSubject.onNext(phone.toString())
            }
            if (it.ticketNumbers != null && !it.ticketNumbers.isEmpty()) {
                vm.ticketNumberSubject.onNext(Phrase.from(this, R.string.itin_traveler_ticket_number_TEMPLATE)
                        .put("number", it.ticketNumbers.joinToString(", "))
                        .format().toString())
            }
        }
    }

    private var travelerPreferenceViewModel: FlightItinTravelerPreferenceViewModel by notNullAndObservable { vm ->
        vm.travelerObservable.subscribe {
            vm.knownTravelerNumberSubject.onNext(it.knownTravelerNumber)
            vm.redressNumberSubject.onNext(it.redressNumber)
            if (it.frequentFlyerMemberships != null) {
                vm.frequentFlyerSubject.onNext(it.frequentFlyerMemberships)
            }
            if (it.specialAssistanceOptions != null) {
                vm.specialRequestSubject.onNext(it.specialAssistanceOptions.joinToString(", "))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_itin_traveler_info)
        trackOmniture()
        viewModel = FlightItinTravelerViewModel(this, intent.getStringExtra(FlightItinTravelerInfoActivity.FLIGHT_ITIN_ID))
        toolbarViewModel = TravelerItinToolBarViewModel(this)
        travelerInfoViewModel = FlightTravelerInfoViewModel()
        travelerPreferenceViewModel = FlightItinTravelerPreferenceViewModel()
        travelerToolbar.viewModel = toolbarViewModel
        travelerTabWidget.addOnTabSelectedListener(viewModel)
        travelerInfoWidget.viewModel = travelerInfoViewModel
        travelerPreferencesWidget.viewModel = travelerPreferenceViewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    @VisibleForTesting
    fun trackOmniture() = OmnitureTracking.trackItinTravelerInfo()

    override fun onBackPressed() = finishActivity()

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
