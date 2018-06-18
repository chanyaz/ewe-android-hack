package com.expedia.bookings.test.robolectric

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.fragment.FilghtsRouteHappyGuideFragment
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FilghtsRouteHappyGuideFragmentTest {

    private val context = RuntimeEnvironment.application
    lateinit var testFragment: FilghtsRouteHappyGuideFragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        fragmentManager = activity.supportFragmentManager
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testOnlyAmenities() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 1)
        testFragment = FilghtsRouteHappyGuideFragment()
        testFragment.show(fragmentManager, "dummy_tag")
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)
        val wifiLabel = testFragment.wifiLabel
        val entertainmentLabel = testFragment.entertainmentLabel
        val powerLabel = testFragment.powerLabel
        val titleLabel = testFragment.titleLabel
        val moreInfoLabel = testFragment.moreInfoLabel
        val dotsLottieView = testFragment.dotsLottieView
        assertEquals(View.VISIBLE, wifiLabel.visibility)
        assertEquals(View.VISIBLE, entertainmentLabel.visibility)
        assertEquals(View.VISIBLE, powerLabel.visibility)
        assertEquals(View.GONE, dotsLottieView.visibility)
        assertEquals(View.GONE, moreInfoLabel.visibility)
        assertEquals(View.VISIBLE, titleLabel.visibility)
        assertEquals("Flight amenities", titleLabel.text)
    }

    @Test
    fun testOnlyFlightScore() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 2)
        testFragment = FilghtsRouteHappyGuideFragment()
        testFragment.show(fragmentManager, "dummy_tag")
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)
        val ratingLabel = testFragment.ratingLabel
        val titleLabel = testFragment.titleLabel
        val infoLabel = testFragment.infoLabel
        val moreInfoLabel = testFragment.moreInfoLabel
        val amenitiesLottieView = testFragment.amenitiesLottieView
        val ratingsLottieView = testFragment.ratingsLottieView
        val dotsLottieView = testFragment.dotsLottieView
        assertEquals(View.GONE, dotsLottieView.visibility)
        assertEquals(View.INVISIBLE, amenitiesLottieView.visibility)
        assertEquals(View.GONE, infoLabel.visibility)
        assertEquals(View.VISIBLE, titleLabel.visibility)
        assertEquals(View.VISIBLE, ratingsLottieView.visibility)
        assertEquals(View.VISIBLE, ratingLabel.visibility)
        assertEquals(View.VISIBLE, moreInfoLabel.visibility)
        assertEquals("Flight score", titleLabel.text)
    }

    @Test
    fun testAmenitiesFlightScore() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRichContent, 3)
        testFragment = FilghtsRouteHappyGuideFragment()
        testFragment.show(fragmentManager, "dummy_tag")
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)
        val ratingLabel = testFragment.ratingLabel
        val titleLabel = testFragment.titleLabel
        val infoLabel = testFragment.infoLabel
        val moreInfoLabel = testFragment.moreInfoLabel
        val amenitiesLottieView = testFragment.amenitiesLottieView
        val ratingsLottieView = testFragment.ratingsLottieView
        val dotsLottieView = testFragment.dotsLottieView
        assertEquals(View.VISIBLE, dotsLottieView.visibility)
        assertEquals(View.INVISIBLE, amenitiesLottieView.visibility)
        assertEquals(View.VISIBLE, infoLabel.visibility)
        assertEquals(View.VISIBLE, titleLabel.visibility)
        assertEquals(View.VISIBLE, ratingsLottieView.visibility)
        assertEquals(View.VISIBLE, ratingLabel.visibility)
        assertEquals(View.VISIBLE, moreInfoLabel.visibility)
        assertEquals("Flight amenities & score", titleLabel.text)
    }

    @Test
    fun testOmnitureOnDialogButtonClick() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        val sharedPref = context.getSharedPreferences("richContentGuide", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("counter", 2)
        editor.apply()
        testFragment = FilghtsRouteHappyGuideFragment()
        testFragment.show(fragmentManager, "dummy_tag")
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)
        testFragment.dismissButton.performClick()
        OmnitureTestUtils.assertLinkTracked("App.Flight.FSR.AmenitiesPopup.Ok.1",
                "App.Flight.FSR.AmenitiesPopup.Ok.1", mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForRichContentPopUpDisplayed() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        val sharedPref = context.getSharedPreferences("richContentGuide", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("counter", 2)
        editor.apply()
        testFragment = FilghtsRouteHappyGuideFragment()
        testFragment.show(fragmentManager, "dummy_tag")
        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)
        OmnitureTestUtils.assertLinkTracked("App.Flight.FSR.AmenitiesPopupshown.1",
                "App.Flight.FSR.AmenitiesPopupshown.1", mockAnalyticsProvider)
    }
}
