package com.expedia.bookings.itin.flight.manageBooking

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerInfoViewModel
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerPreferenceViewModel
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerToolBarViewModel
import com.expedia.bookings.itin.flight.traveler.FlightItinTravelerViewModel
import com.expedia.bookings.itin.scopes.HasItinId
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTabLayout
import com.expedia.bookings.itin.scopes.HasToolbar
import com.expedia.bookings.itin.scopes.HasTravelerInfo
import com.expedia.bookings.itin.scopes.HasTravelerPreference
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject

class Sweeett<S>(val scope: S): DefaultLifecycleObserver where S : HasToolbar, S : HasTabLayout, S : HasTravelerInfo, S : HasTravelerPreference, S : HasItinId, S : HasStringProvider{

    val finishSubject = PublishSubject.create<Unit>()

    var viewModel: FlightItinTravelerViewModel by notNullAndObservable { vm ->
        vm.itinCardDataNotValidSubject.subscribe {
            finishSubject.onNext(Unit)
        }
        vm.updateToolbarSubject.subscribe {
            scope.toolbar.viewModel.updateWidget(it)
        }
        vm.updateTravelerListSubject.subscribe {
            if (it.size > 1) {
                scope.tabLayout.removeAllTabs()
                scope.tabLayout.visibility = View.VISIBLE
                for (traveler: Traveler in it) {
                    val newTab = scope.tabLayout.newTab()
                    newTab.text = traveler.fullName
                    scope.tabLayout.addTab(newTab)
                }
            } else {
                vm.updateCurrentTravelerSubject.onNext(it[0])
            }
        }
        vm.updateCurrentTravelerSubject.subscribe { traveler ->
            scope.travelerInfo.resetWidget()
            scope.travelerPreference.resetWidget()
            travelerInfoViewModel.travelerObservable.onNext(traveler)
            travelerPreferenceViewModel.travelerObservable.onNext(traveler)
        }
    }

    var toolbarViewModel: FlightItinTravelerToolBarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finishSubject.onNext(Unit)
        }
    }

    var travelerInfoViewModel: FlightItinTravelerInfoViewModel by notNullAndObservable { vm ->
        vm.travelerObservable.subscribe {
            vm.travelerNameSubject.onNext(it.fullName)
            vm.travelerEmailSubject.onNext(it.email)
            if (it.passengerCategory == PassengerCategory.INFANT_IN_LAP)
                vm.infantInLapSubject.onNext(scope.strings.fetch(R.string.itin_traveler_infant_in_seat_text))
            val phone = StringBuilder()
            if (!it.phoneCountryCode.isNullOrEmpty() && !it.phoneNumber.isNullOrEmpty()) {
                phone.append("+")
                phone.append(it.phoneCountryCode)
                phone.append(" ")
                phone.append(it.phoneNumber)
                vm.travelerPhoneSubject.onNext(phone.toString())
            }
            if (it.ticketNumbers != null && !it.ticketNumbers.isEmpty()) {
                val ticketNumbers = it.ticketNumbers.joinToString(", ")
                vm.ticketNumberSubject.onNext(scope.strings.fetchWithPhrase(R.string.itin_traveler_ticket_number_TEMPLATE, mapOf("number" to ticketNumbers)))
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


    fun trackOmniture() = OmnitureTracking.trackItinTravelerInfo()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModel.onResume()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        trackOmniture()
        viewModel = FlightItinTravelerViewModel(scope.strings, scope.itinId)
        toolbarViewModel = FlightItinTravelerToolBarViewModel()
        travelerInfoViewModel = FlightItinTravelerInfoViewModel()
        travelerPreferenceViewModel = FlightItinTravelerPreferenceViewModel()
        scope.toolbar.viewModel = toolbarViewModel
        scope.tabLayout.addOnTabSelectedListener(viewModel)
        scope.travelerInfo.viewModel = travelerInfoViewModel
        scope.travelerPreference.viewModel = travelerPreferenceViewModel
    }
}
