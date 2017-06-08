package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.hotel.widget.HotelDetailContentView
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeGalleryColorFilter
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import kotlin.properties.Delegates

val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val ANIMATION_DURATION = 200L
    private val SELECT_ROOM_ANIMATION = 300L
    private var resortViewHeight = 0
    private var bottomButtonContainerHeight = 0
    private val screenSize by lazy { Ui.getScreenSize(context) }

    private var initialScrollTop = 0

    val hotelDetailsToolbar: HotelDetailsToolbar by bindView(R.id.hotel_details_toolbar)
    private var toolBarHeight = 0

    private var galleryHeight = 0
    val gallery: HotelDetailRecyclerGallery by bindView(R.id.images_gallery)
    private val galleryContainer: FrameLayout by bindView(R.id.gallery_container)
    private val galleryRoot: LinearLayout by bindView(R.id.gallery)

    private val contentView: HotelDetailContentView by bindView(R.id.hotel_detail_content_view)

    val bottomButtonWidget: HotelBottomButtonWidget by bindView(R.id.bottom_button_widget)
    private val gradientHeight = context.resources.getDimension(R.dimen.hotel_detail_gradient_height)

    private val hotelGalleryDescriptionContainer: LinearLayout by bindView(R.id.hotel_gallery_description_container)
    private val hotelGalleryIndicator: View by bindView(R.id.hotel_gallery_indicator)
    private val hotelGalleryDescription: TextView by bindView(R.id.hotel_gallery_description)

    private val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)

    private val detailContainer: NewHotelDetailsScrollView by bindView(R.id.detail_container)
    private var statusBarHeight = 0
    private var toolbarHeightOffset: Float by Delegates.notNull()
    private var resortInAnimator: ObjectAnimator by Delegates.notNull()
    private var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonInAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonOutAnimator: ObjectAnimator by Delegates.notNull()

    var viewmodel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        resortFeeWidget.feeDescriptionText.setText(vm.getResortFeeText())
        resortFeeWidget.feesIncludedNotIncluded.visibility = if (vm.showFeesIncludedNotIncluded()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.visibility = if (vm.showFeeType()) View.VISIBLE else View.GONE
        resortFeeWidget.feeType.setText(vm.getFeeTypeText())

        detailContainer.setOnTouchListener(touchListener)
        vm.hotelOffersSubject.subscribe {
            hotelDetailsToolbar.setHotelDetailViewModel(HotelDetailViewModel.convertToToolbarViewModel(vm))
        }
        vm.galleryColorFilter.subscribeGalleryColorFilter(gallery)

        vm.hotelSoldOut.subscribe { soldOut ->
            if (soldOut) {
                bottomButtonWidget.showChangeDates()
            } else {
                bottomButtonWidget.showSelectRoom()
            }
        }
        bottomButtonWidget.changeDatesClickedSubject.subscribe(vm.changeDates)

        vm.galleryObservable.subscribe { galleryUrls ->
            gallery.setOnItemClickListener(vm)
            gallery.setOnItemChangeListener(vm)
            gallery.setDataSource(galleryUrls)
            gallery.setProgressBarOnImageViewsEnabled(true)
            gallery.scrollToPosition(0)

            val galleryItemCount = gallery.adapter.itemCount
            if (galleryItemCount > 0) {
                val indicatorWidth = screenSize.x / galleryItemCount
                val lp = hotelGalleryIndicator.layoutParams
                lp.width = indicatorWidth
                hotelGalleryIndicator.layoutParams = lp
            }
        }

        vm.hotelSoldOut.filter { it }.subscribe { resetGallery() }

        vm.scrollToRoom.subscribe { scrollToRoom(false) }

        vm.galleryItemChangeObservable.subscribe { galleryDescriptionBar: Pair<Int, String> ->
            hotelGalleryIndicator.animate().translationX((galleryDescriptionBar.first * hotelGalleryIndicator.width).toFloat()).
                    setInterpolator(LinearInterpolator()).start()
            hotelGalleryDescription.text = galleryDescriptionBar.second
        }

        vm.hotelResortFeeObservable.subscribeText(resortFeeWidget.resortFeeText)
        vm.hotelResortFeeIncludedTextObservable.subscribeText(resortFeeWidget.feesIncludedNotIncluded)

        vm.payByPhoneContainerVisibility.subscribe {
            contentView.updateSpacer(getStickyRoomSizeMinusShadow())
        }

        vm.galleryClickedSubject.subscribe {
            detailContainer.animateScrollY(detailContainer.scrollY, -initialScrollTop, 500)
        }
        
        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)

        contentView.viewModel = vm
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        setViewVisibilities()
    }

    private val touchListener = View.OnTouchListener { v, event ->
        val action = event.action
        if (action == MotionEvent.ACTION_UP) {
            detailContainer.post { updateGallery(true) }
        }
        false
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_detail, this)
        gallery.addImageViewCreatedListener({ index -> updateGalleryChildrenHeights(index) })
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        Ui.showTransparentStatusBar(getContext())

        toolbarHeightOffset = statusBarHeight.toFloat() + toolBarHeight
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener { view ->
            if (hotelDetailsToolbar.navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
                updateGallery(false)
            } else
                (getContext() as Activity).onBackPressed()
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

    fun resetViews() {
        detailContainer.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        hotelDetailsToolbar.toolBarBackground.alpha = 0f
        hotelDetailsToolbar.toolBarGradient.translationY = 0f
        hotelGalleryDescriptionContainer.alpha = 0f
        resortFeeWidget.visibility = View.GONE
        contentView.resetViews()
        contentView.updateSpacer(getStickyRoomSizeMinusShadow())
        hideResortAndSelectRoom()
        viewmodel.onGalleryItemScrolled(0)
        gallery.setDataSource(emptyList())
    }

    fun refresh() {
        detailContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        resetGallery()
        bottomButtonWidget.translationY = 0f
    }

    fun updateGallery(toFullScreen: Boolean) {
        if (detailContainer.isFlinging) {
            return
        }

        val fromY = detailContainer.scrollY
        val threshold = initialScrollTop / 2
        //In case of slow scrolling, if gallery view is expanding more than halfway then scrollTo full screen else scrollTo initialScroollTop
        if ((toFullScreen && fromY > threshold && fromY < initialScrollTop) || (!toFullScreen)) {
            detailContainer.animateScrollY(fromY, initialScrollTop, ANIMATION_DURATION)
        } else if (fromY < threshold) {
            detailContainer.animateScrollY(fromY, 0, ANIMATION_DURATION)
        }
    }

    private fun resetGallery() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                val lp = galleryContainer.layoutParams
                lp.height = height
                galleryContainer.layoutParams = lp
                galleryHeight = resources.getDimensionPixelSize(R.dimen.gallery_height)
                initialScrollTop = height - galleryHeight

                detailContainer.post {
                    detailContainer.scrollTo(0, initialScrollTop)
                    gallery.scrollToPosition(0)
                    showToolbarGradient()
                }
            }
        })
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
        return viewmodel.hotelResortFeeObservable.value != null
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
        var scrollToAmount = contentView.getRoomContainerScrollPosition() - toolbarHeightOffset + detailContainer.scrollY
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

    companion object {
        val zeroSaturationColorMatrixColorFilter: ColorMatrixColorFilter by lazy {
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f)
            ColorMatrixColorFilter(colorMatrix)
        }
    }

    private var previousYOffset = 0

    private fun setViewVisibilities() {
        var yoffset = detailContainer.scrollY

        updateGalleryChildrenHeights(gallery.selectedItem)
        if (yoffset - initialScrollTop >= 0) {
            galleryRoot.translationY = (yoffset - initialScrollTop) * 0.5f
        } else {
            galleryRoot.translationY = 0f
        }
        var galleryExpanded = false
        // Hotel gallery collapsed
        if (yoffset == initialScrollTop) {
            (gallery.layoutManager as RecyclerGallery.A11yLinearLayoutManager).setCanA11yScroll(false)

            hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)
            gallery.prepareCollapseState(true)
        }
        // Hotel gallery expanded
        if (yoffset == 0) {
            galleryExpanded = true
            (gallery.layoutManager as RecyclerGallery.A11yLinearLayoutManager).setCanA11yScroll(true)

            hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_gallery_cont_desc)
            gallery.prepareCollapseState(false)
        }

        if (previousYOffset == 0 && yoffset >= 10) {
            detailContainer.stopNestedScroll()
            detailContainer.smoothScrollTo(0, initialScrollTop)
        }

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
                && !viewmodel.hotelSoldOut.value && !galleryExpanded) {
            bottomButtonInAnimator.start()
        } else if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat()
                && (areRoomsVisible() && !viewmodel.hotelSoldOut.value || galleryExpanded)) {
            bottomButtonOutAnimator.start()
        }

        val arrowRatio = getArrowRotationRatio(yoffset)
        if (arrowRatio >= 0 && arrowRatio <= 1) {
            hotelDetailsToolbar.navIcon.parameter = 1 - arrowRatio
            hotelGalleryDescriptionContainer.alpha = 1 - arrowRatio
        }

        previousYOffset = yoffset
    }

    private fun getArrowRotationRatio(scrollY: Int): Float {
        return scrollY.toFloat() / (initialScrollTop)
    }

    private fun updateGalleryChildrenHeights(index: Int) {
        resizeImageViews(index)
        resizeImageViews(index - 1)
        resizeImageViews(index + 1)
    }

    private fun resizeImageViews(index: Int) {
        if (index >= 0 && index < gallery.adapter.itemCount) {
            var holder = gallery.findViewHolderForAdapterPosition(index)
            if (holder != null) {
                holder = holder as RecyclerGallery.RecyclerAdapter.GalleryViewHolder
                holder.mImageView?.setIntermediateValue(height - initialScrollTop, height,
                        detailContainer.scrollY.toFloat() / initialScrollTop)
            }
        }
    }

    private fun areRoomsVisible() : Boolean {
        return contentView.isRoomContainerInBounds((screenSize.y / 2).toFloat(), toolbarHeightOffset)
    }

    private fun getStickyRoomSizeMinusShadow(): Int {
        return (bottomButtonWidget.measuredHeight - resources.getDimension(R.dimen.hotel_sticky_bottom_shadow_height)).toInt()
    }

    private fun trackSelectRoomClick(isStickyButton: Boolean) {
        viewmodel.trackHotelDetailSelectRoomClick(isStickyButton)
    }
}
