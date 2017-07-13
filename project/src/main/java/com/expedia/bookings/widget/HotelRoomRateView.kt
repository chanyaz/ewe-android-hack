package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.LoyaltyUtil
import com.expedia.util.notNullAndObservable
import com.expedia.util.setTextAndVisibility
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelRoomRateViewModel
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelRoomRateView(context: Context) : LinearLayout(context) {

    //views for room row
    val row: ViewGroup by bindView(R.id.root)

    val roomHeaderImageContainer: FrameLayout by bindView(R.id.room_header_image_container)
    val roomHeaderImage: ImageView by bindView(R.id.room_header_image)
    val roomInfoChevron: ImageView by bindView(R.id.room_info_chevron)
    val roomInfoHeader: TextView by bindView(R.id.room_info_header_text)
    val roomInfoDivider: View by bindView(R.id.room_info_divider)
    val hotelRoomRateActionButton: HotelRoomRateActionButton by bindView(R.id.hotel_room_row_button)

    var roomInfoHeaderTextHeight = -1
    var roomHeaderImageHeight = -1
    var roomInfoDividerHeight = -1
    var roomInfoDescriptionTextHeight = -1
    var roomInfoChevronHeight = -1
    val animateRoom = PublishSubject.create<Pair<HotelRoomRateView, Boolean>>()

    var expandedMeasurementsDone = false
    private var showTerms = false
    private val ANIMATION_DURATION = 250L
    private val NO_ANIMATION_DURATION = 0L
    private var viewsToHideInExpandedState: Array<View> by Delegates.notNull()
    private var viewsToShowInExpandedState: Array<View> by Delegates.notNull()

    private val roomType: TextView by bindView(R.id.room_type_text_view)
    private val collapsedContainer: RelativeLayout by bindView(R.id.collapsed_container)
    private val collapsedEarnMessaging: TextView by bindView(R.id.collapsed_earn_message_text_view)
    private val collapsedBedType: TextView by bindView(R.id.collapsed_bed_type_text_view)
    private val collapsedUrgency: TextView by bindView(R.id.collapsed_urgency_text_view)

    private val expandedBedType: TextView by bindView(R.id.expanded_bed_type_text_view)
    private val expandedAmenity: TextView by bindView(R.id.expanded_amenity_text_view)
    private val depositTermsButton: TextView by bindView(R.id.deposit_terms_buttons)

    private val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    private val dailyPricePerNight: TextView by bindView(R.id.daily_price_per_night)
    private val perNight: TextView by bindView(R.id.per_night)

    private val roomDiscountPercentage: TextView by bindView(R.id.discount_percentage)
    private val roomInfoContainer: RelativeLayout by bindView(R.id.room_info_container)
    private val roomInfoDescriptionText: TextView by bindView(R.id.room_info_description_text)

    private val freeCancellation: TextView by bindView(R.id.expanded_free_cancellation_text_view)
    private val roomDivider: View by bindView(R.id.row_divider)

    private val collapsedMandatoryFee: TextView by bindView(R.id.collapsed_mandatory_fee_text_view)
    private val expandedMandatoryFee: TextView by bindView(R.id.expanded_mandatory_fee_text_view)

    private val roomContainerTopBottomPadding = getDimensionInDp(12f)
    private val roomContainerLeftRightPadding = getDimensionInDp(15f)


    var viewModel: HotelRoomRateViewModel by notNullAndObservable { vm ->
        if (viewModel.lob == LineOfBusiness.PACKAGES) {
            hotelRoomRateActionButton.setSelectButtonText(context.getString(R.string.select))
            hotelRoomRateActionButton.showViewRoomButton()
        }

        vm.collapsedEarnMessageVisibilityObservable.subscribe {
            viewsToHideInExpandedState = arrayOf(collapsedBedType, collapsedUrgency, collapsedMandatoryFee)
            viewsToShowInExpandedState = arrayOf(expandedBedType, expandedAmenity, freeCancellation, expandedMandatoryFee, strikeThroughPrice)
        }
        vm.collapsedUrgencyVisibilityObservable.subscribeVisibility(collapsedUrgency)
        vm.collapsedEarnMessageVisibilityObservable.subscribeVisibility(collapsedEarnMessaging)

        expandedAmenity.visibility = View.GONE
        hotelRoomRateActionButton.viewRoomClickedSubject.subscribe {
            expandRowWithAnimation()
            vm.roomRowExpanded()
        }

        hotelRoomRateActionButton.bookButtonClickedSubject.subscribe {
            vm.bookRoomClicked()
        }

        vm.roomSoldOut.subscribe { soldOut ->
            row.setOnClickListener(if (soldOut) null else rowClickListener)

            if (soldOut) {
                hotelRoomRateActionButton.showSoldOutButton()
            }
            else {
                hotelRoomRateActionButton.hideSoldOutButton()
            }
        }

        row.setOnClickListener(rowClickListener)

        roomInfoContainer.setOnClickListener {
            if (roomInfoDescriptionText.visibility == View.GONE) {
                expandRoomInformation()
            } else {
                collapseRoomInformation()
            }
        }

        roomInfoContainer.setAccessibilityDelegate(object: AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
               super.onInitializeAccessibilityNodeInfo(host, info)
                val description: String
                if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                    description = context.resources.getString(R.string.collapse_room_info_cont_desc)
                } else {
                    description = context.resources.getString(R.string.expand_room_info_cont_desc)
                }
                val customClick = AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, description)
                info?.addAction(customClick)
            }
        })

        depositTermsButton.subscribeOnClick(vm.depositInfoContainerClick)

        vm.roomRateInfoTextObservable.subscribeText(roomInfoDescriptionText)
        vm.roomTypeObservable.subscribeText(roomType)
        vm.discountPercentage.subscribeText(roomDiscountPercentage)
        vm.shouldShowDiscountPercentage.subscribeVisibility(roomDiscountPercentage)
        vm.collapsedBedTypeObservable.subscribeText(collapsedBedType)
        vm.expandedBedTypeObservable.subscribeText(expandedBedType)
        vm.perNightPriceVisibleObservable.map { it && !vm.onlyShowTotalPrice.value }.subscribeVisibility(perNight)
        vm.expandedAmenityObservable.subscribe { text ->
            expandedAmenity.setTextAndVisibility(text)
        }
        vm.collapsedUrgencyObservable.subscribeText(collapsedUrgency)
        vm.collapsedEarnMessageObservable.subscribeText(collapsedEarnMessaging)

        vm.expandedMessageObservable.subscribe { expandedMessagePair ->
            val drawable = ContextCompat.getDrawable(context, expandedMessagePair.second)
            freeCancellation.text = expandedMessagePair.first
            freeCancellation.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
        vm.dailyPricePerNightObservable.subscribeTextAndVisibility(dailyPricePerNight)
        vm.roomInfoVisibilityObservable.subscribeVisibility(roomInfoContainer)
        vm.roomInfoVisibilityObservable.subscribeVisibility(roomInfoDivider)
        var isShopWithPoints = LoyaltyUtil.isShopWithPoints(viewModel.hotelRate)
        var isAirAttached = vm.hotelRate.isShowAirAttached()
        if (isShopWithPoints || !isAirAttached && !Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)) {
            vm.strikeThroughPriceObservable.subscribeTextAndVisibility(strikeThroughPrice)
        }
        vm.depositTerms.subscribe {
            val depositTerms = it
            showTerms = depositTerms?.isNotEmpty() ?: false
        }

        vm.expandRoomObservable.subscribe {
           expandRowNoAnimation()
        }

        vm.collapseRoomObservable.subscribe {
            collapseRow(NO_ANIMATION_DURATION)
        }
        vm.collapseRoomWithAnimationObservable.subscribe {
            collapseRow(ANIMATION_DURATION)
        }

        vm.dailyMandatoryFeeMessageObservable.subscribe { mandatoryFeeMessage ->
            expandedMandatoryFee.text = mandatoryFeeMessage
            collapsedMandatoryFee.text = mandatoryFeeMessage

            if (!mandatoryFeeMessage.isNullOrBlank()) {
                expandedMandatoryFee.visibility = View.VISIBLE
                collapsedMandatoryFee.visibility = View.VISIBLE
            } else {
                expandedMandatoryFee.visibility = View.GONE
                collapsedMandatoryFee.visibility = View.GONE
            }
        }

        vm.viewRoomButtonContentDescriptionObservable.subscribeContentDescription(hotelRoomRateActionButton.viewRoomButton)
        vm.bookButtonContentDescriptionObservable.subscribeContentDescription(hotelRoomRateActionButton.bookButton)
    }

    val rowClickListener = View.OnClickListener { view ->
        expandRowWithAnimation()
        viewModel.roomRowExpanded()
    }

    init {
        View.inflate(getContext(), R.layout.hotel_room_row, this)
        orientation = LinearLayout.VERTICAL

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                roomHeaderImageHeight = roomHeaderImage.height
                roomInfoHeaderTextHeight = roomInfoHeader.height
                roomInfoDividerHeight = roomInfoDivider.height
                roomInfoDescriptionTextHeight = roomInfoDescriptionText.height
                roomInfoChevronHeight = roomInfoChevron.height

                roomInfoDescriptionText.visibility = View.GONE
                row.viewTreeObserver.removeOnGlobalLayoutListener(this)

                expandedMeasurementsDone = true
                collapseRow(NO_ANIMATION_DURATION)
            }
        }
        row.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        viewSetup()
    }

    private fun viewSetup() {
        val transitionDrawable = TransitionDrawable(arrayOf(ColorDrawable(Color.parseColor("#00000000")), ContextCompat.getDrawable(context, R.drawable.card_background)))
        transitionDrawable.isCrossFadeEnabled = true
        hotelRoomRateActionButton.showViewRoomButton()

        row.background = transitionDrawable

        roomInfoDescriptionText.visibility = View.VISIBLE

        if (ExpediaBookingApp.isDeviceShitty()) {
            roomHeaderImage.visibility = View.GONE
        } else {
            roomHeaderImage.visibility = View.VISIBLE
        }
    }

    private fun expandRowWithAnimation() {
        expandRow(ANIMATION_DURATION)
        animateRoom.onNext(Pair(this, true))
    }

    private fun expandRowNoAnimation() {
        expandRow(NO_ANIMATION_DURATION)
        animateRoom.onNext(Pair(this, false))
    }

    private fun expandRow(animationDuration: Long) {
        if (!viewModel.roomSoldOut.value && expandedMeasurementsDone) {
            viewModel.rowExpanding.onNext(viewModel.rowIndex)

            hotelRoomRateActionButton.showBookButton()

            val imageUrl: String? = viewModel.roomHeaderImageObservable.value
            if (ExpediaBookingApp.isDeviceShitty()) {
                //ignore dont load image
            } else if (imageUrl != null && imageUrl.isNotBlank()) {
                val hotelMedia = HotelMedia(imageUrl)
                PicassoHelper.Builder(roomHeaderImage)
                        .setPlaceholder(R.drawable.room_fallback)
                        .build()
                        .load(hotelMedia.getBestUrls(width / 2))
            } else {
                roomHeaderImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.room_fallback))
            }

            viewsToHideInExpandedState.forEach {
                val alphaAnimation = newAlphaOneToZeroAnimation(it)
                alphaAnimation.duration = animationDuration
                it.startAnimation(alphaAnimation)
            }
            collapsedBedType.visibility = View.GONE
            collapsedUrgency.visibility = View.GONE
            collapsedMandatoryFee.visibility = View.GONE

            viewsToShowInExpandedState.forEach {
                if (it is TextView && it.text.isEmpty()) {
                    it.visibility = View.GONE
                } else {
                    val alphaAnimation = newAlphaZeroToOneAnimation(it)
                    alphaAnimation.duration = animationDuration
                    it.startAnimation(alphaAnimation)
                }
            }

            roomInfoContainer.setPadding(roomContainerLeftRightPadding, roomContainerTopBottomPadding,
                    roomContainerLeftRightPadding, roomContainerTopBottomPadding)

            row.setOnClickListener(null)

            val infoIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.details_info).mutate()
            infoIcon.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
            depositTermsButton.setCompoundDrawablesWithIntrinsicBounds(infoIcon, null, null, null)

            depositTermsButton.visibility = if (showTerms) View.VISIBLE else View.GONE
            if (showTerms) {
                dailyPricePerNight.visibility = View.GONE
            }
            collapsedContainer.setBackgroundColor(Color.WHITE)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            dailyPricePerNight.setTextColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            perNight.setTextColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))

            (row.background as TransitionDrawable).startTransition(animationDuration.toInt())

            topMarginForView(row, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
            topMarginForView(roomDivider, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
        }
    }

    private fun collapseRow(animationDuration: Long) {
        if (expandedMeasurementsDone) {
            hotelRoomRateActionButton.showViewRoomButton()
            viewsToHideInExpandedState.forEach {
                if (it is TextView && it.text.isEmpty()) {
                    it.visibility = View.GONE
                } else {
                    val alphaAnimation = newAlphaZeroToOneAnimation(it)
                    alphaAnimation.duration = animationDuration
                    it.startAnimation(alphaAnimation)
                }
            }
            viewsToShowInExpandedState.forEach {
                val alphaAnimation = newAlphaOneToZeroAnimation(it)
                alphaAnimation.duration = animationDuration
                it.startAnimation(alphaAnimation)
            }

            expandedBedType.visibility = View.GONE
            freeCancellation.visibility = View.GONE
            expandedMandatoryFee.visibility = View.GONE

            row.setOnClickListener(if (viewModel.roomSoldOut.value) null else rowClickListener)

            depositTermsButton.visibility = View.GONE
            if (showTerms) {
                dailyPricePerNight.visibility = View.VISIBLE
            }
            collapsedContainer.background = ContextCompat.getDrawable(context, R.drawable.gray_background_ripple)

            roomInfoContainer.setPadding(0, 0, 0, 0)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            dailyPricePerNight.setTextColor(ContextCompat.getColor(context, R.color.hotel_cell_disabled_text))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            perNight.setTextColor(ContextCompat.getColor(context, R.color.hotel_cell_disabled_text))

            (row.background as TransitionDrawable).reverseTransition(animationDuration.toInt())

            val resizeAnimator = ResizeHeightAnimator(animationDuration)
            resizeAnimator.addViewSpec(roomHeaderImageContainer, 0)
            resizeAnimator.addViewSpec(roomInfoHeader, 0)
            resizeAnimator.addViewSpec(roomInfoDivider, 0)
            resizeAnimator.addViewSpec(roomInfoChevron, 0)
            if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                resizeAnimator.addViewSpec(roomInfoDescriptionText, 0)
            }

            topMarginForView(row, 0)
            topMarginForView(roomDivider, 0)

            resizeAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(p0: Animator?) {
                    recycleImageView(roomHeaderImage)
                    if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                        collapseRoomInformation()
                    }
                }
            })
            resizeAnimator.start()
        }
    }

    private fun expandRoomInformation() {
        if (expandedMeasurementsDone) {
            val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams

            roomInfoDescriptionText.visibility = View.VISIBLE
            lp.addRule(RelativeLayout.BELOW, R.id.room_info_description_text)
            roomInfoChevron.layoutParams = lp
            resizeAnimator.addViewSpec(roomInfoDescriptionText, roomInfoDescriptionTextHeight, 0)
            resizeAnimator.start()
            AnimUtils.rotate(roomInfoChevron)
            //track only when expand the room info
            if (viewModel.lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelRoomMoreInfoClick()
            } else {
                HotelTracking.trackLinkHotelRoomInfoClick()
            }

            roomInfoContainer.contentDescription = roomInfoDescriptionText.text
        }
    }

    private fun collapseRoomInformation() {
        if (expandedMeasurementsDone) {
            val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams

            lp.addRule(RelativeLayout.BELOW, 0)
            roomInfoChevron.layoutParams = lp
            resizeAnimator.addViewSpec(roomInfoDescriptionText, 0)
            resizeAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(p0: Animator?) {
                    roomInfoDescriptionText.visibility = View.GONE
                }
            })
            resizeAnimator.start()
            AnimUtils.reverseRotate(roomInfoChevron)

            roomInfoContainer.contentDescription = roomInfoHeader.text
        }
    }

    private fun recycleImageView(imageView: ImageView) {
        imageView.drawable?.callback = null
        imageView.setImageDrawable(null)
    }

    private fun newAlphaOneToZeroAnimation(view: View): AlphaAnimation {
        val anim = AlphaAnimation(1f, 0f)
        anim.commonSetup()
        anim.setAnimationListener(object : AnimationListenerAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                if (view.id == R.id.strike_through_price) view.visibility = View.GONE
                if (view.id == R.id.expanded_free_cancellation_text_view) view.visibility = View.GONE
            }
        })
        return anim
    }

    private fun newAlphaZeroToOneAnimation(view: View): AlphaAnimation {
        val anim = AlphaAnimation(0f, 1f)
        anim.commonSetup()
        anim.setAnimationListener(object : AnimationListenerAdapter() {
            override fun onAnimationStart(animation: Animation?) {
                view.visibility = View.VISIBLE
            }
        })
        return anim
    }

    private fun AlphaAnimation.commonSetup() {
        this.interpolator = AccelerateDecelerateInterpolator()
        this.fillAfter = true
        this.duration = ANIMATION_DURATION
    }

    private fun topMarginForView(view: View, margin: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = margin
        view.layoutParams = layoutParams
    }

    private fun getDimensionInDp(size: Float) : Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, context.resources.displayMetrics).toInt()
    }
}

