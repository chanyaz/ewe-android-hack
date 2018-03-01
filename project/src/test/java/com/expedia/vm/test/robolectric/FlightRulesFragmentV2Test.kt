package com.expedia.vm.test.robolectric

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.fragment.ExpediaSupportFragmentTestUtil
import com.expedia.bookings.fragment.FlightRulesFragmentV2
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.text.HtmlCompat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightRulesFragmentV2Test {
    private lateinit var testFragment: FlightRulesFragmentV2
    private lateinit var activity: ExpediaSupportFragmentTestUtil.FragmentUtilActivity

    @Before
    fun setup() {
        testFragment = FlightRulesFragmentV2()
        activity = Robolectric.buildActivity(ExpediaSupportFragmentTestUtil.FragmentUtilActivity::class.java).create().start().resume().visible().get()
    }

    @Test
    fun testGeneralConditionViewRemoved() {
        val flightResponse = getFlightCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemFlightV2(flightResponse))
        ExpediaSupportFragmentTestUtil.startFragment(activity.supportFragmentManager, testFragment)
        assertEquals(View.GONE, testFragment.view?.findViewById<android.widget.TextView>(R.id.general_condition_view)?.visibility)
    }

    @Test
    fun testGeneralConditionViewVisibility() {
        val flightResponse = getFlightCreateTripResponse()
        addGeneralConditionRules(flightResponse)
        Db.getTripBucket().add(TripBucketItemFlightV2(flightResponse))
        ExpediaSupportFragmentTestUtil.startFragment(activity.supportFragmentManager, testFragment)
        assertEquals(View.VISIBLE, testFragment.view?.findViewById<android.widget.TextView>(R.id.general_condition_view)?.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testEvolableFlightRulesText() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsEvolable)
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        ExpediaSupportFragmentTestUtil.startFragment(activity.supportFragmentManager, testFragment)
        assertEquals("Cancellation Charge will vary by fare, route, time and date of cancellation. Please refer to Evolable Asia's terms and conditions.",
                HtmlCompat.stripHtml(testFragment.view?.findViewById<android.widget.TextView>(R.id.flight_rules_text_view)?.text.toString()).trim())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testEvolablePenaltyRulesText() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightsEvolable)
        Db.getTripBucket().add(TripBucketItemFlightV2(getFlightCreateTripResponse()))
        ExpediaSupportFragmentTestUtil.startFragment(activity.supportFragmentManager, testFragment)
        assertEquals("All tickets will be subject to a Refund Charge of 430 yen (or 500 yen for Skymark flights) per flight, in addition to the Cancellation Charge.",
                HtmlCompat.stripHtml(testFragment.view?.findViewById<android.widget.TextView>(R.id.complete_penalty_rules_link_text_view)?.text.toString()).trim())
    }

    private fun getFlightCreateTripResponse(): FlightCreateTripResponse {
        val flightCreateTripResponse = FlightCreateTripResponse()
        flightCreateTripResponse.flightRules = flightCreateTripResponse.FlightRules()
        val rulesToTextMap = HashMap<String, String>()
        rulesToTextMap.put("AdditionalAirlineFees", "The airline may charge additional fees for checked baggage or other optional services.")
        rulesToTextMap.put("AirlineLiabilityLimitations", "Please read important information regarding airline liability limitations.")

        flightCreateTripResponse.flightRules = flightCreateTripResponse.FlightRules()
        flightCreateTripResponse.flightRules.rulesToText = rulesToTextMap

        val rulesToUrl = HashMap<String, String>()
        rulesToUrl.put("CompletePenaltyRules", "https://www.expedia.com/Fare-Rules?tripid=8dbb2280-613b-455d-85fd-510b887af728&tlid=1033")
        rulesToUrl.put("AirlineLiabilityLimitations", "https://www.expedia.com/pub/agent.dll?qscr=hgen&hfnm=warsaw.htx&tlid=1033")
        flightCreateTripResponse.flightRules.rulesToUrl = rulesToUrl

        flightCreateTripResponse.details = FlightTripDetails()
        flightCreateTripResponse.details.offer = prepareFlightOffer()

        return flightCreateTripResponse
    }

    private fun addGeneralConditionRules(flightResponse: FlightCreateTripResponse) {
        flightResponse.flightRules.rulesToText.put("GeneralConditions", "Please read the general conditions of carriage which can be found here.")
        flightResponse.flightRules.rulesToUrl.put("GeneralConditions", "https://www.expedia.fr/p/support/check-in")
    }

    private fun prepareFlightOffer(): FlightTripDetails.FlightOffer {
        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.isEvolable = true
        flightOffer.evolableUrls = FlightTripDetails.FlightEvolable()
        flightOffer.evolableUrls.evolablePenaltyRulesUrl = "http://www.evolableasia.com/support/japanflight/oem_cancelprice.html"
        flightOffer.evolableUrls.evolableTermsAndConditionsUrl = "https://www.expedia.co.jp/g/rf/terms-of-use?langid=1041"
        flightOffer.evolableUrls.evolableAsiaUrl = "https://www.expedia.co.jp/g/rf/evolable?langid=1041"
        flightOffer.evolableUrls.evolableCancellationChargeUrl = "https://www.expedia.co.jp/g/rf/check-in?langid=1041"
        return flightOffer
    }

    @After
    fun cleanup() {
        Db.getTripBucket().clear()
    }
}
