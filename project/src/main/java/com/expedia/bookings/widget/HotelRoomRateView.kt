package com.expedia.bookings.widget

import android.animation.Animator
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeToggleButton
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelRoomRateViewModel
import rx.Observable
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelRoomRateView(context: Context, rowIndex: Int) : LinearLayout(context) {

    private val ANIMATION_DURATION = 250L

    //views for room row
    val row: ViewGroup by bindView(R.id.root)

    private val roomType: TextView by bindView(R.id.room_type_text_view)
    private val collapsedBedType: TextView by bindView(R.id.collapsed_bed_type_text_view)
    private val collapsedUrgency: TextView by bindView(R.id.collapsed_urgency_text_view)
    private val expandedBedType: TextView by bindView(R.id.expanded_bed_type_text_view)
    private val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    private val dailyPricePerNight: TextView by bindView(R.id.daily_price_per_night)
    private val perNight: TextView by bindView(R.id.per_night)
    val viewRoom: ToggleButton by bindView(R.id.view_room_button)
    val roomHeaderImageContainer: FrameLayout by bindView(R.id.room_header_image_container)
    val roomHeaderImage: ImageView by bindView(R.id.room_header_image)
    private val roomDiscountPercentage: TextView by bindView(R.id.discount_percentage)
    private val roomInfoDescriptionText: TextView by bindView(R.id.room_info_description_text)
    val roomInfoChevron: ImageView by bindView(R.id.room_info_chevron)
    private val roomInfoContainer: RelativeLayout by bindView(R.id.room_info_container)
    private val expandedAmenity: TextView by bindView(R.id.expanded_amenity_text_view)
    private val freeCancellation: TextView by bindView(R.id.expanded_free_cancellation_text_view)
    val roomInfoHeader: TextView by bindView(R.id.room_info_header_text)
    val roomInfoDivider: View by bindView(R.id.room_info_divider)
    private val roomDivider: View by bindView(R.id.row_divider)
    val spaceAboveRoomInfo: View by bindView(R.id.space_above_room_info)
    val spaceBelowRoomButton: View by bindView(R.id.space_below_room_button)
    private val collapsedContainer: RelativeLayout by bindView(R.id.collapsed_container)
    private val depositTermsButton: TextView by bindView(R.id.deposit_terms_buttons)
    private val collapsedEarnMessaging: TextView by bindView(R.id.collapsed_earn_message_text_view)

    var roomInfoHeaderTextHeight = -1
    var roomHeaderImageHeight = -1
    var roomInfoDividerHeight = -1
    var roomInfoDescriptionTextHeight = -1
    var roomInfoChevronHeight = -1
    var spaceAboveRoomInfoHeight = -1
    private var toggleCollapsed = 0
    private var toggleExpanded = 0
    private var roomContainerTopBottomPadding = 0
    private var roomContainerLeftRightPadding = 0
    private var showTerms = false
    var viewsToHideInExpandedState: Array<View> by Delegates.notNull()
    var viewsToShowInExpandedState: Array<View> by Delegates.notNull()
    val animateRoom = PublishSubject.create<Pair<HotelRoomRateView, Boolean>>()
    var viewmodel: HotelRoomRateViewModel by notNullAndObservable { vm ->

        if (viewmodel.lob == LineOfBusiness.PACKAGES) {
            viewRoom.textOn = resources.getString(R.string.select)
        }

        vm.collapsedEarnMessageVisibilityObservable.subscribe {
            viewsToHideInExpandedState = arrayOf(collapsedBedType, collapsedUrgency, spaceBelowRoomButton)
            viewsToShowInExpandedState = arrayOf(expandedBedType, expandedAmenity, freeCancellation, strikeThroughPrice)
        }
        vm.collapsedUrgencyVisibilityObservable.subscribeVisibility(collapsedUrgency)
        vm.collapsedEarnMessageVisibilityObservable.subscribeVisibility(collapsedEarnMessaging)

        vm.roomSoldOut.filter { it }.map { false }.subscribeChecked(viewRoom)
        vm.roomSoldOut.filter { it }.map { false }.subscribeEnabled(viewRoom)
        vm.soldOutButtonLabelObservable.subscribeToggleButton(viewRoom)

        expandedAmenity.visibility = View.GONE
        viewRoom.subscribeOnClick(vm.expandCollapseRoomRate)
        row.setOnClickListener {
            vm.expandCollapseRoomRate.onNext(!viewRoom.isChecked)
        }
        vm.setViewRoomContentDescription.subscribe {
            viewRoom.contentDescription = it
        }
        vm.roomInfoExpandCollapseObservable1.subscribe {
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams
            lp.addRule(RelativeLayout.BELOW, 0)
            roomInfoChevron.layoutParams = lp
            roomInfoDescriptionText.visibility = View.GONE
        }

        Observable.combineLatest(vm.roomInfoExpandCollapseObservable, vm.expandedMeasurementsDone) { roomInfoExpandCollapsed, expandedMeasurementsDone -> Unit }.subscribe({
            val shouldExpand = roomInfoDescriptionText.visibility == View.GONE
            val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams
            if (shouldExpand) {
                roomInfoDescriptionText.visibility = View.VISIBLE
                roomInfoDescriptionText.requestFocus()
                lp.addRule(RelativeLayout.BELOW, R.id.room_info_description_text)
                roomInfoChevron.layoutParams = lp
                resizeAnimator.addViewSpec(roomInfoDescriptionText, roomInfoDescriptionTextHeight, 0)
                resizeAnimator.start()
                AnimUtils.rotate(roomInfoChevron)
                //track only when expand the room info
                if (viewmodel.lob == LineOfBusiness.PACKAGES) {
                    PackagesTracking().trackHotelRoomMoreInfoClick()
                } else {
                    HotelTracking.trackLinkHotelRoomInfoClick()
                }
            } else {
                lp.addRule(RelativeLayout.BELOW, 0)
                roomInfoChevron.layoutParams = lp
                resizeAnimator.addViewSpec(roomInfoDescriptionText, 0)
                resizeAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(p0: Animator?) {
                        roomInfoDescriptionText.visibility = View.GONE
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        // ignore
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                        // ignore
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        // ignore
                    }
                })
                resizeAnimator.start()
                AnimUtils.reverseRotate(roomInfoChevron)
            }
        })

        roomInfoContainer.subscribeOnClick(vm.expandCollapseRoomRateInfoDescription)
        depositTermsButton.subscribeOnClick(vm.depositInfoContainerClick)

        vm.roomRateInfoTextObservable.subscribeText(roomInfoDescriptionText)
        vm.roomTypeObservable.subscribeText(roomType)
        vm.discountPercentage.subscribeText(roomDiscountPercentage)
        vm.shouldShowDiscountPercentage.subscribeVisibility(roomDiscountPercentage)
        vm.collapsedBedTypeObservable.subscribeText(collapsedBedType)
        vm.expandedBedTypeObservable.subscribeText(expandedBedType)
        vm.perNightPriceVisibleObservable.map { it && !vm.onlyShowTotalPrice.value }.subscribeVisibility(perNight)
        vm.expandedAmenityObservable.subscribe { text ->
            expandedAmenity.visibility = View.VISIBLE
            expandedAmenity.text = text
        }
        vm.collapsedUrgencyObservable.subscribeText(collapsedUrgency)
        vm.collapsedEarnMessageObservable.subscribeText(collapsedEarnMessaging)

        vm.expandedMessageObservable.subscribe { expandedMessagePair ->
            val drawable = ContextCompat.getDrawable(context, expandedMessagePair.second)
            freeCancellation.text = expandedMessagePair.first
            freeCancellation.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
        vm.dailyPricePerNightObservable.subscribeTextAndVisibility(dailyPricePerNight)
        vm.viewRoomObservable.subscribe {
            viewRoom.isChecked = true
        }
        vm.roomInfoVisibiltyObservable.subscribeVisibility(roomInfoContainer)
        vm.roomInfoVisibiltyObservable.subscribeVisibility(roomInfoDivider)
        vm.strikeThroughPriceObservable.subscribeTextAndVisibility(strikeThroughPrice)
        vm.depositTerms.subscribe {
            val depositTerms = it
            showTerms = depositTerms?.isNotEmpty() ?: false
        }
        fun AlphaAnimation.commonSetup() {
            this.interpolator = AccelerateDecelerateInterpolator()
            this.fillAfter = true
            this.duration = ANIMATION_DURATION
        }

        fun newAlphaZeroToOneAnimation(view: View): AlphaAnimation {
            val anim = AlphaAnimation(0f, 1f)
            anim.commonSetup()
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    //ignore
                }
            })
            return anim
        }

        fun newAlphaOneToZeroAnimation(view: View): AlphaAnimation {
            val anim = AlphaAnimation(1f, 0f)
            anim.commonSetup()
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (view.id == R.id.strike_through_price) view.visibility = View.GONE
                    if (view.id == R.id.expanded_free_cancellation_text_view) view.visibility = View.GONE
                    if (view.id == R.id.space_below_room_button) view.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            return anim
        }

        Observable.combineLatest(vm.expandRoomObservable, vm.expandedMeasurementsDone) { animate, unit ->
            animate
        }.filter {
            //Sold Out Room cannot be expanded!
            !vm.roomSoldOut.value
        }.subscribe { animate ->
            //let the observable know that I am expanding
            viewmodel.rowExpanding.onNext(viewmodel.rowIndex)

            viewRoom.isChecked = true

            val imageUrl: String? = vm.roomHeaderImageObservable.value
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
                if (!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }
            viewsToShowInExpandedState.forEach {
                if (it is TextView && Strings.isEmpty(it.text)) {
                    it.visibility = View.GONE
                } else {
                    val alphaAnimation = newAlphaZeroToOneAnimation(it)
                    if (!animate) alphaAnimation.duration = 0
                    it.startAnimation(alphaAnimation)
                }
            }

            viewRoom.setPadding(toggleExpanded, 0, toggleExpanded, 0)
            roomInfoContainer.setPadding(roomContainerLeftRightPadding, roomContainerTopBottomPadding, roomContainerLeftRightPadding, roomContainerTopBottomPadding)
            row.isEnabled = false

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

            if (animate) (row.background as TransitionDrawable).startTransition(ANIMATION_DURATION.toInt())

            topMarginForView(row, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
            topMarginForView(roomDivider, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())

            animateRoom.onNext(Pair(this, animate))
        }

        Observable.combineLatest(vm.collapseRoomObservable, vm.expandedMeasurementsDone) { animate, unit ->
            animate
        }.subscribe { animate ->
            viewRoom.isChecked = false
            viewRoom.contentDescription = context.getString(R.string.hotel_room_expand_cont_desc)
            viewsToHideInExpandedState.forEach {
                val alphaAnimation = newAlphaZeroToOneAnimation(it)
                if (!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }
            viewsToShowInExpandedState.forEach {
                val alphaAnimation = newAlphaOneToZeroAnimation(it)
                if (!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }

            row.isEnabled = true
            depositTermsButton.visibility = View.GONE
            if (showTerms) {
                dailyPricePerNight.visibility = View.VISIBLE
            }
            collapsedContainer.background = ContextCompat.getDrawable(context, R.drawable.gray_background_ripple)
            viewRoom.setPadding(toggleCollapsed, 0, toggleCollapsed, 0)
            roomInfoContainer.setPadding(0, 0, 0, 0)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            dailyPricePerNight.setTextColor(ContextCompat.getColor(context, R.color.hotel_cell_disabled_text))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            perNight.setTextColor(ContextCompat.getColor(context, R.color.hotel_cell_disabled_text))

            if (animate) (row.background as TransitionDrawable).reverseTransition(ANIMATION_DURATION.toInt())

            val resizeAnimator = ResizeHeightAnimator(if (animate) ANIMATION_DURATION else 0)
            resizeAnimator.addViewSpec(roomHeaderImageContainer, 0)
            resizeAnimator.addViewSpec(roomInfoHeader, 0)
            resizeAnimator.addViewSpec(roomInfoDivider, 0)
            resizeAnimator.addViewSpec(roomInfoChevron, 0)
            resizeAnimator.addViewSpec(spaceAboveRoomInfo, 0)
            if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                resizeAnimator.addViewSpec(roomInfoDescriptionText, 0)
            }

            topMarginForView(row, 0)
            topMarginForView(roomDivider, 0)

            resizeAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    recycleImageView(roomHeaderImage)
                    if (roomInfoDescriptionText.visibility == View.VISIBLE)
                        vm.roomInfoExpandCollapseObservable.onNext(Unit)
                }

                override fun onAnimationStart(p0: Animator?) {
                    // ignore
                }

                override fun onAnimationRepeat(p0: Animator?) {
                    // ignore
                }

                override fun onAnimationCancel(p0: Animator?) {
                    // ignore
                }
            })
            resizeAnimator.start()
        }
    }

    fun recycleImageView(imageView: ImageView) {
        imageView.drawable?.callback = null
        imageView.setImageDrawable(null)
    }

    init {
        View.inflate(getContext(), R.layout.hotel_room_row, this)
        orientation = LinearLayout.VERTICAL

        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                roomHeaderImageHeight = roomHeaderImage.height;
                roomInfoHeaderTextHeight = roomInfoHeader.height;
                roomInfoDividerHeight = roomInfoDivider.height;
                roomInfoDescriptionTextHeight = roomInfoDescriptionText.height;
                roomInfoChevronHeight = roomInfoChevron.height
                spaceAboveRoomInfoHeight = spaceAboveRoomInfo.height

                roomInfoDescriptionText.visibility = View.GONE
                row.viewTreeObserver.removeOnGlobalLayoutListener(this)

                viewmodel.expandedMeasurementsDone.onNext(Unit)
            }
        }
        row.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        viewSetup(rowIndex)

        val transitionDrawable = TransitionDrawable(arrayOf(ColorDrawable(Color.parseColor("#00000000")), ContextCompat.getDrawable(context, R.drawable.card_background)))
        transitionDrawable.isCrossFadeEnabled = true
        if (rowIndex == 0) transitionDrawable.startTransition(0)
        row.background = transitionDrawable
        toggleCollapsed = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt()
        toggleExpanded = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt()
        roomContainerTopBottomPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        roomContainerLeftRightPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics).toInt()
    }

    fun viewSetup(rowIndex: Int) {

        val transitionDrawable = TransitionDrawable(arrayOf(ColorDrawable(Color.parseColor("#00000000")), ContextCompat.getDrawable(context, R.drawable.card_background)))
        transitionDrawable.isCrossFadeEnabled = true
        if (rowIndex == 0) {
            transitionDrawable.startTransition(0)
        }
        viewRoom.isChecked = false
        viewRoom.isEnabled = true
        viewRoom.textOff = resources.getString(R.string.view_room_button_text)
        viewRoom.textOn = resources.getString(R.string.book_room_button_text)
        row.background = transitionDrawable
        roomInfoDescriptionText.visibility = View.VISIBLE

        if (ExpediaBookingApp.isDeviceShitty()) {
            roomHeaderImage.visibility = View.GONE
        } else {
            roomHeaderImage.visibility = View.VISIBLE
        }

    }

    fun topMarginForView(view: View, margin: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = margin
        view.layoutParams = layoutParams
    }

}

