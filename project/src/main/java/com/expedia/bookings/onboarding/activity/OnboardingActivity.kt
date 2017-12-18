package com.expedia.bookings.onboarding.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.Scroller
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.enums.OnboardingPagerState
import com.expedia.bookings.onboarding.LeftRightFlingListener
import com.expedia.bookings.onboarding.adapter.OnboardingPagerAdapter
import com.expedia.bookings.services.IClientLogServices
import com.expedia.bookings.tracking.AppStartupTimeClientLog
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.RouterToOnboardingTimeLogger
import com.expedia.bookings.utils.CarnivalUtils
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.DisableableViewPager
import com.squareup.phrase.Phrase
import javax.inject.Inject

class OnboardingActivity: AppCompatActivity() {

    lateinit var clientLogServices: IClientLogServices
        @Inject set

    lateinit var routerToOnboardingTimeLogger: RouterToOnboardingTimeLogger
        @Inject set

    private val title by bindView<TextView>(R.id.title_onboarding)
    private val subtitle by bindView<TextView>(R.id.subtitle_onboarding)
    private val previousButton by bindView<ImageView>(R.id.button_previous)
    private val nextButton by bindView<ImageView>(R.id.button_next)
    private val finalButton by bindView<Button>(R.id.button_final)
    private val viewPager by bindView<DisableableViewPager>(R.id.pager_onboarding)
    private val circles: List<View> by lazy {
        val circles = arrayListOf(
                findViewById<View>(R.id.onboarding_first_circle),
                findViewById<View>(R.id.onboarding_second_circle),
                findViewById<View>(R.id.onboarding_third_circle)
        )
        circles
    }

    private val pagerAdapter: OnboardingPagerAdapter = OnboardingPagerAdapter(this)
    private val flingListener: LeftRightFlingListener = LeftRightFlingListener()
    private val gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(this, flingListener)
    }

    var isAnimating = false
    var previousItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).appComponent().inject(this)
        setContentView(R.layout.activity_onboarding)
        CarnivalUtils.getInstance().toggleNotifications(false)

        viewPager.adapter = pagerAdapter
        viewPager.setPageSwipingEnabled(false)
        viewPager.addOnPageChangeListener(onPageSelectedListener)

        val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
        mScroller.isAccessible = true
        val scroller = FixedSpeedScroller(this, DecelerateInterpolator())
        mScroller.set(viewPager, scroller)

        previousButton.setOnClickListener { showPrevious() }
        nextButton.setOnClickListener { showNext() }
        flingListener.leftFlingSubject.subscribe { showPrevious() }
        flingListener.rightFlingSubject.subscribe { showNext() }
        finalButton.setOnClickListener { finishOnboarding() }

        val handler = Handler()
        handler.postDelayed({
            updateTitle(0)
        }, this.resources.getInteger(R.integer.splash_transition_duration).toLong())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        routerToOnboardingTimeLogger.setEndTime()
        AppStartupTimeClientLog.trackTimeLogger(routerToOnboardingTimeLogger, clientLogServices)
    }

    private val onPageSelectedListener = object: ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            updateTitle(position)
            updateButtonVisibility(position)
            updateCircles(position)
            when(position) {
                OnboardingPagerState.BOOKING_PAGE.ordinal -> OmnitureTracking.trackNewUserOnboardingPage(OnboardingPagerState.BOOKING_PAGE)
                OnboardingPagerState.TRIP_PAGE.ordinal -> OmnitureTracking.trackNewUserOnboardingPage(OnboardingPagerState.TRIP_PAGE)
                OnboardingPagerState.REWARD_PAGE.ordinal -> OmnitureTracking.trackNewUserOnboardingPage(OnboardingPagerState.REWARD_PAGE)
            }
            previousItem = viewPager.currentItem
        }
        override fun onPageScrollStateChanged(state: Int) {
        }
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }
    }

    private fun finishOnboarding() {
        OmnitureTracking.trackNewUserOnboardingGoSignIn()
        NavUtils.goToSignIn(this, false, false, 0)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun updateTitle(position: Int) {
        val pageState = OnboardingPagerState.values()[position]
        title.text = Phrase.from(this, pageState.titleResId).format().toString()
        subtitle.text = Phrase.from(this, pageState.subtitleResId).putOptional("brand_reward_name", this.getString(R.string.brand_reward_name)).format().toString()

        if (position == 0 || position < previousItem) {
            animateTextWhenNewPageLoaded(R.anim.reset_translate_and_fade_in)
        }
        else {
            animateTextWhenNewPageLoaded(R.anim.slide_in_right)
        }
    }

    private fun updateButtonVisibility(position: Int) {
        when (position) {
            0  -> {
                previousButton.visibility = View.INVISIBLE
                nextButton.visibility = View.VISIBLE
                finalButton.visibility  = View.INVISIBLE
            }
            OnboardingPagerState.values().size - 1 -> {
                previousButton.visibility = View.VISIBLE
                nextButton.visibility = View.INVISIBLE
                finalButton.visibility = View.VISIBLE
            }
            else -> {
                previousButton.visibility = View.VISIBLE
                nextButton.visibility = View.VISIBLE
                finalButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateCircles(position: Int) {
        for (i in 0..circles.size-1) {
            if (i == position) {
                circles[i].setBackgroundResource(R.drawable.onboarding_circle_active)
            }
            else {
                circles[i].setBackgroundResource(R.drawable.onboarding_circle)
            }
        }
    }

    private fun showNext() {
        if (!isAnimating && viewPager.currentItem < OnboardingPagerState.values().size -1 ) {
            animateTextThenGoToNewPage(FlingType.RIGHT_FLING)
        }
    }

    private fun showPrevious() {
        if (!isAnimating && viewPager.currentItem > 0) {
            animateTextThenGoToNewPage(FlingType.LEFT_FLING)
        }
    }

    fun animateTextWhenNewPageLoaded(animResTd: Int) {
        isAnimating = true
        val animateView1 = AnimationUtils.loadAnimation(this, animResTd)
        animateView1.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        animateView1.startOffset = this.resources.getInteger(R.integer.onboarding_text_animation_delay).toLong()
        val animateView2 = AnimationUtils.loadAnimation(this, animResTd)
        animateView2.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        animateView2.startOffset = this.resources.getInteger(R.integer.onboarding_text_animation_delay).toLong() * 2

        animateView2.setAnimationListener(object : AnimationListenerAdapter(){
            override fun onAnimationEnd(animation: Animation?) {
                super.onAnimationEnd(animation)
                isAnimating = false
            }
        })
        subtitle.startAnimation(animateView1)
        title.startAnimation(animateView2)
    }


    fun animateTextThenGoToNewPage(flingtype: FlingType) {
        isAnimating = true
        val animResId = if (flingtype == FlingType.RIGHT_FLING) R.anim.fade_out else R.anim.slide_out_right
        val animateView1 = AnimationUtils.loadAnimation(this, animResId)
        animateView1.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        val animateView2 = AnimationUtils.loadAnimation(this, animResId)
        animateView2.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        animateView2.startOffset = this.resources.getInteger(R.integer.onboarding_text_animation_delay).toLong()

        animateView2.setAnimationListener(object : AnimationListenerAdapter(){
            override fun onAnimationEnd(animation: Animation?) {
                super.onAnimationEnd(animation)
                isAnimating = false
                if (flingtype == FlingType.RIGHT_FLING) {
                    viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                }
                else if (flingtype == FlingType.LEFT_FLING) {
                    viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                }
            }
        })
        subtitle.startAnimation(animateView1)
        title.startAnimation(animateView2)
    }

    override fun onDestroy() {
        super.onDestroy()
        CarnivalUtils.getInstance().toggleNotifications(true)
    }

    enum class FlingType {
        RIGHT_FLING,
        LEFT_FLING,
    }

    inner class FixedSpeedScroller(context: Context, interpolator: Interpolator) : Scroller(context, interpolator) {
        private val mDuration = resources.getInteger(R.integer.onboarding_pager_transition_duration)

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, mDuration)
        }
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            super.startScroll(startX, startY, dx, dy, mDuration)
        }
    }
}
