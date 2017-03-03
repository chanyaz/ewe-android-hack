package com.expedia.bookings.test.robolectric


import android.support.v4.app.FragmentActivity

import android.view.ViewStub
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightTravelerEntryWidgetTest {

    private var widget: FlightTravelerEntryWidget by Delegates.notNull()
    private var testVM: FlightTravelerEntryWidgetViewModel by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private var travelerPresenter: FlightTravelersPresenter by Delegates.notNull()

    @Before
    fun setUp() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.test_traveler_presenter)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
    }

    @Test
    fun testMaterialPassportCountryIsNotCountryCode() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        SettingUtils.save(activity.applicationContext, R.string.preference_universal_checkout_material_forms, true)

        val viewStub = activity.findViewById(R.id.traveler_presenter_stub) as ViewStub
        travelerPresenter = viewStub.inflate() as FlightTravelersPresenter
        widget = travelerPresenter.travelerEntryWidget as FlightTravelerEntryWidget
        assertTrue(widget.materialFormTestEnabled)

        val editBoxForDialog = widget.findViewById(R.id.passport_country_btn) as EditText

        Db.getTravelers().add(Traveler())
        val showPassportCountryObservable = BehaviorSubject.create<Boolean>()
        showPassportCountryObservable.onNext(true)

        testVM = FlightTravelerEntryWidgetViewModel(activity.applicationContext, 0, showPassportCountryObservable,
                TravelerCheckoutStatus.CLEAN)

        widget.viewModel = testVM

        editBoxForDialog.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        testAlert.clickOnItem(1)
        val countryName = widget.passportCountryEditBox.text.toString()
        assertEquals(countryName, widget.passportCountryEditBox.text.toString())

        widget.viewModel = testVM

        assertEquals(countryName, widget.passportCountryEditBox.text.toString())
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        SettingUtils.save(activity.applicationContext, R.string.preference_universal_checkout_material_forms, false)
    }
}
