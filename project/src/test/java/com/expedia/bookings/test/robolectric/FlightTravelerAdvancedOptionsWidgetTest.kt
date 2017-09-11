package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.section.AssistanceTypeSpinnerAdapter
import com.expedia.bookings.section.SeatPreferenceSpinnerAdapter
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerAdvancedOptionsWidget
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowAlertDialog::class))

class FlightTravelerAdvancedOptionsWidgetTest {

    private lateinit var context: FragmentActivity
    private lateinit var widget: FlightTravelerAdvancedOptionsWidget

    @Before
    fun setup() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        context = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        context.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
    }

    @Test
    fun testAssistancePreferenceForMaterialForms() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        widget = LayoutInflater.from(context).inflate(R.layout.test_flight_advanced_options_entry_widget, null) as FlightTravelerAdvancedOptionsWidget
        val editBoxForDialog = widget.findViewById<View>(R.id.edit_assistance_preference_button) as EditText
        widget.viewModel = setupViewModel()
        val assistancePreference = widget.viewModel.assistancePreferenceSubject.value

        assertWidgetHasCorrectData(editBoxForDialog,
                expectedType = Traveler.AssistanceType.WHEELCHAIR_IMMOBILE,
                viewModelValue = assistancePreference,
                textDisplayed = "Wheelchair (immobile)")

        editBoxForDialog.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertDialogIsCorrect(testAlert,
                expectedTitle = "Special Assistance",
                numOfItems = 6,
                expectedPosition = widget.assistanceAdapter.currentPosition,
                actualPosition = widget.assistanceAdapter.getAssistanceTypePosition(assistancePreference))

        testAlert.clickOnItem(0);
        assertWidgetHasCorrectData(editBoxForDialog = editBoxForDialog,
                expectedType = Traveler.AssistanceType.NONE,
                viewModelValue = widget.viewModel.assistancePreferenceSubject.value,
                textDisplayed = "No Special Assistance Required")
    }

    @Test
    fun testSeatPreferenceForMaterialForms() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        widget = LayoutInflater.from(context).inflate(R.layout.test_flight_advanced_options_entry_widget, null) as FlightTravelerAdvancedOptionsWidget
        val editBoxForDialog = widget.findViewById<View>(R.id.edit_seat_preference_button) as EditText
        widget.viewModel = setupViewModel()
        val seatPreference = widget.viewModel.seatPreferenceSubject.value

        assertWidgetHasCorrectData(editBoxForDialog,
                expectedType = Traveler.SeatPreference.AISLE,
                viewModelValue = seatPreference,
                textDisplayed = "Prefers: Aisle Seat")

        editBoxForDialog.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertDialogIsCorrect(testAlert,
                expectedTitle = "Seat Preference",
                numOfItems = 2,
                expectedPosition = widget.seatPreferenceAdapter.currentPosition,
                actualPosition = widget.seatPreferenceAdapter.getSeatPreferencePosition(seatPreference))


        testAlert.clickOnItem(1);
        assertWidgetHasCorrectData(editBoxForDialog,
                expectedType = Traveler.SeatPreference.WINDOW,
                viewModelValue = widget.viewModel.seatPreferenceSubject.value,
                textDisplayed = "Prefers: Window Seat")
    }

    @Test
    fun testContactAirlineMessageForMaterialForms() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        widget = LayoutInflater.from(context).inflate(R.layout.test_flight_advanced_options_entry_widget, null) as FlightTravelerAdvancedOptionsWidget
        val contactAirlineMessage = widget.findViewById<View>(R.id.contact_airline_text) as TextView
        assertEquals(View.VISIBLE, contactAirlineMessage.visibility)
        assertEquals("Please contact airline to confirm requests", contactAirlineMessage.text)
    }

    @Test
    fun testSeatPreferenceSpinners() {
        widget = LayoutInflater.from(context).inflate(R.layout.test_flight_advanced_options_entry_widget, null) as FlightTravelerAdvancedOptionsWidget
        val seatPreferenceSpinner = widget.seatPreferenceSpinner
        val adapter = SeatPreferenceSpinnerAdapter(context)
        seatPreferenceSpinner.adapter = adapter
        widget.viewModel = setupViewModel()

        assertEquals("Aisle", seatPreferenceSpinner.selectedItem)
        seatPreferenceSpinner.setSelection(1)

        assertEquals("Window", seatPreferenceSpinner.selectedItem)
    }

    @Test
    fun testSeatAssistanceSpinner() {
        widget = LayoutInflater.from(context).inflate(R.layout.test_flight_advanced_options_entry_widget, null) as FlightTravelerAdvancedOptionsWidget
        val assistanceSpinner = widget.assistancePreferenceSpinner
        val adapter = AssistanceTypeSpinnerAdapter(context)
        assistanceSpinner.adapter = adapter
        widget.viewModel = setupViewModel()

        assertEquals("Wheelchair (immobile)", assistanceSpinner.selectedItem)

        assistanceSpinner.setSelection(0)
        assertEquals("No Special Assistance Required", assistanceSpinner.selectedItem)
    }

    private fun assertWidgetHasCorrectData(editBoxForDialog: EditText, expectedType: Any, viewModelValue: Any, textDisplayed: String) {
        assertEquals(View.VISIBLE, editBoxForDialog.visibility)
        assertEquals(expectedType, viewModelValue)
        assertEquals(textDisplayed, editBoxForDialog.text.toString())
    }

    private fun assertDialogIsCorrect(testAlertDialog: ShadowAlertDialog, expectedTitle: String, numOfItems: Int, expectedPosition: Int, actualPosition:Int) {
        assertNotNull(testAlertDialog)
        assertEquals(expectedTitle, testAlertDialog.title)
        assertEquals(numOfItems, testAlertDialog.items.size)
        assertEquals(expectedPosition, actualPosition)
    }

    private fun setupViewModel() : TravelerAdvancedOptionsViewModel {

        val vm = TravelerAdvancedOptionsViewModel(context)
        val traveler = Traveler()
        traveler.seatPreference = Traveler.SeatPreference.AISLE
        traveler.assistance = Traveler.AssistanceType.WHEELCHAIR_IMMOBILE
        vm.updateTraveler(traveler)
        return vm
    }
}