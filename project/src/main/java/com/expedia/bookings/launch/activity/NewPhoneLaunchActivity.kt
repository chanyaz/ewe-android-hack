package com.expedia.bookings.launch.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.dialog.FlightCheckInDialogBuilder
import com.expedia.bookings.dialog.GooglePlayServicesDialog
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.ItinItemListFragment
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment
import com.expedia.bookings.hotel.animation.TranslateYAnimator
import com.expedia.bookings.launch.fragment.NewPhoneLaunchFragment
import com.expedia.bookings.launch.widget.NewPhoneLaunchToolbar
import com.expedia.bookings.launch.widget.ProWizardLaunchTabView
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.tracking.AppStartupTimeClientLog
import com.expedia.bookings.tracking.AppStartupTimeLogger
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ProWizardBucketCache
import com.expedia.bookings.utils.AbacusHelperUtils
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DebugMenu
import com.expedia.bookings.utils.DebugMenuFactory
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.DisableableViewPager
import com.expedia.bookings.widget.itin.ItinListView
import com.expedia.ui.AbstractAppCompatActivity
import com.expedia.util.updateVisibility
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.fragment.CopyrightFragment
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import javax.inject.Inject

class NewPhoneLaunchActivity : AbstractAppCompatActivity(), NewPhoneLaunchFragment.LaunchFragmentListener, ItinListView.OnListModeChangedListener, AccountSettingsFragment.AccountFragmentListener,
        ItinItemListFragment.ItinItemListFragmentListener, LoginConfirmLogoutDialogFragment.DoLogoutListener, AboutSectionFragment.AboutSectionFragmentListener
        , AboutUtils.CountrySelectDialogListener, ClearPrivateDataDialog.ClearPrivateDataDialogListener, CopyrightFragment.CopyrightFragmentListener {

    private val TOOLBAR_ANIM_DURATION = 200L

    val NUMBER_OF_TABS = 3
    val PAGER_POS_LAUNCH = 0
    val PAGER_POS_ITIN = 1
    val PAGER_POS_ACCOUNT = 2
    var PAGER_SELECTED_POS = PAGER_POS_LAUNCH

    lateinit var appStartupTimeLogger: AppStartupTimeLogger
        @Inject set

    lateinit var clientLogServices: ClientLogServices
        @Inject set

    lateinit var pointOfSaleStateModel: PointOfSaleStateModel
        @Inject set

    lateinit var userStateManager: UserStateManager
        @Inject set

    var jumpToItinId: String? = null
    private var pagerPosition = PAGER_POS_LAUNCH

    private var itinListFragment: ItinItemListFragment? = null
    private var accountFragment: AccountSettingsFragment? = null
    private var newPhoneLaunchFragment: NewPhoneLaunchFragment? = null

    private val debugMenu: DebugMenu by lazy {
        DebugMenuFactory.newInstance(this)
    }
    private var hasMenu = false

    val viewPager: DisableableViewPager by lazy {
        findViewById(R.id.viewpager) as DisableableViewPager
    }
    val toolbar: NewPhoneLaunchToolbar by lazy {
        findViewById(R.id.launch_toolbar) as NewPhoneLaunchToolbar
    }

    val pagerAdapter: PagerAdapter by lazy {
        PagerAdapter(supportFragmentManager)
    }

    private val bottomNavShadow: View by lazy {
        findViewById(R.id.bottom_tab_layout_shadow)
    }

    private val bottomNavTabLayout: TabLayout by lazy {
        findViewById(R.id.bottom_tab_layout) as TabLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).appComponent().inject(this)
        Ui.getApplication(this).defaultLaunchComponents()
        Ui.getApplication(this).launchComponent()
        setContentView(R.layout.activity_phone_new_launch)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter

        setSupportActionBar(toolbar)
        supportActionBar?.elevation = 0f

        if (intent.hasExtra(ARG_ITIN_NUM)) {
            jumpToItinId = intent.getStringExtra(ARG_ITIN_NUM)
        }

        val lineOfBusiness = intent.getSerializableExtra(Codes.LOB_NOT_SUPPORTED) as LineOfBusiness?
        if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
            // No need to do anything special, waterfall is the default behavior anyway
        } else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
            handleArgJumpToNotification(intent)
            gotoItineraries()
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
                else -> ""
            }
            val errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message).put("lob", lobName).format()
            showLOBNotSupportedAlertMessage(this, errorMessage, R.string.ok)
        }
        AbacusHelperUtils.downloadBucket(this)

        GooglePlayServicesDialog(this).startChecking()

        appStartupTimeLogger.setAppLaunchScreenDisplayed(System.currentTimeMillis())
        AppStartupTimeClientLog.trackAppStartupTime(appStartupTimeLogger, clientLogServices)
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
            gotoItineraries()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
            gotoItineraries()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ACCOUNT, false)) {
            gotoAccount()
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == PAGER_POS_ITIN) {
            if ((itinListFragment?.mSignInPresenter?.back() ?: false)) {
                showHideToolBar(true)
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
            if (newPhoneLaunchFragment?.onBackPressed() ?: false) return
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

        if (!Notification.hasExisting(notification)) {
            return
        }

        jumpToItinId = notification.itinId
        OmnitureTracking.trackNotificationClick(notification)

        // There's no need to dismiss with the notification manager, since it was set to
        // auto dismiss when clicked.
        Notification.dismissExisting(notification)
    }

    private val pageChangeListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {

        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {

        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            val tripComponent = Ui.getApplication(this@NewPhoneLaunchActivity).tripComponent()
            if (tab.position != PAGER_SELECTED_POS) {
                PAGER_SELECTED_POS = tab.position
            } else {
                // if we are in shops or account tab scroll to top
                if (PAGER_SELECTED_POS == PAGER_POS_ACCOUNT) {
                    accountFragment?.smoothScrollToTop()
                } else if (PAGER_SELECTED_POS == PAGER_POS_LAUNCH) {
                    newPhoneLaunchFragment?.smoothScrollToTop()
                }
            }

            if (tab.position != PAGER_POS_ITIN) {
                if (tripComponent != null) {
                    val itinPageUsablePerformanceModel = tripComponent.itinPageUsablePerformanceModel()
                    itinPageUsablePerformanceModel.resetStartTime()
                }
            }

            if (tab.position != pagerPosition) {
                when (tab.position) {
                    PAGER_POS_LAUNCH -> {
                        gotoWaterfall()
                        OmnitureTracking.trackPageLoadLaunchScreen(ProWizardBucketCache.getTrackingValue(this@NewPhoneLaunchActivity))
                    }
                    PAGER_POS_ITIN -> {
                        if (tripComponent != null) {
                            val itinPageUsablePerformanceModel = tripComponent.itinPageUsablePerformanceModel()
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
            itinListFragment?.showItinCard(jumpToItinId, false)
            jumpToItinId = null
        }
    }

    @Synchronized private fun gotoAccount() {
        if (userStateManager.isUserAuthenticated()) {
            accountFragment?.refreshUserInfo()
        }
        viewPager.currentItem = PAGER_POS_ACCOUNT
    }

    fun shouldShowOverFlowMenu(): Boolean {
        return (BuildConfig.DEBUG &&
                SettingUtils.get(this@NewPhoneLaunchActivity, this@NewPhoneLaunchActivity.getString(R.string.preference_launch_screen_overflow), false))
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
            PAGER_POS_LAUNCH -> OmnitureTracking.trackPageLoadLaunchScreen(ProWizardBucketCache.getTrackingValue(this))
            PAGER_POS_ACCOUNT -> OmnitureTracking.trackAccountPageLoad()
        }
    }

    override fun onStart() {
        super.onStart()
        if (ProWizardBucketCache.isBucketed(this)) {
            setupBottomNav()
        } else {
            setupTopNav()
        }
    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val frag: Fragment
            when (position) {
                PAGER_POS_ITIN -> frag = ItinItemListFragment.newInstance(jumpToItinId, true)
                PAGER_POS_LAUNCH -> frag = NewPhoneLaunchFragment()
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
                PAGER_POS_ITIN -> title = resources.getString(Ui.obtainThemeResID(this@NewPhoneLaunchActivity, R.attr.skin_tripsTabText))
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
        itinListFragment?.toolBarVisibilitySubject?.subscribe { show ->
            showHideToolBar(show)
            Ui.hideKeyboard(this)
        }
    }

    fun showHideToolBar(show: Boolean) {
        toolbar.updateVisibility(show)
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

    override fun onLaunchFragmentAttached(frag: NewPhoneLaunchFragment) {
        newPhoneLaunchFragment = frag
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

    private fun toolbarSlideInStartAction(): Unit {
        toolbar.translationY = (-supportActionBar!!.height).toFloat()
        toolbar.visibility = View.VISIBLE
    }

    companion object {
        @JvmField val ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL"
        @JvmField val ARG_FORCE_SHOW_ITIN = "ARG_FORCE_SHOW_ITIN"
        @JvmField val ARG_FORCE_SHOW_ACCOUNT = "ARG_FORCE_SHOW_ACCOUNT"
        @JvmField val ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION"
        @JvmField val ARG_ITIN_NUM = "ARG_ITIN_NUM"

        /** Create intent to open this activity and jump straight to a particular itin item.
         */
        @JvmStatic fun createIntent(context: Context, notification: Notification): Intent {
            val intent = Intent(context, NewPhoneLaunchActivity::class.java)
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