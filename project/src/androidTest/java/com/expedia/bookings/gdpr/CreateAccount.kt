package com.expedia.bookings.gdpr

import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.Settings
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.PhoneTestCase
import com.expedia.bookings.test.pagemodels.appengagement.CreateAccountScreen
import com.expedia.bookings.test.pagemodels.appengagement.LegalWebView
import com.expedia.bookings.test.pagemodels.appengagement.LegalWebView.ToolbarTitle.TERMS_OF_USE
import com.expedia.bookings.test.pagemodels.appengagement.LegalWebView.ToolbarTitle.PRIVACY_POLICY
import com.expedia.bookings.test.pagemodels.appengagement.LegalWebView.ToolbarTitle.TERMS_AND_CONDITIONS
import com.expedia.bookings.test.pagemodels.appengagement.LogInScreen
import com.expedia.bookings.test.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.stepdefs.phone.common.HomeScreenSteps
import org.junit.Test

class CreateAccountGDPRTest : PhoneTestCase() {
    @Test fun testUnitedKingdom() { runTestGivenPOS(PointOfSaleId.UNITED_KINGDOM) }
    @Test fun testFrance() { runTestGivenPOS(PointOfSaleId.FRANCE) }
    @Test fun testSpain() { runTestGivenPOS(PointOfSaleId.SPAIN) }
    @Test fun testItaly() { runTestGivenPOS(PointOfSaleId.ITALY) }
    @Test fun testGermany() { runTestGivenPOS(PointOfSaleId.GERMANY) }
    @Test fun testBelgium() { runTestGivenPOS(PointOfSaleId.BELGIUM) }
    @Test fun testFinland() { runTestGivenPOS(PointOfSaleId.FINLAND) }
    @Test fun testIreland() { runTestGivenPOS(PointOfSaleId.IRELAND) }
    @Test fun testNetherlands() { runTestGivenPOS(PointOfSaleId.NETHERLANDS) }
    @Test fun testNorway() { runTestGivenPOS(PointOfSaleId.NORWAY) }
    @Test fun testDenmark() { runTestGivenPOS(PointOfSaleId.DENMARK) }
    @Test fun testSweden() { runTestGivenPOS(PointOfSaleId.SWEDEN) }
    @Test fun testAustria() { runTestGivenPOS(PointOfSaleId.AUSTRIA) }

    fun runTestGivenPOS(posId: PointOfSaleId) {
        Common.setPOS(posId)
        LaunchScreen.waitForLOBHeaderToBeDisplayed()
        HomeScreenSteps.switchToTab("Account")
        LogInScreen.clickSignInWithExpediaButton()
        LogInScreen.clickCreateYourExpediaAccountButton()

        Settings.clearPrivateData()
        CreateAccountScreen.clickTermsOfUseLink()
        LegalWebView.waitUntilLoaded()
        LegalWebView.verifyToolbarTitle(TERMS_OF_USE)
        LegalWebView.verifyGDPRBannerPresence(true)
        LegalWebView.closeTheView()

        Settings.clearPrivateData()
        CreateAccountScreen.clickPrivacyPolicyLink()
        LegalWebView.waitUntilLoaded()
        LegalWebView.verifyToolbarTitle(PRIVACY_POLICY)
        LegalWebView.verifyGDPRBannerPresence(true)
        LegalWebView.closeTheView()

        if (posId != PointOfSaleId.GERMANY) { //Germany does not have T&C
            Settings.clearPrivateData()
            CreateAccountScreen.clickTermsOfUseCheckbox()
            CreateAccountScreen.clickTermsAndConditionsLink()
            LegalWebView.waitUntilLoaded()
            LegalWebView.verifyToolbarTitle(TERMS_AND_CONDITIONS)
            LegalWebView.verifyGDPRBannerPresence(true)
            LegalWebView.closeTheView()
        }
    }
}
