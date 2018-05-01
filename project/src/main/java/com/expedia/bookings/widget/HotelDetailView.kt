package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ScrollView
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.activity.HotelGalleryActivity
import com.expedia.bookings.hotel.activity.HotelGalleryGridActivity
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.hotel.data.HotelGalleryAnalyticsData
import com.expedia.bookings.hotel.data.HotelGalleryConfig
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.widget.HotelDetailContentView
import com.expedia.bookings.hotel.widget.HotelDetailGalleryView
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelInfoToolbarViewModel
import io.reactivex.Observable
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

    val bottomButtonWidget: HotelBottomButtonWidget by bindView(R.id.bottom_button_widget)
    private val gradientHeight = context.resources.getDimension(R.dimen.hotel_detail_gradient_height)

    private val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)

    private val detailContainer: ScrollView by bindView(R.id.detail_container)
    private var galleryExpanded = false
    private val hotelInfoToolbarViewModel = HotelInfoToolbarViewModel(context)

    private var statusBarHeight = 0
    private var toolbarHeightOffset: Float by Delegates.notNull()
    private var resortInAnimator: ObjectAnimator by Delegates.notNull()
    private var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonInAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonOutAnimator: ObjectAnimator by Delegates.notNull()

    private var alreadyTrackedGalleryClick = false

    var viewmodel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        resortFeeWidget.feeDescriptionText.setText(vm.getResortFeeText())
        resortFeeWidget.feesIncludedNotIncluded.visibility = if (vm.showFeesIncludedNotIncluded()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.visibility = if (vm.showFeeType()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.setText(vm.getFeeTypeText())

        vm.hotelOffersSubject.subscribe { hotelOffersResponse ->
            hotelInfoToolbarViewModel.bind(hotelOffersResponse)
        }

        Observable.merge(vm.hotelSoldOut, vm.isDatelessObservable).subscribe {
            when {
                vm.isDatelessObservable.value == true -> bottomButtonWidget.showSelectDates()
                vm.hotelSoldOut.value == true -> bottomButtonWidget.showChangeDates()
                else -> bottomButtonWidget.showSelectRoom()
            }
        }

        bottomButtonWidget.changeDatesClickedSubject.subscribe {
            if (vm.isChangeDatesEnabled()) {
                contentView.showChangeDatesDialog()
            } else {
                vm.returnToSearchSubject.onNext(Unit)
            }
        }

        vm.galleryObservable.subscribe { galleryMediaList ->
            galleryView.setGalleryItems(galleryMediaList)
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
        setViewVisibilities()
    }

    init {
        View.inflate(getContext(), R.layout.hotel_detail_view, this)
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        Ui.showTransparentStatusBar(getContext())
        hotelDetailsToolbar.setHotelDetailViewModel(hotelInfoToolbarViewModel)

        toolbarHeightOffset = statusBarHeight.toFloat() + toolBarHeight
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener {
            (getContext() as Activity).onBackPressed()
        }

        galleryView.galleryClickedSubject.subscribe {
            if (!alreadyTrackedGalleryClick) {
                viewmodel.trackHotelDetailGalleryClick()
                alreadyTrackedGalleryClick = true
            }

            var intent = Intent(context, HotelGalleryActivity::class.java)
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelImageGrid)) {
                intent = Intent(context, HotelGalleryGridActivity::class.java)
            }
            val galleryConfig = HotelGalleryConfig(viewmodel.hotelNameObservable.value,
                    viewmodel.hotelRatingObservable.value,
                    roomCode = DEFAULT_HOTEL_GALLERY_CODE,
                    showDescription = true, startIndex = galleryView.getCurrentIndex())
            intent.putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig)
            val analyticsData = HotelGalleryAnalyticsData(System.currentTimeMillis(), viewmodel.hotelOffersResponse.isPackage, viewmodel.hotelOffersResponse.hotelId)
            intent.putExtra(HotelExtras.GALLERY_ANALYTICS_DATA, analyticsData)
            val bundle = ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
            context.startActivity(intent, bundle)
        }

        bottomButtonWidget.selectRoomClickedSubject.subscribe {
            scrollToRoom(true)
            trackSelectRoomClick(isStickyButton = true)
        }

        bottomButtonWidget.selectDatesClickedSubject.subscribe {
            contentView.showChangeDatesDialog()
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

    fun resetViews() {
        detailContainer.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
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
        detailContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        bottomButtonWidget.translationY = 0f
        detailContainer.post {
            detailContainer.scrollTo(0, 0)
        }
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
        val scrollToAmount = contentView.getRoomContainerScrollPosition() - toolbarHeightOffset + detailContainer.scrollY
        val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.scrollY, scrollToAmount.toInt())

        smoothScrollAnimation.duration = if (animate) SELECT_ROOM_ANIMATION else 0
        smoothScrollAnimation.interpolator = (AccelerateDecelerateInterpolator())
        smoothScrollAnimation.addUpdateListener({ animation ->
            val scrollTo = animation.animatedValue as Int
            detailContainer.scrollTo(0, scrollTo)
        })

        smoothScrollAnimation.start()

        contentView.focusRoomsForAlly()
    }

    private fun setViewVisibilities() {
        val yoffset = detailContainer.scrollY

        hotelDetailsToolbar.toolBarBackground.alpha = AlphaCalculator.fadeInAlpha(startPoint = toolbarHeightOffset,
                endPoint = toolbarHeightOffset / 2, currentPoint = contentView.getPriceContainerYScreenLocation().toFloat())
        contentView.handleScrollWithOffset(yoffset, toolbarHeightOffset)

        showToolbarGradient()
        val shouldShowResortFee = shouldShowResortView()
        if (shouldShowResortFee && !resortInAnimator.isRunning && resortFeeWidget.translationY != 0f) {
            resortFeeWidget.visibility = View.VISIBLE
            resortInAnimator.start()
        } else if (!shouldShowResortFee && !resortOutAnimator.isRunning && resortFeeWidget.translationY != resortViewHeight.toFloat()) {
            resortOutAnimator.start()
        }

        if (!bottomButtonInAnimator.isRunning && bottomButtonWidget.translationY != 0f && !areRoomsVisible()
                && !viewmodel.hotelSoldOut.value && viewmodel.isDatelessObservable.value != true && !galleryExpanded) {
            bottomButtonInAnimator.start()
        } else if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat()
                && (areRoomsVisible() && !viewmodel.hotelSoldOut.value && viewmodel.isDatelessObservable.value != true || galleryExpanded)) {
            bottomButtonOutAnimator.start()
        }
    }

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
