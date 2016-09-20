package com.expedia.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.activity.ExpediaBookingPreferenceActivity
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.dialog.FlightCheckInDialogBuilder
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.ItinItemListFragment
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment
import com.expedia.bookings.fragment.NewPhoneLaunchFragment
import com.expedia.bookings.notification.Notification
import com.expedia.bookings.tracking.AdTracker
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AbacusHelperUtils
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DebugMenu
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.DisableableViewPager
import com.expedia.bookings.widget.ItinListView
import com.expedia.bookings.widget.NewPhoneLaunchToolbar
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase

class NewPhoneLaunchActivity : AbstractAppCompatActivity(), NewPhoneLaunchFragment.LaunchFragmentListener, ItinListView.OnListModeChangedListener, AccountSettingsFragment.AccountFragmentListener,
        ItinItemListFragment.ItinItemListFragmentListener, LoginConfirmLogoutDialogFragment.DoLogoutListener, AboutSectionFragment.AboutSectionFragmentListener
        , AboutUtils.CountrySelectDialogListener, ClearPrivateDataDialog.ClearPrivateDataDialogListener {

    private val TOOLBAR_ANIM_DURATION = 200

    val NUMBER_OF_TABS = 3
    val PAGER_POS_LAUNCH = 0
    val PAGER_POS_ITIN = 1
    val PAGER_POS_ACCOUNT = 2
    var PAGER_SELECTED_POS = PAGER_POS_LAUNCH

    var jumpToItinId: String? = null
    private var pagerPosition = PAGER_POS_LAUNCH

    private var itinListFragment: ItinItemListFragment? = null
    private var accountFragment: AccountSettingsFragment? = null
    private var newPhoneLaunchFragment: NewPhoneLaunchFragment? = null

    private val debugMenu: DebugMenu by lazy {
        DebugMenu(this, ExpediaBookingPreferenceActivity::class.java)
    }
    private var hasMenu = false

    val viewPager: DisableableViewPager by lazy {
        findViewById(R.id.viewpager) as DisableableViewPager
    }
    val toolBar: NewPhoneLaunchToolbar by  lazy {
        findViewById(R.id.launch_toolbar) as NewPhoneLaunchToolbar
    }

    val pagerAdapter: PagerAdapter by lazy {
        PagerAdapter(supportFragmentManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdTracker.trackLaunch()

        Ui.getApplication(this).defaultLaunchComponents()
        setContentView(R.layout.activity_phone_new_launch)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter
        toolBar.slidingTabLayout.setViewPager(viewPager)
        toolBar.slidingTabLayout.setOnPageChangeListener(pageChangeListener)
        toolBar.slidingTabLayout.setOnTabClickedListener { pagerPosition ->
            if (pagerPosition != PAGER_SELECTED_POS) {
                PAGER_SELECTED_POS = pagerPosition
            } else {
                // if we are in shops or account tab scroll to top
                if (PAGER_SELECTED_POS == PAGER_POS_ACCOUNT) {
                    accountFragment?.smoothScrollToTop()
                } else if (PAGER_SELECTED_POS == PAGER_POS_LAUNCH) {
                    newPhoneLaunchFragment?.smoothScrollToTop()
                }

            }
        }
        setSupportActionBar(toolBar)
        supportActionBar?.elevation = 0f

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
        } else if (lineOfBusiness != null) {
            var errorMessage: CharSequence = ""
            if (lineOfBusiness == LineOfBusiness.CARS) {
                errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message).put("lob", getString(R.string.Car)).format()
            } else if (lineOfBusiness == LineOfBusiness.LX) {
                errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message).put("lob", getString(R.string.Activity)).format()
            }
            showLOBNotSupportedAlertMessage(this, errorMessage, R.string.ok)
        }
        AbacusHelperUtils.downloadBucket(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
            gotoWaterfall()
        } else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
            handleArgJumpToNotification(intent)
            gotoItineraries()
        } else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
            gotoItineraries()
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == PAGER_POS_ITIN) {
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
     * *** This is duplicated in ItineraryActivity ***
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


    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            if (position != pagerPosition) {
                when (position) {
                    PAGER_POS_LAUNCH -> {
                        gotoWaterfall()
                        OmnitureTracking.trackPageLoadLaunchScreen()
                    }
                    PAGER_POS_ITIN -> gotoItineraries()
                    PAGER_POS_ACCOUNT -> {
                        accountFragment?.refreshUserInfo()
                        viewPager.currentItem = PAGER_POS_ACCOUNT
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
        when(viewPager.currentItem) {
            PAGER_POS_LAUNCH -> OmnitureTracking.trackPageLoadLaunchScreen()
            PAGER_POS_ACCOUNT -> OmnitureTracking.trackAccountPageLoad()
        }

    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

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

    }

    private val hideToolbarAnimator = ValueAnimator.AnimatorUpdateListener { animation ->
        val yTranslation = animation.animatedValue as Float
        toolBar.translationY = -yTranslation * toolBar.height
    }

    private val hideToolbarListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {
            toolBar.translationY = 0f
        }

        override fun onAnimationEnd(animation: Animator) {
            toolBar.visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator) {
            // ignore
        }

        override fun onAnimationRepeat(animation: Animator) {
            // ignore
        }
    }

    private val showToolbarAnimator = ValueAnimator.AnimatorUpdateListener { animation ->
        val yTranslation = animation.animatedValue as Float
        toolBar.translationY = (1 - yTranslation) * -toolBar.height
    }

    private val showToolbarListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            toolBar.translationY = (-supportActionBar!!.height).toFloat()
            toolBar.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animator) {
            // ignore
        }

        override fun onAnimationCancel(animation: Animator) {
            // ignore
        }

        override fun onAnimationRepeat(animation: Animator) {
            // ignore
        }
    }

    override fun onListModeChanged(isInDetailMode: Boolean, animated: Boolean) {
        viewPager.setPageSwipingEnabled(!isInDetailMode)
        if (isInDetailMode) {
            if (supportActionBar!!.isShowing) {
                val anim = ValueAnimator.ofFloat(0f, 1f)
                anim.duration = TOOLBAR_ANIM_DURATION.toLong()
                anim.addUpdateListener(hideToolbarAnimator)
                anim.addListener(hideToolbarListener)
                anim.start()
            }
        } else {
            // The collapse animation takes 400ms, and the actionbar.show
            // animation happens in 200ms, so make it use the last 200ms
            // of the animation (and check to make sure there wasn't another
            // mode change in between)
            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.duration = TOOLBAR_ANIM_DURATION.toLong()
            anim.addUpdateListener(showToolbarAnimator)
            anim.addListener(showToolbarListener)
            toolBar.visibility = View.VISIBLE
            anim.start()
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
        itinListFragment?.doLogout()
    }

    override fun onAccountFragmentAttached(frag: AccountSettingsFragment) {
        accountFragment = frag
    }

    override fun onLaunchFragmentAttached(frag: NewPhoneLaunchFragment) {
        newPhoneLaunchFragment = frag
    }

    fun showLOBNotSupportedAlertMessage(context: Context, errorMessage: CharSequence,
                                        confirmButtonResourceId: Int) {
        val b = AlertDialog.Builder(context)
        b.setCancelable(false).setMessage(errorMessage).setPositiveButton(confirmButtonResourceId) { dialog, which -> dialog.dismiss() }.show()
    }

    companion object {
        val ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL"
        val ARG_FORCE_SHOW_ITIN = "ARG_FORCE_SHOW_ITIN"
        val ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION"
        /**
         * Create intent to open this activity and jump straight to a particular itin item.
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