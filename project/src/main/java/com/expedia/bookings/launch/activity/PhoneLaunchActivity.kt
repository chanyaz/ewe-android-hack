package com.expedia.bookings.launch.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripUtils
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.dialog.FlightCheckInDialogBuilder
import com.expedia.bookings.dialog.GooglePlayServicesDialog
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.ItinItemListFragment
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment
import com.expedia.bookings.fragment.SoftPromptDialogFragment
import com.expedia.bookings.hotel.animation.TranslateYAnimator
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.launch.fragment.PhoneLaunchFragment
import com.expedia.bookings.launch.widget.PhoneLaunchToolbar
import com.expedia.bookings.launch.widget.ProWizardLaunchTabView
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.notification.NotificationManager
import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.tracking.AppStartupTimeClientLog
import com.expedia.bookings.tracking.AppStartupTimeLogger
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.RouterToLaunchTimeLogger
import com.expedia.bookings.tracking.RouterToSignInTimeLogger
import com.expedia.bookings.utils.AbacusHelperUtils
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.CarnivalUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DebugMenu
import com.expedia.bookings.utils.DebugMenuFactory
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.LXNavUtils
import com.expedia.bookings.utils.PlayStoreUtil
import com.expedia.bookings.utils.ProWizardBucketCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.setContentDescriptionToolbarTabs
import com.expedia.bookings.widget.DisableableViewPager
import com.expedia.bookings.widget.itin.ItinListView
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.ui.AbstractAppCompatActivity
import com.expedia.util.PackageUtil
import com.expedia.util.PermissionsUtils.havePermissionToAccessLocation
import com.expedia.util.PermissionsUtils.isFirstTimeAskingLocationPermission
import com.expedia.util.PermissionsUtils.requestLocationPermission
import com.mobiata.android.LocationServices
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.fragment.CopyrightFragment
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import io.reactivex.disposables.Disposable
import org.joda.time.LocalDate
import javax.inject.Inject

class PhoneLaunchActivity : AbstractAppCompatActivity(), PhoneLaunchFragment.LaunchFragmentListener, ItinListView.OnListModeChangedListener, AccountSettingsFragment.AccountFragmentListener,
        ItinItemListFragment.ItinItemListFragmentListener, LoginConfirmLogoutDialogFragment.DoLogoutListener, AboutSectionFragment.AboutSectionFragmentListener
        , AboutUtils.CountrySelectDialogListener, ClearPrivateDataDialog.ClearPrivateDataDialogListener, CopyrightFragment.CopyrightFragmentListener {

    private var pagerSelectedPosition = PAGER_POS_LAUNCH

    lateinit var appStartupTimeLogger: AppStartupTimeLogger
        @Inject set

    lateinit var routerToLaunchTimeLogger: RouterToLaunchTimeLogger
        @Inject set

    lateinit var routerToSignInTimeLogger: RouterToSignInTimeLogger
        @Inject set

    lateinit var clientLogServices: IClientLogServices
        @Inject set

    lateinit var pointOfSaleStateModel: PointOfSaleStateModel
        @Inject set

    lateinit var userStateManager: UserStateManager
        @Inject set

    lateinit var notificationManager: NotificationManager
        @Inject set

    var jumpToItinId: String? = null
    private var jumpToActivityCross: String? = null
    private var pagerPosition = PAGER_POS_LAUNCH

    private var itinListFragment: ItinItemListFragment? = null
    private var accountFragment: AccountSettingsFragment? = null
    private var phoneLaunchFragment: PhoneLaunchFragment? = null
    private var softPromptDialogFragment: SoftPromptDialogFragment? = null
    var isLocationPermissionPending = false

    private val userLoginStateChangedModel: UserLoginStateChangedModel by lazy {
        Ui.getApplication(this).appComponent().userLoginStateChangedModel()
    }

    private var loginStateSubsciption: Disposable? = null

    private val debugMenu: DebugMenu by lazy {
        DebugMenuFactory.newInstance(this)
    }
    private var hasMenu = false

    val viewPager by bindView<DisableableViewPager>(R.id.viewpager)
    val toolbar by bindView<PhoneLaunchToolbar>(R.id.launch_toolbar)

    val pagerAdapter: PagerAdapter by lazy {
        PagerAdapter(supportFragmentManager)
    }

    private val bottomNavShadow by bindView<View>(R.id.bottom_tab_layout_shadow)

    private val bottomNavTabLayout by bindView<TabLayout>(R.id.bottom_tab_layout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).appComponent().inject(this)
        Ui.getApplication(this).defaultLaunchComponents()
        Ui.getApplication(this).launchComponent()
        setContentView(R.layout.activity_phone_launch)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter

        if (isBrandColorEnabled(this@PhoneLaunchActivity)) {
            window.statusBarColor = ContextCompat.getColor(this@PhoneLaunchActivity, R.color.brand_primary_dark)
            toolbar.setBackgroundColor(ContextCompat.getColor(this@PhoneLaunchActivity, R.color.brand_primary))
        }

        setSupportActionBar(toolbar)
        supportActionBar?.elevation = 0f
        AbacusHelperUtils.downloadBucket(this)
        if (intent.hasExtra(ARG_ITIN_NUM)) {
            jumpToItinId = intent.getStringExtra(ARG_ITIN_NUM)
        }

        if (savedInstanceState != null) {
            softPromptDialogFragment = supportFragmentManager.findFragmentByTag("fragment_dialog_soft_prompt") as? SoftPromptDialogFragment
            isLocationPermissionPending = savedInstanceState.getBoolean("is_location_permission_pending", false)
        }

        if (AbacusFeatureConfigManager.isUserBucketedForTest(this, AbacusUtils.EBAndroidAppSoftPromptLocation)) {
            loginStateSubsciption = userLoginStateChangedModel.userLoginStateChanged.distinctUntilChanged().filter { isSignIn -> isSignIn == true }.subscribe {
                SettingUtils.save(this, PREF_USER_ENTERS_FROM_SIGNIN, true)
            }
        }

        val lineOfBusiness = intent.getSerializableExtra(Codes.LOB_NOT_SUPPORTED) as LineOfBusiness?
        if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
            // No need to do anything special, waterfall is the default behavior anyway
        } else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
            handleArgJumpToNotification(intent)
            if (jumpToActivityCross == null)
                gotoItineraries()
            else gotoActivitiesCrossSell()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
            gotoItineraries()
        } else if (ItineraryManager.haveTimelyItinItem()) {
            gotoItineraries()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ACCOUNT, false)) {
            gotoAccount()
        } else if (lineOfBusiness != null) {
            val lobName = when (lineOfBusiness) {
                LineOfBusiness.CARS -> getString(R.string.Car)
                LineOfBusiness.LX -> getString(R.string.Activity)
                LineOfBusiness.FLIGHTS -> getString(R.string.Flight)
                LineOfBusiness.PACKAGES -> getString(PackageUtil.packageTitle(this))
                else -> ""
            }
            val errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message).put("lob", lobName).format()
            showLOBNotSupportedAlertMessage(this, errorMessage, R.string.ok)
        } else if (intent.getBooleanExtra(ARG_FORCE_UPGRADE, false)) {
            val lineOfBusiness = intent.getSerializableExtra(ARG_LINE_OF_BUSINESS) as LineOfBusiness?
            PlayStoreUtil.showForceUpgradeDailogWithMessage(this)
        }

        GooglePlayServicesDialog(this).startChecking()

        appStartupTimeLogger.setEndTime()

        if (AbacusFeatureConfigManager.isUserBucketedForTest(this, AbacusUtils.EBAndroidAppSoftPromptLocation)) {
            if (shouldShowSoftPrompt()) {
                requestLocationPermissionViaSoftPrompt()
            }
        } else {
            if (!havePermissionToAccessLocation(this)) {
                requestLocationPermission(this)
            }
        }

        val lastLocation = LocationServices.getLastBestLocation(this, 0)
        CarnivalUtils.getInstance().trackLaunch(
                havePermissionToAccessLocation(this), userStateManager.isUserAuthenticated(),
                userStateManager.userSource.user?.primaryTraveler, ItineraryManager.getInstance().trips,
                userStateManager.getCurrentUserLoyaltyTier(), lastLocation?.latitude, lastLocation?.longitude, PointOfSale.getPointOfSale().url)
    }

    private fun requestLocationPermissionViaSoftPrompt() {
        if (isFirstTimeAskingLocationPermission(this) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            isLocationPermissionPending = true
            if (softPromptDialogFragment == null) {
                softPromptDialogFragment = SoftPromptDialogFragment()
            }
            softPromptDialogFragment?.show(supportFragmentManager, "fragment_dialog_soft_prompt")
            SettingUtils.save(this, PREF_LOCATION_PERMISSION_PROMPT_TIMES, SettingUtils.get(this, PREF_LOCATION_PERMISSION_PROMPT_TIMES, 0) + 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_LOCATION -> {
                phoneLaunchFragment?.onReactToLocationRequest()
                OmnitureTracking.trackLocationNativePrompt(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                isLocationPermissionPending = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("is_location_permission_pending", isLocationPermissionPending)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.hasExtra(ARG_ITIN_NUM)) {
            jumpToItinId = intent.getStringExtra(ARG_ITIN_NUM)
        }

        if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
            gotoWaterfall()
        } else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
            handleArgJumpToNotification(intent)
            if (jumpToActivityCross == null)
                gotoItineraries()
            else gotoActivitiesCrossSell()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
            gotoItineraries()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ACCOUNT, false)) {
            gotoAccount()
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == PAGER_POS_ITIN) {
            if ((itinListFragment?.mSignInPresenter?.back() ?: false)) {
                return
            }
            if (itinListFragment?.isInDetailMode ?: false) {
                itinListFragment?.hideDetails()
                return
            }
            viewPager.currentItem = PAGER_POS_LAUNCH
            return
        } else if (viewPager.currentItem == PAGER_POS_ACCOUNT) {
            viewPager.currentItem = PAGER_POS_LAUNCH
            return
        } else if (viewPager.currentItem == PAGER_POS_LAUNCH) {
            if (phoneLaunchFragment?.onBackPressed() ?: false) return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ITIN_CHECK_IN_WEBPAGE_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                showFlightItinCheckinDialog(data)
            }
        } else if (requestCode == Constants.ITIN_CANCEL_ROOM_WEBPAGE_CODE) {
            if (resultCode == RESULT_OK && data != null && !ExpediaBookingApp.isAutomation()) {
                val tripId = data.getStringExtra(Constants.ITIN_CANCEL_ROOM_BOOKING_TRIP_ID)
                ItineraryManager.getInstance().deepRefreshTrip(tripId, true)
            }
        } else if (requestCode == Constants.ITIN_ROOM_UPGRADE_WEBPAGE_CODE) {
            if (resultCode == RESULT_OK && data != null && !ExpediaBookingApp.isAutomation()) {
                val tripId = data.getStringExtra(Constants.ITIN_ROOM_UPGRADE_TRIP_ID)
                itinListFragment?.showDeepRefreshLoadingView(true)
                ItineraryManager.getInstance().deepRefreshTrip(tripId, true)
            }
        } else if (requestCode == Constants.ITIN_SOFT_CHANGE_WEBPAGE_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val tripId = data.getStringExtra(Constants.ITIN_SOFT_CHANGE_TRIP_ID)
                ItineraryManager.getInstance().deepRefreshTrip(tripId, true)
            }
        }
    }

    private fun showFlightItinCheckinDialog(data: Intent) {
        val airlineName = data.extras.getString(Constants.ITIN_CHECK_IN_AIRLINE_NAME, "")
        val airlineCode = data.extras.getString(Constants.ITIN_CHECK_IN_AIRLINE_CODE, "")
        val confirmationCode = data.extras.getString(Constants.ITIN_CHECK_IN_CONFIRMATION_CODE, "")
        val isSplitTicket = data.extras.getBoolean(Constants.ITIN_IS_SPLIT_TICKET, false)
        val flightLegs = data.extras.getInt(Constants.ITIN_FLIGHT_TRIP_LEGS, 0)
        val alertDialog = FlightCheckInDialogBuilder.onCreateDialog(this, airlineName, airlineCode, confirmationCode, isSplitTicket,
                flightLegs)
        alertDialog.show()
    }

    /**
     * Parses ARG_JUMP_TO_NOTIFICATION out of the intent into a Notification object,
     * sets mJumpToItinId.
     * This function expects to be called only when this activity is started via
     * the given intent (onCreate or onNewIntent) and has side effects that
     * rely on that assumption:
     * 1. Tracks this incoming intent in Omniture.
     * 2. Updates the Notifications table that this notification is dismissed.
     *
     *
     */
    private fun handleArgJumpToNotification(intent: Intent) {
        val jsonNotification = intent.getStringExtra(ARG_JUMP_TO_NOTIFICATION)
        val notification = Notification.getInstanceFromJsonString(jsonNotification)

        if (!notificationManager.hasExisting(notification)) {
            return
        }
        if (notification.uniqueId.contains("_activityCross") || notification.uniqueId.contains("_activityInTrip"))
            jumpToActivityCross = notification.itinId
        else {
            jumpToItinId = notification.itinId
        }
        OmnitureTracking.trackNotificationClick(notification)

        // There's no need to dismiss with the notification manager (android's), since it was set to
        // auto dismiss when clicked.
        notificationManager.setNotificationStatusToDismissed(notification)
    }

    private val pageChangeListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            val tripComponent = Ui.getApplication(this@PhoneLaunchActivity).tripComponent()
            if (tab.position != pagerSelectedPosition) {
                pagerSelectedPosition = tab.position
            } else {
                // if we are in shops or account tab scroll to top
                if (pagerSelectedPosition == PAGER_POS_ACCOUNT) {
                    accountFragment?.smoothScrollToTop()
                } else if (pagerSelectedPosition == PAGER_POS_LAUNCH) {
                    phoneLaunchFragment?.smoothScrollToTop()
                }
            }

            if (tab.position != PAGER_POS_ITIN) {
                if (tripComponent != null) {
                    val itinPageUsablePerformanceModel = tripComponent.itinPageUsableTracking()
                    itinPageUsablePerformanceModel.resetStartTime()
                }
            }

            if (tab.position != pagerPosition) {
                when (tab.position) {
                    PAGER_POS_LAUNCH -> {
                        gotoWaterfall()
                        OmnitureTracking.trackPageLoadLaunchScreen(ProWizardBucketCache.getTrackingValue(this@PhoneLaunchActivity), getLaunchTrackingEventsString())
                    }
                    PAGER_POS_ITIN -> {
                        if (tripComponent != null) {
                            val itinPageUsablePerformanceModel = tripComponent.itinPageUsableTracking()
                            itinPageUsablePerformanceModel.markSuccessfulStartTime(System.currentTimeMillis())
                        }
                        gotoItineraries()
                    }
                    PAGER_POS_ACCOUNT -> {
                        pagerPosition = PAGER_POS_ACCOUNT
                        gotoAccount()
                        OmnitureTracking.trackAccountPageLoad()
                    }
                }
                OmnitureTracking.trackGlobalNavigation(viewPager.currentItem)
            }
        }
    }

    @Synchronized private fun gotoWaterfall() {
        if (pagerPosition != PAGER_POS_LAUNCH) {
            pagerPosition = PAGER_POS_LAUNCH
            viewPager.currentItem = PAGER_POS_LAUNCH

            if (itinListFragment?.isInDetailMode ?: false) {
                itinListFragment?.hideDetails()
            }

            if (hasMenu) {
                supportInvalidateOptionsMenu()
            }
        }
    }

    @Synchronized private fun gotoItineraries() {
        if (pagerPosition != PAGER_POS_ITIN) {

            itinListFragment?.resetTrackingState()
            itinListFragment?.enableLoadItins()

            pagerPosition = PAGER_POS_ITIN
            viewPager.currentItem = PAGER_POS_ITIN

            if (hasMenu) {
                supportInvalidateOptionsMenu()
            }
        }

        if (jumpToItinId != null) {
            if (jumpToItinId != "-1") {
                itinListFragment?.goToItin(jumpToItinId)
            }
            jumpToItinId = null
        }
    }

    @Synchronized private fun gotoActivitiesCrossSell() {
        val data = ItineraryManager.getInstance().getItinCardDataFromItinId(jumpToActivityCross) as ItinCardDataHotel
        jumpToActivityCross = null
        var startDate = data.startDate.toLocalDate()
        var endDate = data.endDate.toLocalDate()
        if (startDate.isBefore(LocalDate.now())) {
            startDate = LocalDate.now()
        }
        if (endDate.isBefore(LocalDate.now())) {
            endDate = LocalDate.now().plusDays(14)
        }
        LXNavUtils.goToActivities(this, null, LXDataUtils.fromHotelParams(this, startDate, endDate, data.propertyLocation),
                NavUtils.FLAG_OPEN_RESULTS)
    }

    @Synchronized private fun gotoAccount() {
        if (userStateManager.isUserAuthenticated()) {
            accountFragment?.refreshUserInfo()
        }
        viewPager.currentItem = PAGER_POS_ACCOUNT
    }

    fun shouldShowOverFlowMenu(): Boolean {
        return (BuildConfig.DEBUG &&
                SettingUtils.get(this@PhoneLaunchActivity, this@PhoneLaunchActivity.getString(R.string.preference_launch_screen_overflow), false))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (shouldShowOverFlowMenu()) {
            debugMenu.onCreateOptionsMenu(menu)
            if (shouldShowOverFlowMenu()) {
                hasMenu = super.onCreateOptionsMenu(menu)
            }
        }
        return hasMenu
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val retVal = super.onPrepareOptionsMenu(menu)
        if (shouldShowOverFlowMenu()) {
            debugMenu.onPrepareOptionsMenu(menu)
        }
        return retVal
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (debugMenu.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        when (viewPager.currentItem) {
            PAGER_POS_LAUNCH -> OmnitureTracking.trackPageLoadLaunchScreen(ProWizardBucketCache.getTrackingValue(this), getLaunchTrackingEventsString())
            PAGER_POS_ACCOUNT -> OmnitureTracking.trackAccountPageLoad()
        }
        if (AbacusFeatureConfigManager.isUserBucketedForTest(this, AbacusUtils.EBAndroidAppSoftPromptLocation)) {
            if (SettingUtils.get(this, PREF_USER_ENTERS_FROM_SIGNIN, false)) {

                if (pagerPosition == PAGER_POS_ITIN) {
                    requestLocationPermission(this)
                } else if (shouldShowSoftPrompt()) {
                    requestLocationPermissionViaSoftPrompt()
                }

                SettingUtils.save(this, PREF_USER_ENTERS_FROM_SIGNIN, false)
            }
        }

        if (routerToLaunchTimeLogger.startTime != null) {
            routerToLaunchTimeLogger.setEndTime()
        }

        if (intent.getBooleanExtra(ARG_FORCE_SHOW_ACCOUNT, false) && routerToSignInTimeLogger.startTime != null) {
            routerToSignInTimeLogger.shouldGoToSignIn = true
        }

        trackTimeLogs()
    }

    private fun trackTimeLogs() {
        AppStartupTimeClientLog.trackTimeLogger(appStartupTimeLogger, clientLogServices)
        AppStartupTimeClientLog.trackTimeLogger(routerToLaunchTimeLogger, clientLogServices)
    }

    private fun shouldShowSoftPrompt(): Boolean {
        return !havePermissionToAccessLocation(this)
                && !isLocationPermissionPending
                && SettingUtils.get(this, PREF_LOCATION_PERMISSION_PROMPT_TIMES, 0) < Constants.LOCATION_PROMPT_LIMIT
    }

    override fun onStart() {
        super.onStart()
        if (ProWizardBucketCache.isBucketed(this)) {
            setupBottomNav()
        } else {
            setupTopNav()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginStateSubsciption?.dispose()
    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val frag: Fragment
            when (position) {
                PAGER_POS_ITIN -> frag = ItinItemListFragment.newInstance(jumpToItinId, true)
                PAGER_POS_LAUNCH -> frag = PhoneLaunchFragment()
                PAGER_POS_ACCOUNT -> frag = AccountSettingsFragment()
                else -> throw RuntimeException("Position out of bounds position=" + position)
            }

            return frag
        }

        override fun getCount(): Int {
            return NUMBER_OF_TABS
        }

        override fun getPageTitle(i: Int): String {
            val title: String
            when (i) {
                PAGER_POS_ITIN -> title = resources.getString(Ui.obtainThemeResID(this@PhoneLaunchActivity, R.attr.skin_tripsTabText))
                PAGER_POS_LAUNCH -> title = resources.getString(R.string.shop)
                PAGER_POS_ACCOUNT -> title = resources.getString(R.string.account_settings_menu_label)
                else -> throw RuntimeException("Position out of bounds position = " + i)
            }
            return title
        }

        // By default a viewPager will bundle/restore all fragment states when destroyed/restored. This is unintuitive
        // and also causes issues when the device language is changed while the app is running. This prevents that.
        override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    }

    override fun onListModeChanged(isInDetailMode: Boolean, animated: Boolean) {
        viewPager.setPageSwipingEnabled(!isInDetailMode)
        if (isInDetailMode) {
            slideNavigationOut()
        } else {
            slideNavigationIn()
        }
    }

    override fun onItinItemListFragmentAttached(frag: ItinItemListFragment?) {
        itinListFragment = frag
        if (pagerPosition == PAGER_POS_ITIN) {
            itinListFragment?.enableLoadItins()
        }

        if (jumpToItinId != null) {
            itinListFragment?.showItinCard(jumpToItinId, false)
            jumpToItinId = null
        }
    }

    override fun onItinCardClicked(data: ItinCardData?) {
        // Do nothing (let fragment handle it)
    }

    override fun doLogout() {
        if (Ui.isAdded(itinListFragment)) {
            itinListFragment?.doLogout()
        }
        if (Ui.isAdded(accountFragment)) {
            accountFragment?.doLogout()
        }
    }

    //Method which are used by account fragment
    override fun onAboutRowClicked(tag: Int): Boolean {
        return accountFragment?.onAboutRowClicked(tag) ?: false
    }

    override fun onAboutRowRebind(tag: Int, titleTextView: TextView?, descriptionTextView: TextView?) {
        accountFragment?.onAboutRowRebind(tag, titleTextView, descriptionTextView)
    }

    override fun onPrivateDataCleared() {
        accountFragment?.onPrivateDataCleared()
        itinListFragment?.doLogout()
    }

    override fun showDialogFragment(dialog: DialogFragment) {
        accountFragment?.showDialogFragment(dialog)
    }

    override fun onNewCountrySelected(pointOfSaleId: Int) {
        accountFragment?.onNewCountrySelected(pointOfSaleId)
        pointOfSaleStateModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
        if (viewPager.currentItem == PAGER_POS_ITIN) {
            OmnitureTracking.trackItinChangePOS()
        }

        itinListFragment?.doLogout()
    }

    override fun onAccountFragmentAttached(frag: AccountSettingsFragment) {
        accountFragment = frag
    }

    override fun onLaunchFragmentAttached(frag: PhoneLaunchFragment) {
        phoneLaunchFragment = frag
    }

    override fun onLogoClick() {
        accountFragment?.onCopyrightLogoClick()
    }

    override fun onLogoLongClick(): Boolean {
        return false
    }

    fun showLOBNotSupportedAlertMessage(context: Context, errorMessage: CharSequence,
                                        confirmButtonResourceId: Int) {
        val b = AlertDialog.Builder(context)
        b.setCancelable(false).setMessage(errorMessage).setPositiveButton(confirmButtonResourceId) { dialog, which -> dialog.dismiss() }.show()
    }

    override fun onDialogCancel() {
        //Do nothing here
    }

    private fun getLaunchTrackingEventsString(): String {
        val events = mutableListOf<String>()

        if (ProWizardBucketCache.isBucketed(this)) {
            events.add("event321")
        } else {
            events.add("event328")
        }

        if (ItineraryManager.haveTimelyItinItem()) {
            events.add("event322")
        }

        if (AbacusFeatureConfigManager.isBucketedInAnyVariant(this, AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen)
                && userStateManager.isUserAuthenticated()
                && TripUtils.getUpcomingAirAttachQualifiedFlightTrip(ItineraryManager.getInstance().trips) != null) {
            events.add("event323")
        }

        if (userStateManager.isUserAuthenticated()) {
            events.add("event324")
        } else {
            events.add("event327")
        }

        if (havePermissionToAccessLocation(this)) {
            events.add("event326")
        }

        return events.joinToString(",")
    }

    private fun setupBottomNav() {
        bottomNavTabLayout.setupWithViewPager(viewPager)
        bottomNavTabLayout.addOnTabSelectedListener(pageChangeListener)
        bottomNavTabLayout.visibility = View.VISIBLE
        bottomNavShadow.visibility = View.VISIBLE

        toolbar.visibility = View.GONE

        setupBottomTabIcons()
    }

    private fun setupBottomTabIcons() {
        val shopTab = ProWizardLaunchTabView(this, R.drawable.ic_search, resources.getString(R.string.shop))
        bottomNavTabLayout.getTabAt(PAGER_POS_LAUNCH)?.customView = shopTab

        val itinTab = ProWizardLaunchTabView(this, R.drawable.ic_work, resources.getString(Ui.obtainThemeResID(this, R.attr.skin_tripsTabText)))
        bottomNavTabLayout.getTabAt(PAGER_POS_ITIN)?.customView = itinTab

        val accountTab = ProWizardLaunchTabView(this, R.drawable.ic_account_circle, resources.getString(R.string.account_settings_menu_label))
        bottomNavTabLayout.getTabAt(PAGER_POS_ACCOUNT)?.customView = accountTab
    }

    private fun setupTopNav() {
        toolbar.visibility = View.VISIBLE
        toolbar.tabLayout.setupWithViewPager(viewPager)
        toolbar.tabLayout.setOnTabSelectedListener(pageChangeListener)
        setContentDescriptionToolbarTabs(this, toolbar.tabLayout)

        bottomNavShadow.visibility = View.GONE
        bottomNavTabLayout.visibility = View.GONE
    }

    private fun slideNavigationOut() {
        if (ProWizardBucketCache.isBucketed(this)) {
            bottomNavShadow.visibility = View.GONE
            val bottomBarSlideOut = TranslateYAnimator(bottomNavTabLayout,
                    startY = 0f, endY = bottomNavTabLayout.height.toFloat(),
                    duration = TOOLBAR_ANIM_DURATION,
                    endAction = { bottomNavTabLayout.visibility = View.GONE })
            bottomBarSlideOut.start()
        } else {
            val toolbarSlideOut = TranslateYAnimator(toolbar,
                    startY = 0f, endY = -toolbar.height.toFloat(),
                    duration = TOOLBAR_ANIM_DURATION,
                    startAction = { toolbar.translationY = 0f },
                    endAction = { toolbar.visibility = View.GONE })
            toolbarSlideOut.start()
        }
    }

    private fun slideNavigationIn() {
        if (ProWizardBucketCache.isBucketed(this)) {
            val bottomBarSlideIn = TranslateYAnimator(bottomNavTabLayout,
                    startY = bottomNavTabLayout.height.toFloat(), endY = 0f,
                    duration = TOOLBAR_ANIM_DURATION,
                    startAction = { bottomNavTabLayout.visibility = View.VISIBLE },
                    endAction = { bottomNavShadow.visibility = View.VISIBLE })
            bottomBarSlideIn.start()
        } else {
            val toolbarSlideIn = TranslateYAnimator(toolbar,
                    startY = -toolbar.height.toFloat(), endY = 0f,
                    duration = TOOLBAR_ANIM_DURATION,
                    startAction = { toolbarSlideInStartAction() })
            toolbarSlideIn.start()
        }
    }

    private fun toolbarSlideInStartAction() {
        toolbar.translationY = (-supportActionBar!!.height).toFloat()
        toolbar.visibility = View.VISIBLE
    }

    companion object {
        private const val TOOLBAR_ANIM_DURATION = 200L

        const val NUMBER_OF_TABS = 3
        const val PAGER_POS_LAUNCH = 0
        const val PAGER_POS_ITIN = 1
        const val PAGER_POS_ACCOUNT = 2

        @JvmField val ARG_LINE_OF_BUSINESS = "ARG_LINE_OF_BUSINESS"
        @JvmField val ARG_FORCE_UPGRADE = "ARG_FORCE_UPGRADE"
        @JvmField val ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL"
        @JvmField val ARG_FORCE_SHOW_ITIN = "ARG_FORCE_SHOW_ITIN"
        @JvmField val ARG_FORCE_SHOW_ACCOUNT = "ARG_FORCE_SHOW_ACCOUNT"
        @JvmField val ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION"
        @JvmField val ARG_ITIN_NUM = "ARG_ITIN_NUM"

        @JvmField val PREF_USER_ENTERS_FROM_SIGNIN = "PREF_USER_ENTERS_FROM_SIGNIN"
        @JvmField val PREF_LOCATION_PERMISSION_PROMPT_TIMES = "PREF_SOFT_PROMPT_LAUNCH_TIMES"

        /** Create intent to open this activity and jump straight to a particular itin item.
         */
        @JvmStatic fun createIntent(context: Context, notification: Notification): Intent {
            val intent = Intent(context, PhoneLaunchActivity::class.java)
            intent.putExtra(ARG_JUMP_TO_NOTIFICATION, notification.toJson().toString())
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Even though we don't use the url directly anywhere, Android OS needs a way
            // to differentiate multiple intents to this same activity.
            // http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            return intent
        }
    }
}
