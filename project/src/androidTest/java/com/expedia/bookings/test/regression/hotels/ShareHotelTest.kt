package com.expedia.bookings.test.regression.hotels

import com.expedia.bookings.test.espresso.PhoneTestCase
import com.expedia.bookings.test.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.stepdefs.phone.CommonSteps
import org.joda.time.DateTime
import org.junit.Test

class ShareHotelTest : PhoneTestCase() {

    @Test
    fun testShareButtonPresence() {
        CommonSteps().setBucketingRulesForTests(mapOf("EBAndroidAppGrowthSocialSharing" to "BUCKETED"))

        LaunchScreen.waitForLOBHeaderToBeDisplayed()
        HotelInfoSiteScreen
                .DeepLink(hotelId = "happypath")
                .setDates(DateTime.now().plusDays(10), DateTime.now().plusDays(15))
                .navigate()
        HotelInfoSiteScreen.Toolbar.verifyShareButtonIsVisible()
    }
}
