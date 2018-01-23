package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.InsuranceWidget
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner :: class)
class InsuranceWidgetTest {

    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var insuranceWidget: InsuranceWidget

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        insuranceWidget = LayoutInflater.from(activity).inflate(R.layout.test_insurance_widget_view, null) as InsuranceWidget
    }

    @Test
    fun testContentDescriptionOfInsuranceDescriptionTextView() {
        assertEquals("3 reasons why you might need trip protection Button", insuranceWidget.descriptionTextView.contentDescription)
    }

    @Test
    fun testContentDescriptionOfInsuranceTermsTextView() {
        assertEquals("Terms, conditions and plan sponsors Button", insuranceWidget.termsTextView.contentDescription)
    }
}
