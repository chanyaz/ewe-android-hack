package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.hotel.HotelTravelersPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelTravelerEntryWidget
import com.expedia.vm.test.traveler.MockTravelerProvider
import com.expedia.vm.traveler.HotelTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.HotelTravelersViewModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelTravelerEntryWidgetTest {

    private var activity: FragmentActivity by Delegates.notNull()
    private var widget: HotelTravelerEntryWidget by Delegates.notNull()
    private var testVM: HotelTravelerEntryWidgetViewModel by notNull()
    private var travelerPresenter: HotelTravelersPresenter by notNull()
    private var traveler: Traveler by notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        Ui.getApplication(RuntimeEnvironment.application).defaultHotelComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Db.sharedInstance.resetTravelers()
        activity.setTheme(R.style.Theme_Hotels_Default)
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.test_hotel_traveler_presenter)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        val viewStub = activity.findViewById<View>(R.id.traveler_presenter_stub) as ViewStub
        travelerPresenter = viewStub.inflate() as HotelTravelersPresenter
        travelerPresenter.viewModel = HotelTravelersViewModel(activity, LineOfBusiness.HOTELS, true)
        widget = travelerPresenter.travelerEntryWidget as HotelTravelerEntryWidget
        traveler = MockTravelerProvider().getCompleteTraveler()
        Db.sharedInstance.travelers.add(traveler)
        val createTripOptInObservable = (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus
        testVM = HotelTravelerEntryWidgetViewModel(activity.applicationContext, TravelerCheckoutStatus.CLEAN, createTripOptInObservable)
        widget.viewModel = testVM
    }


    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCreateTripTravelerEmailStatusUpdatesEntryWidgetOptOut() {
        (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(MerchandiseSpam.CONSENT_TO_OPT_OUT)
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.VISIBLE,
                text = "I do not want to receive emails from Expedia with travel deals, special offers, and other information.",
                isChecked = true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCreateTripTravelerEmailStatusUpdatesEntryWidgetOptIn() {
        (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(MerchandiseSpam.CONSENT_TO_OPT_IN)
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.VISIBLE,
                text = "I want to receive emails from Expedia with travel deals, special offers, and other information.",
                isChecked = false)
    }

    @Test
    fun testCreateTripTravelerEmailStatusUpdatesEntryWidgetAlways() {
        (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(MerchandiseSpam.ALWAYS)
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.GONE, text = "", isChecked = true)
    }

    @Test
    fun testCreateTripTravelerEmailStatusUpdatesEntryWidgetConsentToOptInSelected() {
        (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(MerchandiseSpam.CONSENT_TO_OPT_IN_SELECTED)
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.GONE, text = "", isChecked = false)
    }

    @Test
    fun testCreateTripTravelerEmailStatusUpdatesEntryWidgetConsentToOptOutSelected() {
        (travelerPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(MerchandiseSpam.CONSENT_TO_OPT_OUT_SELECTED)
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.GONE, text = "", isChecked = false)
    }

    @Test
    fun testCreateTripEmailStatusEmpty() {
        travelerPresenter.showSelectOrEntryState()

        assertCheckBoxValues(visibility = View.GONE, text = "", isChecked = false)
    }

    private fun assertCheckBoxValues(visibility: Int, text: String, isChecked: Boolean) {
        assertEquals(visibility, widget.merchandiseOptCheckBox.visibility)
        assertEquals(text, widget.merchandiseOptCheckBox.text)
        assertEquals(isChecked, widget.merchandiseOptCheckBox.isChecked)
    }
}
