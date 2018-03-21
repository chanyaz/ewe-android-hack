package com.expedia.bookings.fragment

import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AboutWebViewActivity
import com.expedia.bookings.activity.OpenSourceLicenseWebViewActivity
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.mia.activity.CustomerFirstActivity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.shouldShowCustomerFirstGuarantee
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.fragment.CopyrightFragment
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class AccountSettingsFragmentTest {

    lateinit var fragment: AccountSettingsFragment
    lateinit var activity: FragmentUtilActivity

    @Before
    fun before() {
        activity = Robolectric.setupActivity(FragmentUtilActivity::class.java)
        activity.setTheme(R.style.LaunchTheme)
    }

    @Test
    fun nonRewardsMemberDoesNotSeeRewardsInfo() {
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("No Rewards ForMe", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("norewards@mobiata.com", R.id.toolbar_email)
        assertViewIsEffectivelyGone(R.id.toolbar_loyalty_tier_text)
        assertViewIsEffectivelyGone(R.id.pending_points)
        assertViewIsEffectivelyGone(R.id.first_row_country)
        assertViewIsEffectivelyGone(R.id.points_monetary_value_label)
        assertViewIsEffectivelyGone(R.id.points_monetary_value)
    }

    @Test
    fun nonRewardsMemberSeesCurrencyAndCountry() {
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
    }

    @Test
    fun baseTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getBaseTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Base Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("basetier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_base_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("1,802", R.id.available_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.first_row_country)
        assertViewIsEffectivelyGone(R.id.pending_points)
        assertViewIsEffectivelyGone(R.id.points_monetary_value_label)
        assertViewIsEffectivelyGone(R.id.points_monetary_value)
        assertViewIsEffectivelyGone(R.id.currency_label)
        assertViewIsEffectivelyGone(R.id.currency)
    }

    @Test
    fun middleTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getMiddleTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Middle Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("middletier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_middle_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("22,996", R.id.available_points)
        assertTextIsDisplayedInTextView("965 pending", R.id.pending_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
        assertTextIsDisplayedInTextView("Points value", R.id.points_monetary_value_label)
        assertTextIsDisplayedInTextView("$3,285.14", R.id.points_monetary_value)
        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
    }

    @Test
    fun topTierRewardsMemberSeesPoints() {
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()

        assertTextIsDisplayedInTextView("Top Tier Rewards", R.id.toolbar_name)
        assertTextIsDisplayedInTextView("toptier@mobiata.com", R.id.toolbar_email)
        assertTextIsDisplayedInTextView(R.string.reward_top_tier_name_short, R.id.toolbar_loyalty_tier_text)
        assertTextIsDisplayedInTextView("54,206", R.id.available_points)
        assertTextIsDisplayedInTextView("5,601 pending", R.id.pending_points)
        assertCountryViewDisplayed("USA", R.drawable.ic_flag_us_icon, R.id.second_row_country)
        assertTextIsDisplayedInTextView("Points value", R.id.points_monetary_value_label)
        assertTextIsDisplayedInTextView("$7,743.41", R.id.points_monetary_value)
        assertTextIsDisplayedInTextView("Currency", R.id.currency_label)
        assertTextIsDisplayedInTextView("USD", R.id.currency)
    }

    @Test
    fun argentinaDisplaysProperly() {
        doCountryTest(PointOfSaleId.ARGENTINA, "ARG", R.drawable.ic_flag_ar_icon)
    }

    @Test
    fun canadaDisplaysProperly() {
        doCountryTest(PointOfSaleId.CANADA, "CAN", R.drawable.ic_flag_ca_icon)
    }

    @Test
    fun hongKongDisplaysProperly() {
        doCountryTest(PointOfSaleId.HONG_KONG, "HKG", R.drawable.ic_flag_hk_icon)
    }

    @Test
    fun koreaDisplaysProperly() {
        doCountryTest(PointOfSaleId.SOUTH_KOREA, "KOR", R.drawable.ic_flag_kr_icon)
    }

    @Test
    fun unitedKingdomDisplaysProperly() {
        doCountryTest(PointOfSaleId.UNITED_KINGDOM, "GBR", R.drawable.ic_flag_uk_icon)
    }

    @Test
    fun buttonsToExternalResourcesWorkProperly() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppAccountsEditWebView)
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        clickViewWithTextInSection(activity.resources.getString(R.string.account_settings_edit_label), R.id.section_account)
        assertIntentFiredToOpenWebviewWithUri(PointOfSale.getPointOfSale().accountPageUrl)

        clickViewWithTextInSection("Expedia Website", R.id.section_contact_us)
        assertIntentFiredToViewUri(PointOfSale.getPointOfSale().websiteUrl)

        clickViewWithTextInSection(activity.resources.getString(R.string.app_support), R.id.section_contact_us)
        assertIntentFiredToOpenWebviewWithUri(ProductFlavorFeatureConfiguration.getInstance().getAppSupportUrl(activity))

        clickViewWithTextInSection(activity.resources.getString(R.string.rate_our_app), R.id.section_communicate)
        assertIntentFiredToViewUri("market://details?id=" + activity.packageName)

        clickViewWithTextInSection(activity.resources.getString(R.string.terms_and_conditions), R.id.section_legal)
        assertIntentFiredToOpenWebviewWithUri(PointOfSale.getPointOfSale().termsAndConditionsUrl)

        clickViewWithTextInSection(activity.resources.getString(R.string.info_label_privacy_policy), R.id.section_legal)
        assertIntentFiredToOpenWebviewWithUri(PointOfSale.getPointOfSale().privacyPolicyUrl)

        clickViewWithTextInSection(activity.resources.getString(R.string.open_source_software_licenses), R.id.section_legal)
        assertIntentFiredToViewOSSLicenses()

        fragment.view?.findViewById<View>(R.id.logo)?.performClick()
        assertIntentFiredToViewUri(ProductFlavorFeatureConfiguration.getInstance().getCopyrightLogoUrl(activity))
    }

    @Test
    fun staticTextIsCorrect() {
        givenSignedInAsUser(getNonRewardsMember())
        givenFragmentSetup()

        val multiBrandExpected = Phrase.from(activity, R.string.copyright_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .put("year", Calendar.getInstance().get(Calendar.YEAR))
                .format()
                .toString()
        val expediaExpected = Phrase.from(activity, R.string.expedia_group_copyright_TEMPLATE)
                .put("year", Calendar.getInstance().get(Calendar.YEAR))
                .format()
                .toString()
        val copyrightView = fragment.view?.findViewById<TextView>(R.id.copyright_info)

        if (BuildConfig.brand != "Expedia") {
            assertEquals(multiBrandExpected, copyrightView?.text)
        } else {
            assertEquals(expediaExpected, copyrightView?.text)
        }

        val expected = activity.getString(R.string.this_app_makes_use_of_the_following) + " " +
                activity.getString(R.string.open_source_names)
        val ossView = fragment.view?.findViewById<TextView>(R.id.open_source_credits_textview)
        assertEquals(expected, ossView?.text)
    }

    @Test
    fun appSupportEmailUs() {
        val webViewActivity = Robolectric.buildActivity(AboutWebViewActivity::class.java).create().get()
        val webView = LayoutInflater.from(webViewActivity).inflate(R.layout.web_view_toolbar, null) as FrameLayout
        val toolbarView = webView.findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)

        assertFalse(toolbarView.isOverflowMenuShowing)
    }

    @Test
    fun testTermsAndConditionsHiddenForAustria() {
        givenPOS(PointOfSaleId.AUSTRIA)
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()
        assertTermsLinkVisibility(View.GONE)
    }

    @Test
    fun testTermsAndConditionsHiddenForGermany() {
        givenPOS(PointOfSaleId.GERMANY)
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()

        assertTermsLinkVisibility(View.GONE)
    }

    @Test
    fun testBundlePackageTitleChangeVariate1() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.PackagesTitleChange, AbacusVariant.ONE.value)
        givenFragmentSetup()
        givenSignedInAsUser(getMiddleTierRewardsMember())
        val pendingPoints = fragment.view?.findViewById<TextView>(R.id.pending_points)
        pendingPoints?.performClick()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        val stringToTest = dialog.findViewById<TextView>(R.id.packages_pp)
        assertEquals("Hotel + Flight", stringToTest.text.toString())
    }

    @Test
    fun testBundlePackageTitleChangeVariate2() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.PackagesTitleChange, AbacusVariant.TWO.value)
        givenFragmentSetup()
        givenSignedInAsUser(getMiddleTierRewardsMember())
        val pendingPoints = fragment.view?.findViewById<TextView>(R.id.pending_points)
        pendingPoints?.performClick()
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        val stringToTest = dialog.findViewById<TextView>(R.id.packages_pp)
        assertEquals("Hotel + Flight Deals", stringToTest.text.toString())
    }

    private fun doCountryTest(pointOfSaleId: PointOfSaleId, expectedCountryCode: String, expectedFlagResId: Int) {
        givenPOS(pointOfSaleId)
        givenSignedInAsUser(getTopTierRewardsMember())
        givenFragmentSetup()

        assertCountryViewDisplayed(expectedCountryCode, expectedFlagResId, R.id.second_row_country)

        if (shouldShowCustomerFirstGuarantee(activity)) {
            clickViewWithTextInSection(activity.resources.getString(R.string.customer_first_chat_with_us_now), R.id.section_contact_us)

            val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
            OmnitureTestUtils.assertLinkTracked("Accounts", "App.Account.Support.CFG", Matchers.allOf(
                    OmnitureMatchers.withProps(mapOf(16 to "App.Account.Support.CFG")),
                    OmnitureMatchers.withEvars(mapOf(28 to "App.Account.Support.CFG"))), mockAnalyticsProvider)

            assertIntentFiredToOpenActivity(CustomerFirstActivity::class.java.name)
        } else {
            clickViewWithTextInSection(activity.resources.getString(R.string.booking_support), R.id.section_contact_us)
            clickPhoneSupportButton()

            val userStateManager = Ui.getApplication(RuntimeEnvironment.application).appComponent().userStateManager()
            val user = userStateManager.userSource.user

            assertIntentFiredToDialPhone(PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(user))
        }
    }

    private fun givenFragmentSetup() {
        fragment = AccountSettingsFragment()
        activity.supportFragmentManager.beginTransaction().add(1, fragment, null).commit()
        activity.setFragment(fragment)
    }

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenPOS(pointOfSaleId: PointOfSaleId) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, Integer.toString(pointOfSaleId.id))
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun getNonRewardsMember(): User {
        val user = User()
        val traveler = Traveler()

        traveler.firstName = "No"
        traveler.middleName = "Rewards"
        traveler.lastName = "ForMe"
        traveler.email = "norewards@mobiata.com"
        user.primaryTraveler = traveler

        return user
    }

    private fun getBaseTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Base"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "basetier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.BASE
        loyaltyInfo.loyaltyPointsAvailable = 1802.0
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getMiddleTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Middle"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "middletier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.MIDDLE
        loyaltyInfo.loyaltyPointsAvailable = 22996.0
        loyaltyInfo.loyaltyPointsPending = 965.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("3285.14", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$3,285.14")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getTopTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Top"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "toptier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 54206.0
        loyaltyInfo.loyaltyPointsPending = 5601.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("7743.41", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$7,743.41")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun clickViewWithTextInSection(expectedText: String, @IdRes sectionId: Int) {
        val sectionView = fragment.view?.findViewById<View>(sectionId) as ViewGroup

        clickChildViewWithText(expectedText, sectionView)
    }

    private fun clickChildViewWithText(expectedText: String, viewGroup: ViewGroup) {
        for (i in 0..viewGroup.childCount - 1) {
            val childView = viewGroup.getChildAt(i)
            if (childView is ViewGroup) {
                clickChildViewWithText(expectedText, childView)
            } else if (childView is TextView && childView.text == expectedText) {
                childView.performClick()
            }
        }
    }

    private fun clickPhoneSupportButton() {
        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        Shadows.shadowOf(dialog).clickOnItem(0)
    }

    private fun assertTextIsDisplayedInTextView(expectedString: String, @IdRes viewId: Int) {
        assertViewIsEffectivelyVisibile(viewId)
        assertEquals(expectedString, (fragment.view?.findViewById<TextView>(viewId))?.text)
    }

    private fun assertTextIsDisplayedInTextView(@StringRes expectedStringId: Int, @IdRes viewId: Int) {
        assertEquals(fragment.view?.context?.getString(expectedStringId), (fragment.view?.findViewById<TextView>(viewId))?.text)
    }

    private fun assertCountryViewDisplayed(expectedCountryCode: String, @DrawableRes expectedFlagResId: Int, @IdRes viewId: Int) {
        assertViewIsEffectivelyVisibile(viewId)
        val countryView = fragment.view?.findViewById<View>(viewId)
        assertEquals(expectedCountryCode, (countryView?.findViewById<TextView>(R.id.country))?.text)
        val flagViewDrawable = shadowOf(countryView?.findViewById<ImageView>(R.id.flagView)?.drawable)
        assertEquals(expectedFlagResId, flagViewDrawable?.createdFromResId)
    }

    private fun assertTermsLinkVisibility(visibility: Int) {
        val ROW_TERMS_AND_CONDITIONS = 5
        val termsFragment = Ui.findSupportFragment<AboutSectionFragment>(fragment, "TAG_LEGAL")
        val termsAndConditionsView = termsFragment.view?.findViewWithTag<View>(ROW_TERMS_AND_CONDITIONS)
        assertEquals(visibility, termsAndConditionsView?.visibility)
    }

    private fun assertViewIsEffectivelyGone(@IdRes viewId: Int) {
        val view = fragment.view?.findViewById<View>(viewId)
        if (view != null && view.visibility != View.GONE) {
            var parent = view.parent
            while (parent != null && parent is View) {
                if (parent.visibility == View.GONE) {
                    return
                }
                parent = (parent as ViewParent).parent
            }
        }
        assertEquals(View.GONE, view?.visibility)
    }

    private fun assertViewIsEffectivelyVisibile(@IdRes viewId: Int) {
        val view = fragment.view?.findViewById<View>(viewId)
        assertEquals(View.VISIBLE, view?.visibility)
        var parent = view?.parent
        while (parent != null && parent is View) {
            assertEquals(View.VISIBLE, parent.visibility)
            parent = (parent as ViewParent).parent
        }
    }

    private fun assertIntentFiredToViewOSSLicenses() {
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(OpenSourceLicenseWebViewActivity::class.java.name, actualIntent.component.className)
    }

    private fun assertIntentFiredToOpenWebviewWithUri(uri: String?) {
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(AboutWebViewActivity::class.java.name, actualIntent.component.className)
        assertStartsWith(uri, actualIntent.getStringExtra("ARG_URL"))
    }

    private fun assertIntentFiredToViewUri(uri: String?) {
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, actualIntent.action)
        assertEquals(uri, actualIntent.data.toString())
    }

    private fun assertIntentFiredToDialPhone(phoneNumber: String?) {
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, actualIntent.action)
        val actualPhoneNumber = actualIntent.data.toString().replace("[^0-9]".toRegex(), "")
        val expectedPhoneNumber = phoneNumber?.replace("[^0-9]".toRegex(), "")
        assertEquals(expectedPhoneNumber, actualPhoneNumber)
    }

    private fun assertIntentFiredToOpenActivity(className: String?) {
        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(className, actualIntent.component.className)
    }

    private fun assertStartsWith(expected: String?, actual: String?) {
        assertEquals(expected, actual?.substring(0, expected?.length ?: 0))
    }

    class FragmentUtilActivity() : FragmentActivity(), AboutSectionFragment.AboutSectionFragmentListener, CopyrightFragment.CopyrightFragmentListener {

        private lateinit var fragment: AccountSettingsFragment

        fun setFragment(fragment: AccountSettingsFragment) {
            this.fragment = fragment
        }

        override fun onAboutRowClicked(tag: Int): Boolean {
            return fragment.onAboutRowClicked(tag)
        }

        override fun onAboutRowRebind(tag: Int, titleTextView: TextView?, descriptionTextView: TextView?) {
            fragment.onAboutRowRebind(tag, titleTextView, descriptionTextView)
        }

        override fun onLogoLongClick(): Boolean {
            return false
        }

        override fun onLogoClick() {
            fragment.onCopyrightLogoClick()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val view = LinearLayout(this)
            view.id = 1

            setContentView(view)
        }
    }
}
