package com.expedia.bookings.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.expedia.account.Config
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.ExpediaBookingPreferenceActivity
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.dialog.TextViewDialog
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.AdTracker
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.ClearPrivateDataUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.DebugMenu
import com.expedia.bookings.utils.DebugMenuFactory
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.mobiata.android.SocialUtils
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.fragment.CopyrightFragment
import com.mobiata.android.util.AndroidUtils
import com.mobiata.android.util.HtmlUtils
import com.mobiata.android.util.SettingUtils
import com.squareup.otto.Subscribe
import com.squareup.phrase.Phrase
import java.text.NumberFormat
import java.util.Calendar
import javax.inject.Inject

class AccountSettingsFragment : Fragment(), UserAccountRefresher.IUserAccountRefreshListener {

    private val TAG_SUPPORT = "TAG_SUPPORT"
    private val TAG_LEGAL = "TAG_LEGAL"
    private val TAG_DEBUG_SETTINGS = "TAG_DEBUG_SETTINGS"
    private val TAG_COPYRIGHT = "TAG_COPYRIGHT"
    private val TAG_COMMUNICATE = "TAG_COMMUNICATE"
    private val TAG_APP_SETTINGS = "TAG_APP_SETTINGS"
    private val GOOGLE_SIGN_IN_SUPPORT = "GOOGLE_SIGN_IN_SUPPORT"

    private val ROW_BOOKING_SUPPORT = 1
    private val ROW_EXPEDIA_WEBSITE = 2
    private val ROW_APP_SUPPORT = 3
    private val ROW_WERE_HIRING = 4
    private val ROW_PRIVACY_POLICY = 5
    private val ROW_TERMS_AND_CONDITIONS = 6
    private val ROW_ATOL_INFO = 7
    private val ROW_OPEN_SOURCE_LICENSES = 8

    val ROW_VSC_VOYAGES = 9
    private val PKG_VSC_VOYAGES = "com.vsct.vsc.mobile.horaireetresa.android"

    private val ROW_CLEAR_PRIVATE_DATA = 10
    private val ROW_RATE_APP = 11
    private val ROW_COUNTRY = 12

    private val ROW_SETTINGS = 13
    private val ROW_TEST_SCREEN = 14
    private val INSTALL_SHORTCUTS = 15
    private val ROW_REWARDS_VISA_CARD = 16

    private val aboutUtils: AboutUtils by lazy {
        AboutUtils(activity)
    }

    private var appSettingsFragment: AboutSectionFragment? = null
    private var supportFragment: AboutSectionFragment? = null
    private var copyrightFragment: CopyrightFragment? = null
    private var legalFragment: AboutSectionFragment? = null
    private var debugFragment: AboutSectionFragment? = null
    private val scrollContainer: ScrollView by bindView(R.id.scroll_container)
    val openSourceCredits: TextView by bindView(R.id.open_source_credits_textview)
    val logo: ImageView by lazy {
        activity.findViewById(com.mobiata.android.R.id.logo) as ImageView
    }

    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val toolBarHeight: Float by lazy {
        Ui.getToolbarSize(context).toFloat()
    }
    val signInButton: Button by bindView(R.id.sign_in_button)
    val signOutButton: Button by bindView(R.id.sign_out_button)
    val facebookSignInButton: Button by bindView(R.id.sign_in_with_facebook_button)
    val createAccountButton: Button by bindView(R.id.create_account_button)
    val googleAccountChange: TextView by bindView(R.id.google_account_change)

    val signInSection: ViewGroup by bindView(R.id.section_sign_in)
    val loyaltySection: ViewGroup by bindView(R.id.section_loyalty_info)

    val availablePointsTextView: TextView by bindView (R.id.available_points)
    val pendingPointsTextView: TextView by bindView(R.id.pending_points)
    val currencyTextView: TextView by bindView(R.id.currency)
    val pointsMonetaryValueLabel: TextView by bindView(R.id.points_monetary_value_label)
    val pointsMonetaryValueTextView: TextView by bindView(R.id.points_monetary_value)

    val firstRowContainer: View by bindView(R.id.first_row_container)
    val secondRowContainer: View by bindView(R.id.second_row_container)
    val rowDivider1: View by bindView(R.id.row_divider1)
    val rowDivider2: View by bindView(R.id.row_divider2)
    val firstRowCountry: View by bindView(R.id.first_row_country)
    val secondRowCountry: View by bindView(R.id.second_row_country)


    val memberNameView: TextView by bindView(R.id.toolbar_name)
    val memberEmailView: TextView by bindView(R.id.toolbar_email)
    val memberTierView: TextView by bindView(R.id.toolbar_loyalty_tier_text)
    val userAccountRefresher: UserAccountRefresher by lazy {
        UserAccountRefresher(context, LineOfBusiness.PROFILE, this)
    }

    protected lateinit var userStateManager: UserStateManager
        @Inject set

    val debugMenu: DebugMenu by lazy {
        DebugMenuFactory.newInstance(activity, ExpediaBookingPreferenceActivity::class.java)
    }

    val debugAlertDialog: AlertDialog by lazy {
        val alertDialog = AlertDialog.Builder(context)
        val convertView = activity.layoutInflater.inflate(R.layout.alert_dialog_with_list, null)
        alertDialog.setView(convertView)
        alertDialog.setIcon(R.drawable.ic_launcher)
        alertDialog.setTitle(R.string.debug_screens_sub_menu)
        val activityList = convertView.findViewById(R.id.listView) as ListView
        val activityInfoList = debugMenu.debugActivityInfoList
        val names = activityInfoList.map(DebugMenu.DebugActivityInfo::displayName)
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names)
        activityList.adapter = adapter
        activityList.setOnItemClickListener({ adapterView, view, position, id ->
            debugMenu.startTestActivity(activityInfoList[position].className)
        })
        alertDialog.setPositiveButton(R.string.ok, { dialog, which -> dialog.dismiss() })
        alertDialog.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Ui.getApplication(context).appComponent().inject(this)
        userAccountRefresher.setUserAccountRefreshListener(this)
        if (context is AccountFragmentListener) {
            val listener: AccountFragmentListener = context
            listener.onAccountFragmentAttached(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_settings, null)
    }

    override fun onDetach() {
        super.onDetach()
        userAccountRefresher.setUserAccountRefreshListener(null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var builder: AboutSectionFragment.Builder
        val ft = childFragmentManager.beginTransaction()
        setGoogleAccountChangeVisibility(googleAccountChange)
        googleAccountChange.setOnClickListener(GoogleAccountChangeListener())
        setCountryChangeListeners()

        // App Settings
        appSettingsFragment = Ui.findSupportFragment<AboutSectionFragment>(this, TAG_APP_SETTINGS)
        if (appSettingsFragment == null) {
            builder = AboutSectionFragment.Builder(context)

            builder.setTitle(R.string.about_section_app_settings)
            val rowDescriptor = AboutSectionFragment.RowDescriptor()

            rowDescriptor.clickTag = ROW_COUNTRY
            rowDescriptor.title = getString(R.string.preference_point_of_sale_title)
            rowDescriptor.description = getCountryDescription()
            builder.addRow(rowDescriptor)

            appSettingsFragment = builder.build()
            ft.add(R.id.section_app_settings, appSettingsFragment, TAG_APP_SETTINGS)
        }

        // Support
        supportFragment = Ui.findSupportFragment<AboutSectionFragment>(this, TAG_SUPPORT)
        if (supportFragment == null) {
            builder = AboutSectionFragment.Builder(context)

            builder.setTitle(R.string.about_section_support)

            builder.addRow(getPOSSpecificWebsiteSupportString(), ROW_EXPEDIA_WEBSITE)

            builder.addRow(R.string.booking_support, ROW_BOOKING_SUPPORT)
            builder.addRow(R.string.app_support, ROW_APP_SUPPORT)

            if (ProductFlavorFeatureConfiguration.getInstance().isRewardsCardEnabled) {
                builder.addRow(Phrase.from(context, R.string.rewards_visa_card_TEMPLATE).put("brand_reward_name",
                        getString(R.string.brand_reward_name)).format()
                        .toString(), ROW_REWARDS_VISA_CARD)
            }
            supportFragment = builder.build()
            ft.add(R.id.section_contact_us, supportFragment, TAG_SUPPORT)
        }

        // Communicate
        var communicateFragment: AboutSectionFragment? = Ui.findSupportFragment<AboutSectionFragment>(this, TAG_COMMUNICATE)
        if (communicateFragment == null) {
            if (ProductFlavorFeatureConfiguration.getInstance().isCommunicateSectionEnabled()) {
                builder = AboutSectionFragment.Builder(context)

                builder.setTitle(R.string.about_section_communicate)

                if (ProductFlavorFeatureConfiguration.getInstance().isRateOurAppEnabled()) {
                    builder.addRow(R.string.rate_our_app, ROW_RATE_APP)
                }

                if (ProductFlavorFeatureConfiguration.getInstance().isWeReHiringEnabled()) {
                    builder.addRow(R.string.WereHiring, ROW_WERE_HIRING)
                }

                communicateFragment = builder.build()
                ft.add(R.id.section_communicate, communicateFragment, TAG_COMMUNICATE)
            }
        }

        // T&C, privacy, etc
        legalFragment = Ui.findSupportFragment<AboutSectionFragment>(this, TAG_LEGAL)
        if (legalFragment == null) {
            builder = AboutSectionFragment.Builder(context)
            builder.setTitle(R.string.legal_information)
            builder.addRow(R.string.clear_private_data, ROW_CLEAR_PRIVATE_DATA)
            builder.addRow(R.string.info_label_terms_conditions, ROW_TERMS_AND_CONDITIONS)
            builder.addRow(R.string.info_label_privacy_policy, ROW_PRIVACY_POLICY)
            builder.addRow(R.string.lawyer_label_atol_information, ROW_ATOL_INFO)
            builder.addRow(R.string.open_source_software_licenses, ROW_OPEN_SOURCE_LICENSES)
            legalFragment = builder.build()
            ft.add(R.id.section_legal, legalFragment, TAG_LEGAL)
        }

        // For debug builds only and show only if overflow menu is disabled
        debugFragment = Ui.findSupportFragment<AboutSectionFragment>(this, TAG_DEBUG_SETTINGS)
        if (debugFragment == null && BuildConfig.DEBUG &&
                !SettingUtils.get(context, context.getString(R.string.preference_launch_screen_overflow), false)) {
            builder = AboutSectionFragment.Builder(context)
            builder.setTitle(R.string.debug_settings)
            builder.addRow(R.string.Settings, ROW_SETTINGS)
            val endpointString = context.resources.getString(R.string.connected_server,
                    Ui.getApplication(activity).appComponent().endpointProvider().endPoint.toString())
            builder.addRow(endpointString, 0)

            val buildNumber = context.resources.getString(R.string.build_number, BuildConfig.BUILD_NUMBER)
            builder.addRow(buildNumber, 0)

            val hashString = context.resources.getString(R.string.hash) + " " + BuildConfig.GIT_REVISION
            builder.addRow(hashString, 0)
            builder.addRow(R.string.debug_screens_sub_menu, ROW_TEST_SCREEN)
            builder.addRow(R.string.debug_install_shortcuts, INSTALL_SHORTCUTS)

            debugFragment = builder.build()
            ft.add(R.id.debug_section, debugFragment, TAG_DEBUG_SETTINGS)
        }

        // Copyright
        copyrightFragment = Ui.findSupportFragment<CopyrightFragment>(this, TAG_COPYRIGHT)
        if (copyrightFragment == null) {
            val copyBuilder = CopyrightFragment.Builder()
            copyBuilder.setAppName(R.string.app_copyright_name)
            copyBuilder.setCopyright(getCopyrightString())
            copyBuilder.setLogo(ProductFlavorFeatureConfiguration.getInstance().posSpecificBrandLogo)
            copyrightFragment = copyBuilder.build()
            ft.add(R.id.section_copyright, copyrightFragment, TAG_COPYRIGHT)
        }

        // All done
        ft.commit()

        openSourceCredits.text = getString(R.string.this_app_makes_use_of_the_following) + " " + getString(R.string.open_source_names)

        signInButton.setOnClickListener {
            val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.SignIn, null)
            User.signIn(activity, args)
        }

        signOutButton.setOnClickListener {
            OmnitureTracking.trackClickSignOut()
            LoginConfirmLogoutDialogFragment().show(activity.supportFragmentManager, LoginConfirmLogoutDialogFragment.TAG)
        }

        facebookSignInButton.setOnClickListener {
            val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.FacebookSignIn, null)
            User.signIn(activity, args)
        }

        createAccountButton.setOnClickListener {
            val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.CreateAccount, null)
            User.signIn(activity, args)
        }

        openSourceCredits.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                openSourceCredits.viewTreeObserver.removeOnPreDrawListener(this)
                legalFragment?.setRowVisibility(ROW_ATOL_INFO, if (PointOfSale.getPointOfSale().showAtolInfo()) View.VISIBLE else View.GONE)
                return true
            }
        })

        if (userStateManager.isUserAuthenticated()) {
            toolbarShadow.alpha = 0f
        }

        pendingPointsTextView.setOnClickListener {
            val pendingPointsDialog = PendingPointsDialogFragment.newInstance(resources.getString(R.string.pending_points_dialog_title))
            pendingPointsDialog.show(fragmentManager, "fragment_dialog_pending_points")
            OmnitureTracking.trackPendingPointsTooltipTapped()
        }
    }

    override fun onResume() {
        super.onResume()
        Events.register(this)
        adjustLoggedInViews()
        scrollContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        legalFragment?.setRowVisibility(ROW_TERMS_AND_CONDITIONS,
                (if (PointOfSale.getPointOfSale().termsAndConditionsUrl.isNullOrEmpty()) View.GONE else View.VISIBLE))
    }

    override fun onUserAccountRefreshed() {
        adjustLoggedInViews()
    }

    fun refreshUserInfo() {
        userAccountRefresher.forceAccountRefresh()
    }

    val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        if (userStateManager.isUserAuthenticated()) {
            val value = scrollContainer.scrollY / toolBarHeight
            toolbarShadow.alpha = Math.min(1f, Math.max(0f, value))
        }
    }

    override fun onPause() {
        super.onPause()
        Events.unregister(this)
        scrollContainer.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    fun showDialogFragment(dialog: DialogFragment) {
        dialog.show(activity.supportFragmentManager, "dialog_from_about_utils")
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onPOSChanged(event: Events.PhoneLaunchOnPOSChange) {
        adjustLoggedInViews()
        appSettingsFragment?.notifyOnRowDataChanged(ROW_COUNTRY)
        supportFragment?.notifyOnRowDataChanged(ROW_EXPEDIA_WEBSITE)
        legalFragment?.setRowVisibility(ROW_ATOL_INFO, if (PointOfSale.getPointOfSale().showAtolInfo()) View.VISIBLE else View.GONE)
        legalFragment?.setRowVisibility(ROW_TERMS_AND_CONDITIONS,
                (if (PointOfSale.getPointOfSale().termsAndConditionsUrl.isNullOrEmpty()) View.GONE else View.VISIBLE))
    }

    fun onNewCountrySelected(pointOfSaleId: Int) {
        SettingUtils.save(context, R.string.PointOfSaleKey, Integer.toString(pointOfSaleId))

        ClearPrivateDataUtil.clear(context)
        PointOfSale.onPointOfSaleChanged(context)
        AdTracker.updatePOS()

        activity.setResult(Constants.RESULT_CHANGED_PREFS)
        Events.post(Events.PhoneLaunchOnPOSChange())

        Toast.makeText(context, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show()

        refreshCopyrightFragment()
    }

    fun doLogout() {
        User.signOut(activity)
        scrollContainer.smoothScrollTo(0, 0)
        adjustLoggedInViews()
        toolbarShadow.alpha = 1.0f
    }

    fun onPrivateDataCleared() {
        adjustLoggedInViews()
        Toast.makeText(context, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show()
    }

    private fun showCountrySelector() {
        if (PointOfSale.getAllPointsOfSale(context).size > 1) {
            OmnitureTracking.trackClickCountrySetting()
            val selectCountryDialog = aboutUtils.createCountrySelectDialog()
            selectCountryDialog.show(activity.supportFragmentManager, "selectCountryDialog")
        }
    }

    private fun getCountryDescription(): String {
        val info = PointOfSale.getPointOfSale()
        val country = getString(info.countryNameResId)
        val url = info.url
        return country + " - " + url
    }

    private fun getCopyrightString(): String {
        return Phrase.from(context, R.string.copyright_TEMPLATE).put("brand", BuildConfig.brand).put("year", Calendar.getInstance().get(Calendar.YEAR)).format().toString()
    }

    private fun getPOSSpecificWebsiteSupportString(): String {
        return Phrase.from(context, R.string.website_TEMPLATE).put("brand", ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format().toString()
    }

    private fun adjustLoggedInViews() {
        if (userStateManager.isUserAuthenticated()) {
            toolbarShadow.alpha = 0f
            signInSection.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            // commitAllowingStateLoss is okay because we call this function again in onResume anyway
            childFragmentManager.beginTransaction().hide(appSettingsFragment).commitAllowingStateLoss()

            val user = Db.getUser()
            val member = user.primaryTraveler

            memberNameView.text = member.fullName
            memberEmailView.text = member.email
            val userLoyaltyInfo = user.loyaltyMembershipInformation
            loyaltySection.visibility = View.VISIBLE
            if (userLoyaltyInfo?.isLoyaltyMembershipActive ?: false) {
                firstRowContainer.visibility = View.VISIBLE
                rowDivider1.visibility = View.VISIBLE
                if (ProductFlavorFeatureConfiguration.getInstance().shouldShowMemberTier()) {
                    memberTierView.visibility = View.VISIBLE
                    when (userLoyaltyInfo?.loyaltyMembershipTier) {
                        LoyaltyMembershipTier.BASE -> {
                            memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_base_tier)
                            memberTierView.setTextColor(ContextCompat.getColor(context, R.color.reward_base_tier_text_color))
                            memberTierView.setText(R.string.reward_base_tier_name_short)
                        }
                        LoyaltyMembershipTier.MIDDLE -> {
                            memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_middle_tier)
                            memberTierView.setTextColor(ContextCompat.getColor(context, R.color.reward_middle_tier_text_color))
                            memberTierView.setText(R.string.reward_middle_tier_name_short)
                        }
                        LoyaltyMembershipTier.TOP -> {
                            memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_top_tier)
                            memberTierView.setTextColor(ContextCompat.getColor(context, R.color.reward_top_tier_text_color))
                            memberTierView.setText(R.string.reward_top_tier_name_short)
                        }
                        else -> {
                            // User is not in member ship tier
                        }
                    }
                } else {
                    memberTierView.visibility = View.GONE
                }


                val numberFormatter = NumberFormat.getInstance()
                if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType) {
                    availablePointsTextView.text = numberFormatter.format(member.loyaltyPointsAvailable)
                } else {
                    availablePointsTextView.text = userLoyaltyInfo?.loyaltyMonetaryValue?.formattedMoneyFromAmountAndCurrencyCode
                }
                if (member.loyaltyPointsPending > 0) {
                    pendingPointsTextView.visibility = View.VISIBLE
                    pendingPointsTextView.text = getString(R.string.loyalty_points_pending,
                            numberFormatter.format(member.loyaltyPointsPending))
                    pendingPointsTextView.contentDescription = pendingPointsTextView.text.toString() + Phrase.from(activity, R.string.pending_points_button).format().toString()
                } else {
                    pendingPointsTextView.visibility = View.GONE
                }

                if (userLoyaltyInfo?.isAllowedToShopWithPoints ?: false && ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
                    val loyaltyMonetaryValue = userLoyaltyInfo?.loyaltyMonetaryValue
                    currencyTextView.text = loyaltyMonetaryValue?.currency
                    setupCountryView(secondRowCountry)
                    if (Strings.isNotEmpty(loyaltyMonetaryValue?.currency)) {
                        pointsMonetaryValueTextView.text = loyaltyMonetaryValue?.formattedMoney
                    }
                    pointsMonetaryValueTextView.visibility = View.VISIBLE
                    pointsMonetaryValueLabel.visibility = View.VISIBLE
                    secondRowContainer.visibility = View.VISIBLE
                    rowDivider2.visibility = View.VISIBLE
                    firstRowCountry.visibility = View.GONE
                } else {
                    setupCountryView(firstRowCountry)
                    secondRowContainer.visibility = View.GONE
                    pointsMonetaryValueTextView.visibility = View.GONE
                    pointsMonetaryValueLabel.visibility = View.GONE
                    firstRowCountry.visibility = View.VISIBLE
                    rowDivider2.visibility = View.GONE
                }

            } else {
                currencyTextView.text = CurrencyUtils.currencyForLocale(PointOfSale.getPointOfSale().threeLetterCountryCode)
                setupCountryView(secondRowCountry)
                memberTierView.visibility = View.GONE
                rowDivider1.visibility = View.GONE
                rowDivider2.visibility = View.VISIBLE
                secondRowContainer.visibility = View.VISIBLE
                firstRowContainer.visibility = View.GONE
            }
        } else {
            loyaltySection.visibility = View.GONE
            signInSection.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
            childFragmentManager.beginTransaction().show(appSettingsFragment).commitAllowingStateLoss()

            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled) {
                facebookSignInButton.visibility = View.VISIBLE
            } else {
                facebookSignInButton.visibility = View.GONE
            }

            createAccountButton.text = Phrase.from(context, R.string.acct__Create_a_new_brand_account).put("brand", BuildConfig.brand).format()
        }
    }

    private fun setupCountryView(countryView: View) {
        val countryTextView = countryView.findViewById(R.id.country) as TextView
        val flagIconView = countryView.findViewById(R.id.flagView) as ImageView
        val pos = PointOfSale.getPointOfSale()
        countryTextView.text = pos.threeLetterCountryCode
        flagIconView.setImageResource(pos.countryFlagResId)
        countryView.contentDescription = Phrase.from(context, R.string.country_pos_cont_desc_TEMPLATE)
                .put("countrycode", pos.threeLetterCountryCode)
                .format()
    }

    interface AccountFragmentListener {
        fun onAccountFragmentAttached(frag: AccountSettingsFragment)
    }

    fun onAboutRowClicked(id: Int): Boolean {
        when (id) {
            ROW_COUNTRY -> {
                showCountrySelector()
                return true
            }
            ROW_BOOKING_SUPPORT -> {
                OmnitureTracking.trackClickSupportBooking()
                val contactExpediaDialog = aboutUtils.createContactExpediaDialog()
                contactExpediaDialog.show(activity.supportFragmentManager, "contactExpediaDialog")
                return true
            }
            ROW_EXPEDIA_WEBSITE -> {
                aboutUtils.openExpediaWebsite()
                return true
            }
            ROW_REWARDS_VISA_CARD -> {
                aboutUtils.openRewardsCard()
                return true
            }
            ROW_APP_SUPPORT -> {
                aboutUtils.openAppSupport()
                return true
            }
            ROW_RATE_APP -> {
                aboutUtils.rateApp()
                return true
            }
            ROW_WERE_HIRING -> {
                aboutUtils.openCareers()
                return true
            }

        // Legal section
            ROW_TERMS_AND_CONDITIONS -> {
                aboutUtils.openTermsAndConditions()
                return true
            }
            ROW_PRIVACY_POLICY -> {
                aboutUtils.openPrivacyPolicy()
                return true
            }
            ROW_ATOL_INFO -> {
                OmnitureTracking.trackClickAtolInformation()

                val builder = WebViewActivity.IntentBuilder(context)

                val message = getString(R.string.lawyer_label_atol_long_message)
                val html = HtmlUtils.wrapInHeadAndBody(message)

                builder.setHtmlData(html)

                startActivity(builder.intent)

                return true
            }
            ROW_OPEN_SOURCE_LICENSES -> {
                aboutUtils.openOpenSourceLicenses()
                return true
            }

            AboutSectionFragment.ROW_FLIGHT_TRACK -> {
                OmnitureTracking.trackClickDownloadAppLink("FlightTrack")
                return false
            }
            AboutSectionFragment.ROW_FLIGHT_BOARD -> {
                OmnitureTracking.trackClickDownloadAppLink("FlightBoard")
                return false
            }

            ROW_VSC_VOYAGES -> {
                SocialUtils.openSite(context, AndroidUtils.getMarketAppLink(context, PKG_VSC_VOYAGES))
                return true
            }
            ROW_CLEAR_PRIVATE_DATA -> {
                OmnitureTracking.trackClickClearPrivateData()
                val dialog = ClearPrivateDataDialog()
                dialog.show(activity.supportFragmentManager, "clearPrivateDataDialog")
                return true
            }

            ROW_SETTINGS -> {
                val intent = Intent(context, ExpediaBookingPreferenceActivity::class.java)
                activity.startActivityForResult(intent, Constants.REQUEST_SETTINGS)
                return true
            }

            ROW_TEST_SCREEN -> {
                if (BuildConfig.DEBUG) {
                    debugAlertDialog.show()
                    debugAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.new_launch_alert_dialog_button_color))
                }
                return true
            }

            INSTALL_SHORTCUTS -> {
                if (BuildConfig.DEBUG) {
                    debugMenu.addShortcutsForAllLaunchers()
                }
                return true
            }

        }

        return false
    }

    fun onAboutRowRebind(id: Int, titleTextView: TextView?, descriptionTextView: TextView?) {
        when (id) {
            ROW_COUNTRY -> {
                descriptionTextView?.text = getCountryDescription()
            }
            ROW_EXPEDIA_WEBSITE -> {
                titleTextView?.text = getPOSSpecificWebsiteSupportString()
            }
        }
    }

    fun smoothScrollToTop() {
        scrollContainer.smoothScrollTo(0, 0)
    }

    fun onCopyrightLogoClick() {
        SocialUtils.openSite(context, ProductFlavorFeatureConfiguration.getInstance().getCopyrightLogoUrl(context))
    }

    private fun setGoogleAccountChangeVisibility(view: View) {
        view.visibility = if (ProductFlavorFeatureConfiguration.getInstance().isGoogleAccountChangeEnabled)
            View.VISIBLE
        else
            View.GONE
    }

    private inner class GoogleAccountChangeListener : View.OnClickListener {

        override fun onClick(view: View) {
            val fm = fragmentManager
            var mDialog: TextViewDialog? = fm.findFragmentByTag(GOOGLE_SIGN_IN_SUPPORT) as? TextViewDialog
            if (mDialog == null) {
                //Create the dialog
                mDialog = TextViewDialog()
                mDialog.isCancelable = false
                mDialog.setCanceledOnTouchOutside(false)
                mDialog.setMessage(
                        Phrase.from(context, R.string.google_account_change_message_TEMPLATE).put("brand", BuildConfig.brand).format())
            }
            mDialog.show(fm, GOOGLE_SIGN_IN_SUPPORT)
        }
    }

    private fun setCountryChangeListeners() {
        val countryChangeListener = CountryChangeListener()
        firstRowCountry.setOnClickListener(countryChangeListener)
        secondRowCountry.setOnClickListener(countryChangeListener)
    }

    private fun refreshCopyrightFragment() {
        copyrightFragment = null
        val copyBuilder = CopyrightFragment.Builder()
        copyBuilder.setAppName(R.string.app_copyright_name)
        copyBuilder.setCopyright(getCopyrightString())
        copyBuilder.setLogo(ProductFlavorFeatureConfiguration.getInstance().posSpecificBrandLogo)
        copyrightFragment = copyBuilder.build()
        childFragmentManager.beginTransaction().replace(R.id.section_copyright, copyrightFragment, TAG_APP_SETTINGS).commit()
    }

    private inner class CountryChangeListener : View.OnClickListener {

        override fun onClick(view: View) {
            showCountrySelector()
        }
    }
}