package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.widget.NestedScrollView
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionListenerAdapter
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.widget.HotelDetailContentView
import com.expedia.bookings.hotel.widget.HotelDetailGalleryView
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.facebook.stetho.common.LogUtil
import kotlin.properties.Delegates

val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val ANIMATION_DURATION = 200L
    private val SELECT_ROOM_ANIMATION = 300L
    private var resortViewHeight = 0
    private var bottomButtonContainerHeight = 0
    private val screenSize by lazy { Ui.getScreenSize(context) }

    val hotelDetailsToolbar: HotelDetailsToolbar by bindView(R.id.hotel_details_toolbar)
    private var toolBarHeight = 0

    val galleryView: HotelDetailGalleryView by bindView(R.id.detail_hotel_gallery)

    private val contentView: HotelDetailContentView by bindView(R.id.hotel_detail_content_view)

    private val nestedScrollView: NestedScrollView by bindView(R.id.nested_scrollview)

    val bottomButtonWidget: HotelBottomButtonWidget by bindView(R.id.bottom_button_widget)
    private val gradientHeight = context.resources.getDimension(R.dimen.hotel_detail_gradient_height)

    private val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)

//    private val detailContainer: ScrollView by bindView(R.id.detail_container)
    private val coordinatorLayout: CoordinatorLayout by bindView(R.id.content_constraint_container)
    private var galleryExpanded = false

    private var statusBarHeight = 0
    private var toolbarHeightOffset: Float by Delegates.notNull()
    private var resortInAnimator: ObjectAnimator by Delegates.notNull()
    private var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonInAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonOutAnimator: ObjectAnimator by Delegates.notNull()
    private var behavior: BottomSheetBehavior<NestedScrollView>

    private var alreadyTrackedGalleryClick = false

    var viewmodel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        resortFeeWidget.feeDescriptionText.setText(vm.getResortFeeText())
        resortFeeWidget.feesIncludedNotIncluded.visibility = if (vm.showFeesIncludedNotIncluded()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.visibility = if (vm.showFeeType()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.setText(vm.getFeeTypeText())

        vm.hotelOffersSubject.subscribe {
            hotelDetailsToolbar.setHotelDetailViewModel(HotelDetailViewModel.convertToToolbarViewModel(vm))
        }

        vm.hotelSoldOut.subscribe { soldOut ->
            galleryView.updateSoldOut(soldOut)
            if (soldOut) {
                bottomButtonWidget.showChangeDates()
            } else {
                bottomButtonWidget.showSelectRoom()
            }
        }
        bottomButtonWidget.changeDatesClickedSubject.subscribe {
            if (vm.isChangeDatesEnabled()) {
                contentView.showChangeDatesDialog()
            } else {
                vm.returnToSearchSubject.onNext(Unit)
            }
        }

        vm.galleryObservable.subscribe { galleryUrls ->
            galleryView.setGalleryItems(galleryUrls)
        }

        vm.scrollToRoom.subscribe { scrollToRoom(false) }

        vm.hotelResortFeeObservable.subscribeText(resortFeeWidget.resortFeeText)
        vm.hotelResortFeeIncludedTextObservable.subscribeText(resortFeeWidget.feesIncludedNotIncluded)

        vm.payByPhoneContainerVisibility.subscribe {
            contentView.updateSpacer(getStickyRoomSizeMinusShadow())
        }

        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)

        contentView.viewModel = vm
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
//        setViewVisibilities()
    }

    init {
        View.inflate(getContext(), R.layout.hotel_detail_view_collapsed_gallery, this)
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        Ui.showTransparentStatusBar(getContext())

        behavior = BottomSheetBehavior.from(nestedScrollView)

        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when(newState){
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        collapseGallery()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        collapseGallery()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        expandGallery()
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })

        toolbarHeightOffset = statusBarHeight.toFloat() + toolBarHeight
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener { view ->
            if (hotelDetailsToolbar.navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
                collapseGallery()
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else
                (getContext() as Activity).onBackPressed()
        }

        galleryView.galleryClickedSubject.subscribe {
            if (!alreadyTrackedGalleryClick) {
                viewmodel.trackHotelDetailGalleryClick()
                alreadyTrackedGalleryClick = true
            }
            if (galleryExpanded) {
                collapseGallery()
            } else {
                expandGallery()
            }
        }

        bottomButtonWidget.selectRoomClickedSubject.subscribe {
            scrollToRoom(true)
            trackSelectRoomClick(isStickyButton = true)
        }
        contentView.requestFocusOnRoomsSubject.subscribe { scrollToRoom(true) }

        resortFeeWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        resortViewHeight = resortFeeWidget.measuredHeight
        resortInAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", resortViewHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        resortOutAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", 0f, resortViewHeight.toFloat()).setDuration(ANIMATION_DURATION)

        bottomButtonWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bottomButtonContainerHeight = bottomButtonWidget.measuredHeight
        bottomButtonInAnimator = ObjectAnimator.ofFloat(bottomButtonWidget, "translationY", bottomButtonContainerHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        bottomButtonOutAnimator = ObjectAnimator.ofFloat(bottomButtonWidget, "translationY", 0f, bottomButtonContainerHeight.toFloat()).setDuration(ANIMATION_DURATION)

        hideResortAndSelectRoom()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

//        galleryCollapsedConstraintSet.clone(constraintWrapper)
//        galleryFullScreenConstraintSet.clone(context, R.layout.hotel_detail_view_expanded_gallery)
    }

    fun resetViews() {
//        detailContainer.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        hotelDetailsToolbar.toolBarBackground.alpha = 0f
        hotelDetailsToolbar.toolBarGradient.translationY = 0f
        resortFeeWidget.visibility = View.GONE
        contentView.resetViews()
        contentView.updateSpacer(getStickyRoomSizeMinusShadow())
        hideResortAndSelectRoom()
        galleryView.setGalleryItems(ArrayList())
        alreadyTrackedGalleryClick = false
    }

    fun refresh() {
//        detailContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        bottomButtonWidget.translationY = 0f
//        detailContainer.post {
//            detailContainer.scrollTo(0, 0)
//        }
    }

    fun collapseGallery() {
        galleryView.collapse()
//        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        if (!bottomButtonInAnimator.isRunning && bottomButtonWidget.translationY != 0f) {
            bottomButtonInAnimator.startDelay = 100
            bottomButtonInAnimator.start()
        }
        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelThrottleGalleryAnimation)) {
            val collapseTransition = TransitionInflater.from(context).inflateTransition(R.transition.gallery_collapse_transition)
            collapseTransition.addListener(object: TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition?) {
                    galleryExpanded = false
                }
            })
            TransitionManager.beginDelayedTransition(coordinatorLayout, collapseTransition)
        } else {
            galleryExpanded = false
        }
//        galleryCollapsedConstraintSet.applyTo(coordinatorLayout)

        hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)
        hotelDetailsToolbar.navIcon.parameter = 0f
    }

    private fun expandGallery() {
        galleryView.expand()
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat()) {
            bottomButtonOutAnimator.start()
        }
        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelThrottleGalleryAnimation)) {
            val expandTransition = TransitionInflater.from(context).inflateTransition(R.transition.gallery_expand_transition)
            expandTransition.addListener(object: TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition?) {
                    galleryExpanded = true
                }
            })
            TransitionManager.beginDelayedTransition(coordinatorLayout, expandTransition)
        } else {
            galleryExpanded = true
        }
//        galleryFullScreenConstraintSet.applyTo(constraintWrapper)

        hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_gallery_cont_desc)
        hotelDetailsToolbar.navIcon.parameter = 1f
    }

    private fun showToolbarGradient() {
        val priceLocationY = contentView.getPriceContainerYScreenLocation()
        if (priceLocationY < gradientHeight) {
            hotelDetailsToolbar.toolBarGradient.translationY = (-(gradientHeight - priceLocationY))
        } else {
            hotelDetailsToolbar.toolBarGradient.translationY = 0f
        }
    }

    private fun shouldShowResortView(): Boolean {
        return viewmodel.hotelResortFeeObservable.value.isNotEmpty()
                && contentView.isRoomContainerInBounds((screenSize.y / 2).toFloat(), toolbarHeightOffset)
    }

    private fun hideResortAndSelectRoom() {
        val activity = context as Activity
        if (!activity.intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            resortFeeWidget.animate().translationY(resortViewHeight.toFloat()).setInterpolator(LinearInterpolator()).setDuration(ANIMATION_DURATION).start()
        } else {
            resortFeeWidget.translationY = resortViewHeight.toFloat()
        }
    }

    private fun scrollToRoom(animate: Boolean) {
//        val scrollToAmount = contentView.getRoomContainerScrollPosition() - toolbarHeightOffset + detailContainer.scrollY
//        val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.scrollY, scrollToAmount.toInt())
//
//        smoothScrollAnimation.duration = if (animate) SELECT_ROOM_ANIMATION else 0
//        smoothScrollAnimation.interpolator = (AccelerateDecelerateInterpolator())
//        smoothScrollAnimation.addUpdateListener({ animation ->
//            val scrollTo = animation.animatedValue as Int
//            detailContainer.scrollTo(0, scrollTo)
//        })
//
//        smoothScrollAnimation.start()
//
//        contentView.focusRoomsForAlly()
    }

//    private fun setViewVisibilities() {
//        val yoffset = detailContainer.scrollY
//
//        hotelDetailsToolbar.toolBarBackground.alpha = AlphaCalculator.fadeInAlpha(startPoint = toolbarHeightOffset,
//                endPoint = toolbarHeightOffset / 2, currentPoint = contentView.getPriceContainerYScreenLocation().toFloat())
//        contentView.handleScrollWithOffset(yoffset, toolbarHeightOffset)
//
//        showToolbarGradient()
//        val shouldShowResortFee = shouldShowResortView()
//        if (shouldShowResortFee && !resortInAnimator.isRunning && resortFeeWidget.translationY != 0f) {
//            resortFeeWidget.visibility = View.VISIBLE
//            resortInAnimator.start()
//        } else if (!shouldShowResortFee && !resortOutAnimator.isRunning && resortFeeWidget.translationY != resortViewHeight.toFloat()) {
//            resortOutAnimator.start()
//        }
//
//        if (!bottomButtonInAnimator.isRunning && bottomButtonWidget.translationY != 0f && !areRoomsVisible()
//                && !viewmodel.hotelSoldOut.value && !galleryExpanded) {
//            bottomButtonInAnimator.start()
//        } else if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat()
//                && (areRoomsVisible() && !viewmodel.hotelSoldOut.value || galleryExpanded)) {
//            bottomButtonOutAnimator.start()
//        }
//    }

    private fun areRoomsVisible(): Boolean {
        return contentView.isRoomContainerInBounds((screenSize.y / 2).toFloat(), toolbarHeightOffset)
    }

    private fun getStickyRoomSizeMinusShadow(): Int {
        return (bottomButtonWidget.measuredHeight - resources.getDimension(R.dimen.hotel_sticky_bottom_shadow_height)).toInt()
    }

    private fun trackSelectRoomClick(isStickyButton: Boolean) {
        viewmodel.trackHotelDetailSelectRoomClick(isStickyButton)
    }
}
