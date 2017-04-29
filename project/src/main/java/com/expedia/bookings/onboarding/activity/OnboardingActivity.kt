package com.expedia.bookings.onboarding.activity

import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.enums.OnboardingPagerState
import com.expedia.bookings.onboarding.LeftRightFlingListener
import com.expedia.bookings.onboarding.adapter.OnboardingPagerAdapter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.widget.DisableableViewPager
import com.squareup.phrase.Phrase


class OnboardingActivity: AppCompatActivity() {

    private val title: TextView by lazy {
        findViewById(R.id.title_onboarding) as TextView
    }
    private val subtitle: TextView by lazy {
        findViewById(R.id.subtitle_onboarding) as TextView
    }
    private val previousButton: ImageView by lazy {
        findViewById(R.id.button_previous) as ImageView
    }
    private val nextButton: ImageView by lazy {
        findViewById(R.id.button_next) as ImageView
    }
    private val finalButton: Button by lazy {
        findViewById(R.id.button_final) as Button
    }
    private val viewPager: DisableableViewPager by lazy {
        findViewById(R.id.pager_onboarding) as DisableableViewPager
    }
    private val circles: List<View> by lazy {
        val circles = arrayListOf(
            findViewById(R.id.onboarding_first_circle) as View,
            findViewById(R.id.onboarding_second_circle) as View,
            findViewById(R.id.onboarding_third_circle) as View
        )
        circles
    }

    private val pagerAdapter: OnboardingPagerAdapter = OnboardingPagerAdapter(this)
    private val flingListener: LeftRightFlingListener = LeftRightFlingListener()
    private val gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(this, flingListener)
    }

    var isAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager.adapter = pagerAdapter
        viewPager.setPageSwipingEnabled(false)
        viewPager.addOnPageChangeListener(onPageSelectedListener)

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

        if (position == 0) {
            animateTitle(R.anim.fade_in)
        }
        else {
            animateTitle(R.anim.slide_in_right)
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
            fadeOutTitleThenGoToNewPage(FlingType.RIGHT_FLING)
        }
    }

    private fun showPrevious() {
        if (!isAnimating && viewPager.currentItem > 0) {
            fadeOutTitleThenGoToNewPage(FlingType.LEFT_FLING)
        }
    }

    fun animateTitle(animResTd: Int) {
        val animateView1 = AnimationUtils.loadAnimation(this, animResTd)
        animateView1.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        val animateView2 = AnimationUtils.loadAnimation(this, animResTd)
        animateView2.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        animateView2.startOffset = this.resources.getInteger(R.integer.onboarding_text_animation_delay).toLong()

        animateView2.setAnimationListener(object : AnimationListenerAdapter(){
            override fun onAnimationStart(animation: Animation?) {
                super.onAnimationStart(animation)
                isAnimating = true
            }
            override fun onAnimationEnd(animation: Animation?) {
                super.onAnimationEnd(animation)
                isAnimating = false
            }
        })
        subtitle.startAnimation(animateView1)
        title.startAnimation(animateView2)
    }


    fun fadeOutTitleThenGoToNewPage(flingtype: FlingType) {
        val animateView1 = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animateView1.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        val animateView2 = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animateView2.duration = this.resources.getInteger(R.integer.onboarding_text_animation_duration).toLong()
        animateView2.startOffset = this.resources.getInteger(R.integer.onboarding_text_animation_delay).toLong()

        animateView2.setAnimationListener(object : AnimationListenerAdapter(){
            override fun onAnimationStart(animation: Animation?) {
                super.onAnimationStart(animation)
                isAnimating = true
            }
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

    enum class FlingType {
        RIGHT_FLING,
        LEFT_FLING,
    }
}
