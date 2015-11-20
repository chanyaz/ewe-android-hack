package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.util.subscribeToggleButton
import com.expedia.vm.HotelRoomRateViewModel
import com.mobiata.android.util.AndroidUtils
import rx.Observer
import rx.Observable
import rx.Subscription
import kotlin.properties.Delegates

public class HotelRoomRateView(context: Context, var scrollAncestor: ScrollView, var rowTopConstraintViewObservable: Observable<View>, var rowIndex: Int) : LinearLayout(context) {

    val PICASSO_HOTEL_ROOM = "HOTEL_ROOMS"

    private val ANIMATION_DURATION = 250L

    //views for room row
    private val row: ViewGroup by bindView(R.id.root)

    private val roomType: TextView by bindView(R.id.room_type_text_view)
    private val collapsedBedType: TextView by bindView(R.id.collapsed_bed_type_text_view)
    private val collapsedUrgency: TextView by bindView(R.id.collapsed_urgency_text_view)
    private val expandedBedType: TextView by bindView(R.id.expanded_bed_type_text_view)
    private val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    private val dailyPricePerNight: TextView by bindView(R.id.daily_price_per_night)
    private val perNight: TextView by bindView(R.id.per_night)
    val viewRoom: ToggleButton by bindView(R.id.view_room_button)
    private val roomHeaderImageContainer: FrameLayout by bindView(R.id.room_header_image_container)
    private val roomHeaderImage: ImageView by bindView(R.id.room_header_image)
    private val roomDiscountPercentage: TextView by bindView(R.id.discount_percentage)
    private val roomInfoDescriptionText: TextView by bindView(R.id.room_info_description_text)
    private val roomInfoChevron: ImageView by bindView(R.id.room_info_chevron)
    private val roomInfoContainer: RelativeLayout by bindView(R.id.room_info_container)
    private val expandedAmenity: TextView by bindView(R.id.expanded_amenity_text_view)
    private val freeCancellation: TextView by bindView(R.id.expanded_free_cancellation_text_view)
    private val roomInfoHeader: TextView by bindView(R.id.room_info_header_text)
    private val roomInfoDivider: View by bindView(R.id.room_info_divider)
    private val roomDivider: View by bindView(R.id.row_divider)
    private val spaceAboveRoomInfo: View by bindView(R.id.space_above_room_info)
    private val collapsedContainer: RelativeLayout by bindView(R.id.collapsed_container)
    private val depositTermsButton: TextView by bindView(R.id.deposit_terms_buttons)

    private var roomInfoHeaderTextHeight = -1
    private var roomHeaderImageHeight = -1
    private var roomInfoDividerHeight = -1
    private var roomInfoDescriptionTextHeight = -1
    private var roomInfoChevronHeight = -1
    private var spaceAboveRoomInfoHeight = -1
    private var toggleCollapsed = 0
    private var toggleExpanded = 0
    private var roomContainerTopBottomPadding = 0
    private var roomContainerLeftRightPadding = 0

    public var rowTopConstraintView: View by Delegates.notNull()
    var viewsToHideInExpandedState : Array<View> by Delegates.notNull()
    var viewsToShowInExpandedState : Array<View> by Delegates.notNull()
    var rowTopConstraintSubscription: Subscription? = null

    var viewmodel: HotelRoomRateViewModel by notNullAndObservable { vm ->

        vm.roomSoldOut.filter { it }.map { false }.subscribeChecked(viewRoom)
        vm.roomSoldOut.filter { it }.map { false }.subscribeEnabled(viewRoom)
        vm.soldOutButtonLabelObservable.subscribeToggleButton(viewRoom)

        expandedAmenity.visibility = View.GONE
        viewRoom.subscribeOnCheckChanged(vm.expandCollapseRoomRate)
        row.setOnClickListener {
            vm.expandCollapseRoomRate.onNext(!viewRoom.isChecked)
        }

        vm.roomInfoExpandCollapseObservable1.subscribe {
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams
            lp.addRule(RelativeLayout.BELOW, 0)
            roomInfoChevron.layoutParams = lp
            roomInfoDescriptionText.visibility = View.GONE
        }

        Observable.combineLatest(vm.roomInfoExpandCollapseObservable, vm.expandedMeasurementsDone) { visibility, unit -> visibility }.subscribe({ visibility ->
            val shouldExpand = roomInfoDescriptionText.visibility == View.GONE
            val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
            val lp = roomInfoChevron.layoutParams as RelativeLayout.LayoutParams
            if (shouldExpand) {
                roomInfoDescriptionText.visibility = View.VISIBLE
                lp.addRule(RelativeLayout.BELOW, R.id.room_info_description_text)
                roomInfoChevron.layoutParams = lp
                resizeAnimator.addViewSpec(roomInfoDescriptionText, roomInfoDescriptionTextHeight, 0)
                resizeAnimator.start()
                AnimUtils.rotate(roomInfoChevron)
                //track only when expand the room info
                HotelV2Tracking().trackLinkHotelV2RoomInfoClick()
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
        vm.discountPercentage.subscribeTextAndVisibility(roomDiscountPercentage)
        vm.collapsedBedTypeObservable.subscribeText(collapsedBedType)
        vm.expandedBedTypeObservable.subscribeText(expandedBedType)
        vm.perNightPriceVisibleObservable.map { it && !vm.onlyShowTotalPrice.value }.subscribeVisibility(perNight)
        vm.expandedAmenityObservable.subscribe { text ->
            expandedAmenity.visibility = View.VISIBLE
            expandedAmenity.text = text
        }
        vm.collapsedUrgencyObservable.subscribeText(collapsedUrgency)
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
                    //ignore
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
            if (imageUrl != null && imageUrl.isNotBlank()) {
                val drawable = Images.makeHotelBitmapDrawable(getContext(), emptyPicassoCallback, roomHeaderImage.maxWidth/2, imageUrl, PICASSO_HOTEL_ROOM, R.drawable.room_fallback)
                drawable.setCornerMode(HeaderBitmapDrawable.CornerMode.TOP)
                roomHeaderImage.setImageDrawable(drawable)
            }

            viewsToHideInExpandedState.forEach {
                val alphaAnimation = newAlphaOneToZeroAnimation(it)
                if(!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }
            viewsToShowInExpandedState.forEach {
                if (it is TextView && Strings.isEmpty(it.text)) {
                    it.visibility = View.GONE
                } else {
                    val alphaAnimation = newAlphaZeroToOneAnimation(it)
                    if(!animate) alphaAnimation.duration = 0
                    it.startAnimation(alphaAnimation)
                }
            }

            viewRoom.setPadding(toggleExpanded, 0, toggleExpanded, 0)
            roomInfoContainer.setPadding(roomContainerLeftRightPadding, roomContainerTopBottomPadding, roomContainerLeftRightPadding, roomContainerTopBottomPadding)
            row.isEnabled = false
            var depositTerms = vm.depositTerms.value
            var showTerms = depositTerms?.isNotEmpty() ?: false
            depositTermsButton.visibility = if (showTerms) View.VISIBLE else View.GONE
            if (showTerms) {
                dailyPricePerNight.visibility = View.GONE
            }
            collapsedContainer.setBackgroundColor(Color.WHITE)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            dailyPricePerNight.setTextColor(resources.getColor(R.color.hotels_primary_color))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            perNight.setTextColor(resources.getColor(R.color.hotels_primary_color))

            if (animate) (row.background as TransitionDrawable).startTransition(ANIMATION_DURATION.toInt())

            topMarginForView(row, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
            topMarginForView(roomDivider, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())

            val resizeAnimator = ResizeHeightAnimator(if (animate) ANIMATION_DURATION else 0)
            resizeAnimator.addViewSpec(roomHeaderImageContainer, roomHeaderImageHeight)
            resizeAnimator.addViewSpec(roomInfoHeader, roomInfoHeaderTextHeight)
            resizeAnimator.addViewSpec(roomInfoDivider, roomInfoDividerHeight)
            resizeAnimator.addViewSpec(roomInfoChevron, roomInfoChevronHeight)
            resizeAnimator.addViewSpec(spaceAboveRoomInfo, spaceAboveRoomInfoHeight)

            if (animate) {
                val screenHeight = AndroidUtils.getScreenSize(context).y
                resizeAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                    override fun onAnimationUpdate(p0: ValueAnimator?) {
                        val location = IntArray(2)

                        row.getLocationOnScreen(location)
                        val rowLocationTopY = location[1]
                        val rowLocationBottomY = rowLocationTopY + row.height

                        rowTopConstraintView.getLocationOnScreen(location)
                        val rowTopConstraintViewBottomY = location[1] + rowTopConstraintView.height

                        if (rowLocationBottomY > screenHeight) {
                            scrollAncestor.smoothScrollBy(0, rowLocationBottomY - screenHeight)
                        } else if (rowLocationTopY < rowTopConstraintViewBottomY) {
                            scrollAncestor.smoothScrollBy(0, rowLocationTopY - rowTopConstraintViewBottomY)
                        }
                    }
                })
            }
            resizeAnimator.start()
        }

        Observable.combineLatest(vm.collapseRoomObservable, vm.expandedMeasurementsDone) { animate, unit ->
            animate
        }.subscribe { animate ->
            viewRoom.isChecked = false

            viewsToHideInExpandedState.forEach {
                val alphaAnimation = newAlphaZeroToOneAnimation(it)
                if(!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }
            viewsToShowInExpandedState.forEach {
                val alphaAnimation = newAlphaOneToZeroAnimation(it)
                if(!animate) alphaAnimation.duration = 0
                it.startAnimation(alphaAnimation)
            }

            row.isEnabled = true
            depositTermsButton.visibility = View.GONE
            var depositTerms = vm.depositTerms.value
            var showTerms = depositTerms?.isNotEmpty() ?: false
            if (showTerms) {
                dailyPricePerNight.visibility = View.VISIBLE
            }
            collapsedContainer.background = ContextCompat.getDrawable(context, R.drawable.hotel_detail_ripple)
            viewRoom.setPadding(toggleCollapsed, 0, toggleCollapsed, 0)
            roomInfoContainer.setPadding(0, 0, 0, 0)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            dailyPricePerNight.setTextColor(resources.getColor(R.color.hotel_cell_disabled_text))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            perNight.setTextColor(resources.getColor(R.color.hotel_cell_disabled_text))

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
                    roomHeaderImage.setImageDrawable(null)
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

    init {
        View.inflate(getContext(), R.layout.hotel_room_row, this)
        orientation = LinearLayout.VERTICAL

        viewsToHideInExpandedState = arrayOf(collapsedBedType, collapsedUrgency)
        viewsToShowInExpandedState = arrayOf(expandedBedType, expandedAmenity, freeCancellation, strikeThroughPrice)

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

        viewSetup(scrollAncestor, rowTopConstraintViewObservable, rowIndex)

        val transitionDrawable = TransitionDrawable(arrayOf(ColorDrawable(Color.parseColor("#00000000")), resources.getDrawable(R.drawable.card_background)))
        transitionDrawable.isCrossFadeEnabled = true
        if(rowIndex == 0) transitionDrawable.startTransition(0)
        row.background = transitionDrawable
        toggleCollapsed = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt()
        toggleExpanded = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt()
        roomContainerTopBottomPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
        roomContainerLeftRightPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics).toInt()
    }



    fun viewSetup(scrollAncestor: ScrollView, rowTopConstraintViewObservable: Observable<View>, rowIndex: Int) {
        this.scrollAncestor = scrollAncestor
        this.rowTopConstraintViewObservable = rowTopConstraintViewObservable
        this.rowIndex = rowIndex

        rowTopConstraintSubscription?.unsubscribe()
        rowTopConstraintSubscription = rowTopConstraintViewObservable.subscribe { rowTopConstraintView = it }

        val transitionDrawable = TransitionDrawable(arrayOf(ColorDrawable(Color.parseColor("#00000000")), resources.getDrawable(R.drawable.card_background)))
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

    }

    fun topMarginForView(view: View, margin: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = margin
        view.layoutParams = layoutParams
    }

}

val emptyPicassoCallback = object : HeaderBitmapDrawable.CallbackListener {
    override fun onBitmapLoaded() {
    }

    override fun onBitmapFailed() {
    }

    override fun onPrepareLoad() {
    }
}
