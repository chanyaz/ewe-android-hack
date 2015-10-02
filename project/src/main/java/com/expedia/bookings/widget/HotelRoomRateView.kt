package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimation
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelRoomRateViewModel
import rx.Observer
import rx.Observable

public class HotelRoomRateView(context: Context, val selectedRoomObserver: Observer<HotelOffersResponse.HotelRoomResponse>) : LinearLayout(context) {

    val PICASSO_HOTEL_ROOM = "HOTEL_ROOMS"

    private val ANIMATION_DURATION = 500L

    //views for room row
    private val row: ViewGroup by bindView(R.id.root)

    private val roomType: TextView by bindView(R.id.room_type_text_view)
    private val collapsedBedType: TextView by bindView(R.id.collapsed_bed_type_text_view)
    private val collapsedUrgency : TextView by bindView(R.id.collapsed_urgency_text_view)
    private val expandedBedType: TextView by bindView(R.id.expanded_bed_type_text_view)
    private val dailyPricePerNight: TextView by bindView(R.id.daily_price_per_night)
    private val totalPricePerNight: TextView by bindView(R.id.total_price_per_night)
    private val perNight : TextView by bindView(R.id.per_night)
    private val viewRoom: ToggleButton by bindView (R.id.view_room_button)
    private val roomHeaderImage: ImageView by bindView(R.id.room_header_image)
    private val roomInfoDescriptionText: TextView by bindView(R.id.room_info_description_text)
    private val roomInfoContainer: RelativeLayout by bindView(R.id.room_info_container)
    private val expandedAmenity: TextView by bindView(R.id.expanded_amenity_text_view)
    private val freeCancellation: TextView by bindView(R.id.expanded_free_cancellation_text_view)
    private val roomInfoHeader: TextView by bindView(R.id.room_info_header_text)
    private val roomInfoDivider : View by bindView(R.id.room_info_divider)
    private val roomDivider : View by bindView(R.id.row_divider)

    private var roomInfoHeaderTextHeight = -1
    private var roomHeaderImageHeight = -1
    private var roomImageGradientBottomHeight = -1
    private var roomInfoDividerHeight = -1
    private var roomInfoDescriptionTextHeight = -1
    private var toggleCollapsed = 0
    private var toggleExpanded = 0

    var viewmodel: HotelRoomRateViewModel by notNullAndObservable { vm ->
        val viewsToHideInExpandedState = arrayOf(collapsedBedType, collapsedUrgency)
        val viewsToShowInExpandedState = arrayOf(expandedBedType, expandedAmenity, freeCancellation, totalPricePerNight, roomInfoContainer)

        expandedAmenity.visibility = View.GONE
        viewRoom.subscribeOnCheckChanged(vm.expandCollapseRoomRate)
        row.setOnClickListener {
            vm.expandCollapseRoomRate.onNext(!viewRoom.isChecked)
        }
        vm.roomSelectedObservable.subscribe(selectedRoomObserver)

        Observable.combineLatest(vm.roomInfoExpandCollapseObservable, vm.expandedMeasurementsDone) { visibility, unit -> visibility }.subscribe({ visibility ->
            roomInfoDescriptionText.visibility = if (roomInfoDescriptionText.getVisibility() == View.VISIBLE) View.GONE else View.VISIBLE
        })

        roomInfoContainer.subscribeOnClick(vm.expandCollapseRoomRateInfoDescription)

        vm.totalPricePerNightObservable.subscribe(totalPricePerNight)
        vm.roomRateInfoTextObservable.subscribe(roomInfoDescriptionText)
        vm.roomTypeObservable.subscribe(roomType)
        vm.collapsedBedTypeObservable.subscribe(collapsedBedType)
        vm.expandedBedTypeObservable.subscribe(expandedBedType)
        vm.expandedAmenityObservable.subscribe { text ->
            expandedAmenity.visibility = View.VISIBLE
            expandedAmenity.text = text
        }

        vm.collapsedUrgencyObservable.subscribe(collapsedUrgency)
        vm.expandedMessageObservable.subscribe { expandedMessagePair ->
            freeCancellation.text = expandedMessagePair.first
            freeCancellation.setCompoundDrawablesWithIntrinsicBounds(expandedMessagePair.second, null, null, null)
        }

        vm.dailyPricePerNightObservable.subscribe(dailyPricePerNight)
        vm.perNightObservable.subscribeVisibility(perNight)
        vm.roomHeaderImageObservable.subscribe { imageUrl ->
            val drawable = Images.makeHotelBitmapDrawable(getContext(), emptyPicassoCallback, roomHeaderImage.maxWidth, imageUrl, PICASSO_HOTEL_ROOM, R.drawable.room_fallback)
            drawable.setCornerMode(HeaderBitmapDrawable.CornerMode.TOP)
            roomHeaderImage.setImageDrawable(drawable)
        }

        vm.viewRoomObservable.subscribe {
            viewRoom.isChecked = true
        }

        fun newAlphaZeroToOneAnimation(view: View): AlphaAnimation {
            val anim = AlphaAnimation(0f, 1f)
            anim.fillAfter = true
            anim.setAnimationListener(object: Animation.AnimationListener {
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
            anim.duration = ANIMATION_DURATION
            return anim
        }

        fun newAlphaOneToZeroAnimation(): AlphaAnimation {
            val anim = AlphaAnimation(1f, 0f)
            anim.fillAfter = true
            anim.duration = ANIMATION_DURATION
            return anim
        }

        Observable.combineLatest(vm.expandRoomObservable, vm.expandedMeasurementsDone) { animate, unit -> animate }.subscribe { animate ->
            viewRoom.isChecked = true

            viewsToHideInExpandedState.forEach {
                it.startAnimation(newAlphaOneToZeroAnimation())
            }
            viewsToShowInExpandedState.forEach {
                if (it.id == R.id.expanded_amenity_text_view && Strings.isEmpty((it as TextView).text)) {
                    it.visibility = View.GONE
                }
                else {
                    it.startAnimation(newAlphaZeroToOneAnimation(it))
                }
            }

            viewRoom.setPadding(toggleExpanded, 0, toggleExpanded, 0)
            row.isEnabled = false
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            dailyPricePerNight.setTextColor(resources.getColor(R.color.hotels_primary_color))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            perNight.setTextColor(resources.getColor(R.color.hotels_primary_color))

            if (animate) {
                (row.background as TransitionDrawable).reverseTransition(ANIMATION_DURATION.toInt())
            }

            val resizeAnimation = ResizeHeightAnimation()
            topMarginForView(row, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
            topMarginForView(roomDivider, resources.getDimension(R.dimen.launch_tile_margin_top).toInt())
            resizeAnimation.addViewSpec(roomHeaderImage, roomHeaderImageHeight)
            resizeAnimation.addViewSpec(roomInfoHeader, roomInfoHeaderTextHeight)
            resizeAnimation.addViewSpec(roomInfoDivider, roomInfoDividerHeight)
            if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                resizeAnimation.addViewSpec(roomInfoDescriptionText, roomInfoDescriptionTextHeight)
            }

            resizeAnimation.duration = if (animate) ANIMATION_DURATION else 0
            row.startAnimation(resizeAnimation)
        }

        Observable.combineLatest(vm.collapseRoomObservable, vm.expandedMeasurementsDone) { animate, unit -> animate }.subscribe { animate ->
            viewRoom.isChecked = false

            viewsToHideInExpandedState.forEach {
                it.startAnimation(newAlphaZeroToOneAnimation(it))
            }
            viewsToShowInExpandedState.forEach {
                if (it.id == R.id.room_info_container) {
                    it.visibility = View.GONE
                } else {
                    it.startAnimation(newAlphaOneToZeroAnimation())
                }
            }

            row.isEnabled = true
            viewRoom.setPadding(toggleCollapsed, 0, toggleCollapsed, 0)
            dailyPricePerNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            dailyPricePerNight.setTextColor(resources.getColor(R.color.hotel_cell_disabled_text))
            perNight.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            perNight.setTextColor(resources.getColor(R.color.hotel_cell_disabled_text))

            (row.background as TransitionDrawable).startTransition(ANIMATION_DURATION.toInt())

            val resizeAnimation = ResizeHeightAnimation()
            resizeAnimation.addViewSpec(roomHeaderImage, 0)
            resizeAnimation.addViewSpec(roomInfoHeader, 0)
            resizeAnimation.addViewSpec(roomInfoDivider, 0)
            if (roomInfoDescriptionText.visibility == View.VISIBLE) {
                resizeAnimation.addViewSpec(roomInfoDescriptionText, 0)
            }

            topMarginForView(row, 0)
            topMarginForView(roomDivider, 0)

            resizeAnimation.duration = if (animate) ANIMATION_DURATION else 0
            row.startAnimation(resizeAnimation)
        }
    }

    init {
        View.inflate(getContext(), R.layout.hotel_room_row, this)

        val globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                roomHeaderImageHeight = roomHeaderImage.height;
                roomInfoHeaderTextHeight = roomInfoHeader.height;
                roomInfoDividerHeight = roomInfoDivider.height;
                roomInfoDescriptionTextHeight = roomInfoDescriptionText.height;

                row.viewTreeObserver.removeOnGlobalLayoutListener(this)

                viewmodel.expandedMeasurementsDone.onNext(Unit)
            }
        }
        row.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        val transitionDrawable = TransitionDrawable(arrayOf(resources.getDrawable(R.drawable.card_background), ColorDrawable(Color.parseColor("#00000000"))))
        transitionDrawable.isCrossFadeEnabled = true
        row.background = transitionDrawable
        toggleCollapsed = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.resources.displayMetrics).toInt()
        toggleExpanded = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics).toInt()
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
