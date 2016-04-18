package com.expedia.ui

import android.animation.Animator
import android.animation.ValueAnimator
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
import com.expedia.bookings.activity.ExpediaBookingPreferenceActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.ItinItemListFragment
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment
import com.expedia.bookings.fragment.NewPhoneLaunchFragment
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment
import com.expedia.bookings.interfaces.IPhoneLaunchFragmentListener
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.DebugMenu
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.DisableableViewPager
import com.expedia.bookings.widget.ItinListView
import com.expedia.bookings.widget.NewPhoneLaunchToolbar
import com.mobiata.android.fragment.AboutSectionFragment
import com.mobiata.android.util.SettingUtils

class NewPhoneLaunchActivity : AbstractAppCompatActivity(), IPhoneLaunchFragmentListener, ItinListView.OnListModeChangedListener, AccountSettingsFragment.AccountFragmentListener,
        ItinItemListFragment.ItinItemListFragmentListener, LoginConfirmLogoutDialogFragment.DoLogoutListener, AboutSectionFragment.AboutSectionFragmentListener
        , AboutUtils.CountrySelectDialogListener, ClearPrivateDataDialog.ClearPrivateDataDialogListener {

    private val TOOLBAR_ANIM_DURATION = 200

    val NUMBER_OF_TABS = 3
    val PAGER_POS_LAUNCH = 0
    val PAGER_POS_ITIN = 1
    val PAGER_POS_ACCOUNT = 2

    //TODO expand itin based on notification click
    var jumpToItinId: String? = null
    private var pagerPosition = PAGER_POS_LAUNCH

    private var launchFragment: IPhoneLaunchActivityLaunchFragment? = null
    private var itinListFragment: ItinItemListFragment? = null
    private var accountFragment: AccountSettingsFragment? = null

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

    //TODO create intent method

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_new_launch)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = pagerAdapter
        toolBar.slidingTabLayout.setViewPager(viewPager)
        toolBar.slidingTabLayout.setOnPageChangeListener(pageChangeListener)
        setSupportActionBar(toolBar)

        //TODO enable Abacus bucked download
        //    AbacusHelperUtils.downloadBucket(this)

        //TODO show view pager based on intent data

    }

    override fun onLaunchFragmentAttached(frag: IPhoneLaunchActivityLaunchFragment) {
        launchFragment = frag
    }


    override fun onBackPressed() {
        if (launchFragment?.onBackPressed() ?: false) {
            return
        }

        if (viewPager.currentItem == PAGER_POS_ITIN) {
            if (itinListFragment?.isInDetailMode ?: false) {
                itinListFragment?.hideDetails()
                return
            }

            viewPager.currentItem = PAGER_POS_LAUNCH
            return
        }

        super.onBackPressed()
    }


    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            if (position != pagerPosition) {
                if (position == PAGER_POS_LAUNCH) {
                    gotoWaterfall()
                } else if (position == PAGER_POS_ITIN) {
                    gotoItineraries()
                } else if (position == PAGER_POS_ACCOUNT) {
                    //TODO go to account screen
                }
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


    inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val frag: Fragment
            when (position) {
                PAGER_POS_ITIN -> frag = ItinItemListFragment.newInstance(jumpToItinId)
                PAGER_POS_LAUNCH -> frag = NewPhoneLaunchFragment()
            //TODO have to refactor account activity to fragment
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
        accountFragment?.onAboutRowRebind(tag, descriptionTextView)
    }

    override fun onPrivateDataCleared() {
        accountFragment?.onPrivateDataCleared()
    }

    override fun showDialogFragment(dialog: DialogFragment) {
        accountFragment?.showDialogFragment(dialog)
    }

    override fun onNewCountrySelected(pointOfSaleId: Int) {
        accountFragment?.onNewCountrySelected(pointOfSaleId)
    }

    override fun onAccountFragmentAttached(frag: AccountSettingsFragment) {
        accountFragment = frag
    }

}