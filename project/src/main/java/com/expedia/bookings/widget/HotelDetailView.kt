package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.publishOnClick
import com.expedia.util.subscribeBackground
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeBackgroundResource
import com.expedia.util.subscribeGalleryColorFilter
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.mobiata.android.util.AndroidUtils
import rx.Observable
import rx.Observer
import java.util.ArrayList
import kotlin.properties.Delegates

val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    var bottomMargin = 0
    val ANIMATION_DURATION = 200L
    val SELECT_ROOM_ANIMATION = 300L
    var resortViewHeight = 0
    var selectRoomContainerHeight = 0
    val screenSize by lazy { Ui.getScreenSize(context) }

    var initialScrollTop = 0

    val hotelDetailsToolbar: HotelDetailsToolbar by bindView(R.id.hotel_details_toolbar)
    var toolBarHeight = 0

    var galleryHeight = 0
    val gallery: RecyclerGallery by bindView(R.id.images_gallery)
    val galleryContainer: FrameLayout by bindView(R.id.gallery_container)
    val galleryRoot: LinearLayout by bindView(R.id.gallery)

    val priceContainer: ViewGroup by bindView(R.id.price_widget)
    val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    val price: TextView by bindView(R.id.price)
    val perNight: TextView by bindView(R.id.per_night)
    val detailsSoldOut: TextView by bindView(R.id.details_sold_out)
    val priceWidget: View by bindView(R.id.price_widget)
    val searchDatesInfo: TextView by bindView(R.id.search_dates_info)
    val hotelPriceContainer: View by bindView(R.id.hotel_price_container)

    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    val selectRoomButton: Button by bindView(R.id.select_room_button)
    val changeDatesButton: Button by bindView(R.id.change_dates_button)
    val stickySelectRoomContainer: ViewGroup by bindView(R.id.sticky_select_room_container)
    val stickySelectRoomButton: Button by bindView(R.id.sticky_select_room)
    val userRating: TextView by bindView(R.id.user_rating)
    val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    val userRatingRecommendationText: TextView by bindView(R.id.user_rating_recommendation_text)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val readMoreView: ImageButton by bindView(R.id.read_more)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val hotelDescriptionContainer: ViewGroup by bindView(R.id.hotel_description_container)
    val miniMapView: LocationMapImageView by bindView(R.id.mini_map_view)
    val transparentViewOverMiniMap: View by bindView(R.id.transparent_view_over_mini_map)
    val gradientHeight = context.resources.getDimension(R.dimen.hotel_detail_gradient_height)

    val hotelMessagingContainer: RelativeLayout by bindView(R.id.promo_messaging_container)
    val discountPercentage: TextView by bindView(R.id.discount_percentage)
    val airAttachSWPImage: ImageView by bindView(R.id.air_attach_swp_image_details)
    val vipAccessMessageContainer: LinearLayout by bindView(R.id.vip_access_message_container)
    val vipLoyaltyMessage: TextView by bindView(R.id.vip_loyalty_message_details)
    val regularLoyaltyMessage: TextView by bindView(R.id.regular_loyalty_applied)
    val promoMessage: TextView by bindView(R.id.promo_text)
    val earnMessage: TextView by bindView(R.id.earn_message)

    val payLaterButtonContainer: FrameLayout by bindView(R.id.radius_pay_later_container)
    val payNowButtonContainer: FrameLayout by bindView(R.id.radius_pay_now_container)
    val payNowButton: TextView by bindView(R.id.radius_pay_now)
    val payLaterButton: TextView by bindView(R.id.radius_pay_later)
    val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)
    val etpInfoText: TextView by bindView(R.id.etp_info_text)
    val etpInfoTextSmall: TextView by bindView(R.id.etp_info_text_small)
    val freeCancellation: TextView by bindView(R.id.free_cancellation)
    val bestPriceGuarantee: TextView by bindView(R.id.best_price_guarantee)
    val singleMessageContainer: ViewGroup by bindView(R.id.single_message_container)
    val freeCancellationAndETPMessaging: ViewGroup by bindView(R.id.free_cancellation_etp_messaging)
    val etpContainer: HotelEtpStickyHeaderLayout by bindView(R.id.etp_placeholder)
    val etpContainerDropShadow: View by bindView(R.id.pay_later_drop_shadow)
    val renovationContainer: ViewGroup by bindView(R.id.renovation_container)
    val payByPhoneTextView: TextView by bindView(R.id.book_by_phone_text)
    val payByPhoneContainer: ViewGroup by bindView(R.id.book_by_phone_container)
    val space: Space by bindView(R.id.spacer)

    val hotelGalleryDescriptionContainer: LinearLayout by bindView(R.id.hotel_gallery_description_container)
    val hotelGalleryIndicator: View by bindView(R.id.hotel_gallery_indicator)
    val hotelGalleryDescription: TextView by bindView(R.id.hotel_gallery_description)
    val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    val amenityDivider: View by bindView(R.id.etp_and_free_cancellation_divider)

    val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)
    val roomRateHeader: LinearLayout by bindView(R.id.room_rate_header)
    val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    val roomRateRegularLoyaltyAppliedView: LinearLayout by bindView(R.id.room_rate_regular_loyalty_applied_container)
    val roomRateVIPLoyaltyAppliedContainer: View by bindView(R.id.room_rate_vip_loyalty_applied_container)
    val commonAmenityDivider: View by bindView(R.id.common_amenities_divider)
    val roomContainer: TableLayout by bindView(R.id.room_container)
    val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)

    val detailContainer: NewHotelDetailsScrollView by bindView(R.id.detail_container)
    var statusBarHeight = 0
    var offset: Float by Delegates.notNull()
    var priceContainerLocation = IntArray(2)
    var urgencyContainerLocation = IntArray(2)
    var roomContainerPosition = IntArray(2)

    var resortInAnimator: ObjectAnimator by Delegates.notNull()
    var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    var selectRoomInAnimator: ObjectAnimator by Delegates.notNull()
    var selectRoomOutAnimator: ObjectAnimator by Delegates.notNull()

    private val ANIMATION_DURATION_ROOM_CONTAINER = if (ExpediaBookingApp.isAutomation()) 0L else 250L

    var viewmodel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        detailContainer.setOnTouchListener(touchListener)
        hotelDetailsToolbar.setHotelDetailViewModel(vm)

        vm.galleryColorFilter.subscribeGalleryColorFilter(gallery)

        vm.hotelSoldOut.subscribeVisibility(changeDatesButton)
        vm.hotelSoldOut.subscribeInverseVisibility(selectRoomButton)
        vm.hotelSoldOut.subscribeVisibility(detailsSoldOut)
        vm.hotelSoldOut.subscribeInverseVisibility(price)
        vm.hotelSoldOut.subscribeInverseVisibility(roomContainer)
        vm.hotelSoldOut.subscribeInverseVisibility(stickySelectRoomContainer)

        changeDatesButton.publishOnClick(vm.changeDates)

        vm.galleryObservable.subscribe { galleryUrls ->
            gallery.setOnItemClickListener(vm)
            gallery.setOnItemChangeListener(vm)
            gallery.setDataSource(galleryUrls)
            gallery.setProgressBarOnImageViewsEnabled(true)
            gallery.scrollToPosition(0)
            gallery.startFlipping()

            val galleryItemCount = gallery.adapter.itemCount
            if (galleryItemCount > 0) {
                val indicatorWidth = screenSize.x / galleryItemCount
                val lp = hotelGalleryIndicator.layoutParams
                lp.width = indicatorWidth
                hotelGalleryIndicator.layoutParams = lp
            }
        }

        vm.hotelSoldOut.filter { it }.subscribe { resetGallery() }
        vm.priceWidgetBackground.subscribeBackgroundColor(priceWidget)

        vm.scrollToRoom.subscribe { scrollToRoom(false) }

        vm.noAmenityObservable.subscribe {
            amenityContainer.visibility = View.GONE
            amenityDivider.visibility = View.GONE
        }
        vm.amenitiesListObservable.subscribe { amenityList ->
            amenityContainer.visibility = View.VISIBLE
            amenityDivider.visibility = View.VISIBLE
            Amenity.addHotelAmenity(amenityContainer, amenityList)
        }
        vm.commonAmenityTextObservable.subscribe { text ->
            displayRoomRateHeader()
            commonAmenityText.visibility = View.VISIBLE
            commonAmenityText.text = Html.fromHtml(text)
        }

        vm.hasVipAccessLoyaltyObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        }

        vm.hasRegularLoyaltyPointsAppliedObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        vm.galleryItemChangeObservable.subscribe { galleryDescriptionBar: Pair<Int, String> ->
            hotelGalleryIndicator.animate().translationX((galleryDescriptionBar.first * hotelGalleryIndicator.width).toFloat()).
                    setInterpolator(LinearInterpolator()).start()
            hotelGalleryDescription.text = galleryDescriptionBar.second
        }

        transparentViewOverMiniMap.subscribeOnClick(vm.mapClickedSubject)

        vm.renovationObservable.subscribeVisibility(renovationContainer)
        vm.hotelResortFeeObservable.subscribeText(resortFeeWidget.resortFeeText)
        vm.hotelResortFeeIncludedTextObservable.subscribeText(resortFeeWidget.feesIncludedNotIncluded)

        vm.sectionBodyObservable.subscribe {
            hotelDescription.text = it
            hotelDescription.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    hotelDescription.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (hotelDescription.lineCount <= HOTEL_DESC_COLLAPSE_LINES) {
                        readMoreView.visibility = View.GONE
                        hotelDescriptionContainer.isClickable = false
                    } else {
                        readMoreView.visibility = View.VISIBLE
                        hotelDescriptionContainer.isClickable = true
                        hotelDescriptionContainer.subscribeOnClick(vm.hotelDescriptionContainerObserver)
                    }
                }
            })
        }
        vm.strikeThroughPriceObservable.subscribeText(strikeThroughPrice)
        vm.strikeThroughPriceVisibility.subscribeVisibility(strikeThroughPrice)
        vm.pricePerNightObservable.subscribeText(price)
        vm.searchInfoObservable.subscribeText(searchInfo)
        vm.roomPriceToShowCustomer.subscribeText(price)
        vm.perNightVisibility.subscribeInverseVisibility(perNight)

        vm.isPackageHotelObservable.subscribeInverseVisibility(hotelPriceContainer)
        vm.isPackageHotelObservable.subscribeVisibility(searchDatesInfo)
        vm.searchDatesObservable.subscribeText(searchDatesInfo)

        vm.isUserRatingAvailableObservable.subscribeVisibility(userRating)
        vm.userRatingObservable.subscribeText(userRating)
        vm.userRatingBackgroundColorObservable.subscribeBackground(userRating)
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRatingRecommendationText)
        vm.userRatingRecommendationTextObservable.subscribeText(userRatingRecommendationText)
        vm.isUserRatingAvailableObservable.map { !it }.subscribeVisibility(noGuestRating)

        vm.numberOfReviewsObservable.subscribeText(numberOfReviews)
        vm.hotelLatLngObservable.subscribe {
            values ->
            val location = Location()
            location.latitude = values[0]
            location.longitude = values[1]
            miniMapView.setLocation(location)
        }
        vm.payByPhoneContainerVisibility.subscribe { spaceAboveSelectARoom() }
        vm.payByPhoneContainerVisibility.subscribeVisibility(payByPhoneContainer)
        vm.discountPercentageObservable.subscribeText(discountPercentage)
        vm.discountPercentageBackgroundObservable.subscribeBackgroundResource(discountPercentage)
        vm.showDiscountPercentageObservable.subscribeVisibility(discountPercentage)
        vm.showAirAttachSWPImageObservable.subscribeVisibility(airAttachSWPImage)
        vipAccessMessageContainer.subscribeOnClick(vm.vipAccessInfoObservable)
        vm.hasVipAccessObservable.subscribeVisibility(vipAccessMessageContainer)
        vm.hasVipAccessLoyaltyObservable.subscribeVisibility(vipLoyaltyMessage)
        vm.hasRegularLoyaltyPointsAppliedObservable.subscribeVisibility(regularLoyaltyMessage)
        vm.promoMessageObservable.subscribeText(promoMessage)
        vm.promoMessageVisibilityObservable.subscribeVisibility(promoMessage)
        vm.earnMessageObservable.subscribeText(earnMessage)
        vm.earnMessageVisibilityObservable.subscribeVisibility(earnMessage)

        vm.hotelMessagingContainerVisibility.subscribeVisibility(hotelMessagingContainer)

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(roomList.first)) {
                return@subscribe
            }
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(roomList.first.size)

            val roomContainerAlphaZeroToOneAnimation = AlphaAnimation(0f, 1f)
            roomContainerAlphaZeroToOneAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER

            val roomContainerAlphaOneToZeroAnimation = AlphaAnimation(1f, 0f)
            roomContainerAlphaOneToZeroAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER
            roomContainerAlphaOneToZeroAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(p0: Animation?) {
                    for (index in 0..(roomContainer.childCount - 1)) {
                        val room = roomContainer.getChildAt(index) as HotelRoomRateView
                        recycleImageView(room.roomHeaderImage)
                    }
                    roomContainer.removeAllViews()
                    roomList.first.forEachIndexed { roomResponseIndex, room ->
                        val hasETP = viewmodel.hasETPObservable.value
                        val view = HotelRoomRateView(context, roomResponseIndex)
                        view.viewmodel = HotelRoomRateViewModel(context, vm.hotelOffersResponse.hotelId, roomList.first[roomResponseIndex], roomList.second[roomResponseIndex], roomResponseIndex, vm.rowExpandingObservable, vm.roomSelectedObserver, hasETP, viewmodel.getLOB())
                        view.animateRoom.subscribe(rowAnimation)
                        var parent = view.parent
                        if (parent != null) {
                            (parent as ViewGroup).removeView(view)
                        }
                        roomContainer.addView(view)
                        hotelRoomRateViewModels.add(view.viewmodel)
                        view.viewmodel.depositTermsClickedObservable.subscribe {
                            vm.depositInfoContainerClickObservable.onNext(Pair(vm.hotelOffersResponse.hotelCountry, room))
                        }
                    }

                    vm.lastExpandedRowObservable.onNext(-1)
                    vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
                    roomContainer.startAnimation(roomContainerAlphaZeroToOneAnimation)

                    //set focus on first room row for accessibility
                    (roomContainer.getChildAt(0) as HotelRoomRateView).row.isFocusableInTouchMode = true
                }

                override fun onAnimationStart(p0: Animation?) {
                    //ignore
                }

                override fun onAnimationRepeat(p0: Animation?) {
                    //ignore
                }
            })
            roomContainer.startAnimation(roomContainerAlphaOneToZeroAnimation)
        }

        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.etpContainerVisibility.subscribeVisibility(etpContainer)
        vm.hasETPObservable.filter { it == true }.subscribe { payNowLaterSelectionChanged(true) }

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut -> hasETP && hasFreeCancellation && !hotelSoldOut }
                .subscribeVisibility(freeCancellationAndETPMessaging)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut -> !(hasETP && hasFreeCancellation) && !hotelSoldOut }
                .subscribeVisibility(singleMessageContainer)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hasBestPriceGuarantee, hotelSoldOut -> (hasETP || hasFreeCancellation || hasBestPriceGuarantee) && !hotelSoldOut }
                .subscribeVisibility(etpAndFreeCancellationMessagingContainer)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, vm.isUserRatingAvailableObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hasBestPriceGuarantee, hasUserReviews, hotelSoldOut -> !hasETP && !hasFreeCancellation && hasBestPriceGuarantee && !hasUserReviews && !hotelSoldOut }
                .subscribeVisibility(bestPriceGuarantee)

        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(etpRoomList.first)) {
                return@subscribe
            }
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(etpRoomList.first.size)

            val roomContainerAlphaZeroToOneAnimation = AlphaAnimation(0f, 1f)
            roomContainerAlphaZeroToOneAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER

            val roomContainerAlphaOneToZeroAnimation = AlphaAnimation(1f, 0f)
            roomContainerAlphaOneToZeroAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER
            roomContainerAlphaOneToZeroAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(p0: Animation?) {
                    for (index in 0..(roomContainer.childCount - 1)) {
                        val room = roomContainer.getChildAt(index) as HotelRoomRateView
                        recycleImageView(room.roomHeaderImage)
                    }
                    roomContainer.removeAllViews()
                    etpRoomList.first.forEachIndexed { roomResponseIndex, room ->
                        val hasETP = viewmodel.hasETPObservable.value
                        val view = HotelRoomRateView(context, roomResponseIndex)
                        view.viewmodel = HotelRoomRateViewModel(context, vm.hotelOffersResponse.hotelId, etpRoomList.first.get(roomResponseIndex).payLaterOffer, etpRoomList.second.get(roomResponseIndex), roomResponseIndex, vm.rowExpandingObservable, vm.roomSelectedObserver, hasETP, viewmodel.getLOB())
                        view.animateRoom.subscribe(rowAnimation)
                        var parent = view.parent
                        if (parent != null) {
                            (parent as ViewGroup).removeView(view)
                        }
                        roomContainer.addView(view)
                        hotelRoomRateViewModels.add(view.viewmodel)
                        view.viewmodel.depositTermsClickedObservable.subscribe {
                            vm.depositInfoContainerClickObservable.onNext(Pair(vm.hotelOffersResponse.hotelCountry, room))
                        }
                    }
                    vm.lastExpandedRowObservable.onNext(-1)
                    vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
                    roomContainer.startAnimation(roomContainerAlphaZeroToOneAnimation)
                }

                override fun onAnimationStart(p0: Animation?) {
                    //ignore
                }

                override fun onAnimationRepeat(p0: Animation?) {
                    //ignore
                }
            })

            roomContainer.startAnimation(roomContainerAlphaOneToZeroAnimation)
        }

        vm.ratingContainerBackground.subscribeBackground(ratingContainer)
        vm.isUserRatingAvailableObservable.filter { it }.subscribe { ratingContainer.subscribeOnClick(vm.reviewsClickedSubject) }
        vm.isUserRatingAvailableObservable.filter { !it }.subscribe { ratingContainer.unsubscribeOnClick() }

        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        etpInfoTextSmall.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        galleryContainer.subscribeOnClick(vm.galleryClickedSubject)

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            infoList.forEach { propertyTextContainer.addView(HotelInfoView(context).setText(it.name, it.content)) }
        }

        vm.sectionImageObservable.subscribe { isExpanded ->
            val values = if (hotelDescription.maxLines == HOTEL_DESC_COLLAPSE_LINES) hotelDescription.lineCount else HOTEL_DESC_COLLAPSE_LINES
            var animation = ObjectAnimator.ofInt(hotelDescription, "maxLines", values)

            animation.setDuration(DESCRIPTION_ANIMATION).start()

            if (isExpanded) {
                AnimUtils.rotate(readMoreView)
            } else {
                AnimUtils.reverseRotate(readMoreView)
            }

        }

        vm.sectionImageObservable.subscribe { isExpanded ->
            if (isExpanded) AnimUtils.rotate(readMoreView) else AnimUtils.reverseRotate(readMoreView)
        }
        vm.galleryClickedSubject.subscribe { detailContainer.animateScrollY(detailContainer.scrollY, -initialScrollTop, 500) }
        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        gallery.stopFlipping()
    }

    fun resetViews() {
        detailContainer.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        AnimUtils.reverseRotate(readMoreView)
        hotelDescription.maxLines = HOTEL_DESC_COLLAPSE_LINES
        renovationContainer.visibility = View.GONE
        etpContainer.visibility = View.GONE
        etpContainerDropShadow.visibility = View.GONE
        etpAndFreeCancellationMessagingContainer.visibility = View.GONE
        hotelDetailsToolbar.toolBarBackground.alpha = 0f
        hotelDetailsToolbar.toolBarGradient.translationY = 0f
        priceViewAlpha(1f)
        urgencyViewAlpha(1f)
        hotelGalleryDescriptionContainer.alpha = 0f
        resortFeeWidget.visibility = View.GONE
        roomRateHeader.visibility = View.GONE
        commonAmenityText.visibility = View.GONE
        roomRateRegularLoyaltyAppliedView.visibility = View.GONE
        roomRateVIPLoyaltyAppliedContainer.visibility = View.GONE
        commonAmenityDivider.visibility = View.GONE
        hideResortandSelectRoom()
        freeCancellationAndETPMessaging.visibility = View.GONE
        singleMessageContainer.visibility = View.GONE
        viewmodel.onGalleryItemScrolled(0)
        payNowButtonContainer.unsubscribeOnClick()
        payLaterButtonContainer.unsubscribeOnClick()
        gallery.setDataSource(emptyList())
        for (index in 0..(roomContainer.childCount - 1)) {
            val room = roomContainer.getChildAt(index) as HotelRoomRateView
            recycleImageView(room.roomHeaderImage)
        }
        roomContainer.removeAllViews()
    }

    private fun hideResortandSelectRoom() {
        val activity = context as Activity
        if (!activity.intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            stickySelectRoomContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            bottomMargin = (stickySelectRoomContainer.measuredHeight - resources.getDimension(R.dimen.breakdown_text_margin)).toInt()
            resortFeeWidget.animate().translationY(resortViewHeight.toFloat()).setInterpolator(LinearInterpolator()).setDuration(ANIMATION_DURATION).start()
            stickySelectRoomContainer.animate().translationY(selectRoomContainerHeight.toFloat()).setInterpolator(DecelerateInterpolator()).start()
        } else {
            resortFeeWidget.translationY = resortViewHeight.toFloat()
            stickySelectRoomContainer.translationY = selectRoomContainerHeight.toFloat()
        }
    }

    fun spaceAboveSelectARoom() {
        val params = space.layoutParams
        params.height = bottomMargin
        space.layoutParams = params
    }

    val payNowObserver: Observer<Unit> = endlessObserver {
        //pay now show all the offers
        payNowLaterSelectionChanged(true)
        viewmodel.roomResponseListObservable.onNext(Pair(viewmodel.hotelOffersResponse.hotelRoomResponse, viewmodel.uniqueValueAddForRooms))

        if (viewmodel.hasVipAccessLoyaltyObservable.value) {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        } else if (viewmodel.hasRegularLoyaltyPointsAppliedObservable.value) {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        HotelTracking().trackPayNowContainerClick()
    }

    val payLaterObserver: Observer<Unit> = endlessObserver {
        //pay later show only etp offers
        payNowLaterSelectionChanged(false)
        viewmodel.etpRoomResponseListObservable.onNext(Pair(viewmodel.etpOffersList, viewmodel.etpUniqueValueAddForRooms))
        roomRateVIPLoyaltyAppliedContainer.visibility = View.GONE
        roomRateRegularLoyaltyAppliedView.visibility = View.GONE
        HotelTracking().trackPayLaterContainerClick()
    }

    fun payNowLaterSelectionChanged(payNowSelected: Boolean) {
        payNowButtonContainer.isSelected = payNowSelected
        payLaterButtonContainer.isSelected = !payNowSelected

        val checkMarkIcon = ContextCompat.getDrawable(context, R.drawable.sliding_radio_selector_left)
        if (payNowSelected) {
            payNowButton.setCompoundDrawablesWithIntrinsicBounds(checkMarkIcon, null, null, null)
            payLaterButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            payNowButtonContainer.unsubscribeOnClick()
            payLaterButtonContainer.subscribeOnClick(payLaterObserver)
        } else {
            payNowButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            payLaterButton.setCompoundDrawablesWithIntrinsicBounds(checkMarkIcon, null, null, null)
            payLaterButtonContainer.unsubscribeOnClick()
            payNowButtonContainer.subscribeOnClick(payNowObserver)
        }

        // Scroll to the top room in case of change in ETP selection when ETP container is sticked
        if (etpContainerDropShadow.visibility == View.VISIBLE) {
            val etpLocation = etpContainer.y + etpContainer.height
            val offsetToETP = if (roomRateHeader.visibility == View.VISIBLE) roomRateHeader.y else roomContainer.y
            val offset = offsetToETP - etpLocation
            detailContainer.smoothScrollBy(0, offset.toInt())
        }
    }

    val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        setViewVisibilities()

        // start/stop gallery when it's showing/not showing on display
        val hitRect = Rect()
        detailContainer.getHitRect(hitRect)
        val isGalleryShowingOnDisplay = gallery.getLocalVisibleRect(hitRect)
        if (isGalleryShowingOnDisplay && !gallery.isFlipping) {
            gallery.startFlipping()
        } else if (!isGalleryShowingOnDisplay && gallery.isFlipping) {
            gallery.stopFlipping()
        }
    }

    val touchListener = View.OnTouchListener { v, event ->
        val action = event.action;
        if (action == MotionEvent.ACTION_UP) {
            detailContainer.post { updateGallery(true) }
        }
        false
    }

    private fun setViewVisibilities() {
        var yoffset = detailContainer.scrollY

        updateGalleryChildrenHeights(gallery.selectedItem)
        if (yoffset - initialScrollTop >= 0) {
            galleryRoot.translationY = (yoffset - initialScrollTop) * 0.5f
        } else {
            galleryRoot.translationY = 0f
        }

        miniMapView.translationY = yoffset * 0.15f
        transparentViewOverMiniMap.translationY = miniMapView.translationY

        priceContainer.getLocationOnScreen(priceContainerLocation)
        var ratio = (priceContainerLocation[1] - (offset / 2)) / offset
        priceViewAlpha(ratio * 1.5f)

        hotelMessagingContainer.getLocationOnScreen(urgencyContainerLocation)
        var urgencyRatio = (urgencyContainerLocation[1] - (offset / 2)) / offset
        urgencyViewAlpha(urgencyRatio * 1.5f)

        if (priceContainerLocation[1] + priceContainer.height <= offset) {
            hotelDetailsToolbar.toolBarBackground.alpha = 1.0f
            hotelDetailsToolbar.toolbarShadow.visibility = View.VISIBLE
        } else {
            hotelDetailsToolbar.toolBarBackground.alpha = 0f
            hotelDetailsToolbar.toolbarShadow.visibility = View.GONE
        }

        showToolbarGradient()

        val shouldShowResortFee = shouldShowResortView()
        if (shouldShowResortFee && !resortInAnimator.isRunning && resortFeeWidget.translationY != 0f) {
            resortFeeWidget.visibility = View.VISIBLE
            resortInAnimator.start()
        } else if (!shouldShowResortFee && !resortOutAnimator.isRunning && resortFeeWidget.translationY != resortViewHeight.toFloat()) {
            resortOutAnimator.start()
        }

        shouldShowStickySelectRoomView()
        if (etpContainer.visibility == View.VISIBLE) {
            shouldShowETPContainer()
        }
        val arrowRatio = getArrowRotationRatio(yoffset)
        if (arrowRatio >= 0 && arrowRatio <= 1) {
            hotelDetailsToolbar.navIcon.parameter = 1 - arrowRatio
            hotelGalleryDescriptionContainer.alpha = 1 - arrowRatio
        }
    }

    fun priceViewAlpha(ratio: Float) {
        perNight.alpha = ratio
        price.alpha = ratio
        searchDatesInfo.alpha = ratio
        searchInfo.alpha = ratio
        selectRoomButton.alpha = ratio
        strikeThroughPrice.alpha = ratio
    }

    fun urgencyViewAlpha(ratio: Float) {
        discountPercentage.alpha = ratio
        vipAccessMessageContainer.alpha = ratio
        promoMessage.alpha = ratio
    }

    fun showToolbarGradient() {
        if (hotelMessagingContainer.visibility == View.VISIBLE)
            hotelMessagingContainer.getLocationOnScreen(priceContainerLocation)
        else
            priceContainer.getLocationOnScreen(priceContainerLocation)

        if (priceContainerLocation[1] < gradientHeight) {
            hotelDetailsToolbar.toolBarGradient.translationY = (-(gradientHeight - priceContainerLocation[1]))
        } else
            hotelDetailsToolbar.toolBarGradient.translationY = 0f
    }

    fun shouldShowResortView(): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        val isOutOfView = roomContainerPosition[1] + roomContainer.height < offset
        val isInView = roomContainerPosition[1] < screenSize.y / 2
        if (viewmodel.hotelResortFeeObservable.value != null && isInView && !isOutOfView) {
            return true
        }
        return false
    }

    fun shouldShowStickySelectRoomView() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        var selectRoomButtonOffset = offset

        if (etpContainer.visibility == View.VISIBLE) {
            selectRoomButtonOffset = (offset + (etpContainer.height) / 2)
        }
        val showStickySelectRoom = roomContainerPosition[1] + roomContainer.height < selectRoomButtonOffset

        if (showStickySelectRoom && !selectRoomInAnimator.isRunning && stickySelectRoomContainer.translationY != 0f) {
            selectRoomInAnimator.start()
        } else if (!showStickySelectRoom && !selectRoomOutAnimator.isRunning && stickySelectRoomContainer.translationY != selectRoomContainerHeight.toFloat()) {
            selectRoomOutAnimator.start()
        }
    }

    fun shouldShowETPContainer() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        if (roomContainerPosition[1] + roomContainer.height < offset + etpContainer.height) {
            etpContainer.isEnabled = false
        } else
            etpContainer.isEnabled = true
    }

    fun scrollToRoom(animate: Boolean) {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        var scrollToAmount = roomContainerPosition[1] - offset + detailContainer.scrollY
        if (etpContainer.visibility == View.VISIBLE) scrollToAmount -= etpContainer.height
        if (roomRateHeader.visibility == View.VISIBLE) scrollToAmount -= roomRateHeader.height
        val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.scrollY, scrollToAmount.toInt())

        smoothScrollAnimation.duration = if (animate) SELECT_ROOM_ANIMATION else 0
        smoothScrollAnimation.interpolator = (AccelerateDecelerateInterpolator())
        smoothScrollAnimation.addUpdateListener({ animation ->
            val scrollTo = animation.animatedValue as Int
            detailContainer.scrollTo(0, scrollTo)
        })

        smoothScrollAnimation.start()

        //request focus for accessibility on first room row after scrolling
        (roomContainer.getChildAt(0) as HotelRoomRateView).row.requestFocus()
    }


    init {
        View.inflate(getContext(), R.layout.widget_hotel_detail, this)
        gallery.addImageViewCreatedListener({ index -> updateGalleryChildrenHeights(index) })
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        Ui.showTransparentStatusBar(getContext())

        offset = statusBarHeight.toFloat() + toolBarHeight
        hotelDetailsToolbar.toolbar.setNavigationOnClickListener { view ->
            if (hotelDetailsToolbar.navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
                updateGallery(false)
            } else
                (getContext() as Activity).onBackPressed()
        }

        //share hotel listing text view set up drawable
        val phoneIconDrawable = ContextCompat.getDrawable(context, R.drawable.detail_phone).mutate()
        phoneIconDrawable.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)
        selectRoomButton.setOnClickListener {
            scrollToRoom(true)
            trackSelectRoomClick(false)
        }
        stickySelectRoomButton.setOnClickListener {
            scrollToRoom(true)
            trackSelectRoomClick(true)
        }
        resortFeeWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        resortViewHeight = resortFeeWidget.measuredHeight
        resortInAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", resortViewHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        resortOutAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", 0f, resortViewHeight.toFloat()).setDuration(ANIMATION_DURATION)

        stickySelectRoomContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        selectRoomContainerHeight = stickySelectRoomContainer.measuredHeight
        selectRoomInAnimator = ObjectAnimator.ofFloat(stickySelectRoomContainer, "translationY", selectRoomContainerHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        selectRoomOutAnimator = ObjectAnimator.ofFloat(stickySelectRoomContainer, "translationY", 0f, selectRoomContainerHeight.toFloat()).setDuration(ANIMATION_DURATION)

        hideResortandSelectRoom()

        FontCache.setTypeface(payNowButton, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(payLaterButton, FontCache.Font.ROBOTO_REGULAR)
    }

    private fun trackSelectRoomClick(isStickyButton: Boolean) {
        viewmodel.trackHotelDetailSelectRoomClick(isStickyButton)
    }

    fun refresh() {
        detailContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        resetGallery()
    }

    fun resetGallery() {
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

    fun updateGallery(toFullScreen: Boolean) {
        if (detailContainer.isFlinging) {
            return
        }

        val fromY = detailContainer.scrollY
        val threshold = initialScrollTop / 2
        //In case of slow scrolling, if gallery view is expanding more than halfway then scrollTo full screen else scrollTo initialScroollTop
        if ((toFullScreen && fromY > threshold && fromY < initialScrollTop) || (!toFullScreen)) {
            detailContainer.animateScrollY(fromY, initialScrollTop, ANIMATION_DURATION)
        } else if (fromY < threshold ) {
            detailContainer.animateScrollY(fromY, 0, ANIMATION_DURATION)
        }
    }

    fun getArrowRotationRatio(scrollY: Int): Float {
        return scrollY.toFloat() / (initialScrollTop)
    }

    companion object {
        val zeroSaturationColorMatrixColorFilter: ColorMatrixColorFilter by lazy {
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f)
            ColorMatrixColorFilter(colorMatrix)
        }
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
                holder = holder as RecyclerGallery.RecyclerAdapter.ViewHolder
                holder.mImageView?.setIntermediateValue(height - initialScrollTop, height,
                        detailContainer.scrollY.toFloat() / initialScrollTop)
            }
        }
    }

    fun recycleImageView(imageView: ImageView) {
        imageView.drawable?.callback = null
        imageView.setImageDrawable(null)
    }

    private fun displayRoomRateHeader() {
        roomRateHeader.visibility = View.VISIBLE
        commonAmenityDivider.visibility = View.VISIBLE
    }

    val rowAnimation = endlessObserver<Pair<HotelRoomRateView, Boolean>> { pair ->
        val room = pair.first
        val animate = pair.second
        val resizeAnimator = ResizeHeightAnimator(if (animate) ANIMATION_DURATION else 0)
        resizeAnimator.addViewSpec(room.roomHeaderImageContainer, room.roomHeaderImageHeight)
        resizeAnimator.addViewSpec(room.roomInfoHeader, room.roomInfoHeaderTextHeight)
        resizeAnimator.addViewSpec(room.roomInfoDivider, room.roomInfoDividerHeight)
        resizeAnimator.addViewSpec(room.roomInfoChevron, room.roomInfoChevronHeight)
        resizeAnimator.addViewSpec(room.spaceAboveRoomInfo, room.spaceAboveRoomInfoHeight)
        if (animate) {
            resizeAnimator.addUpdateListener({
                val rowTopConstraintView = if (viewmodel.hasETPObservable.value) etpContainer else hotelDetailsToolbar

                val screenHeight = AndroidUtils.getScreenSize(context).y
                val location = IntArray(2)

                room.row.getLocationOnScreen(location)
                val rowLocationTopY = location[1]
                val rowLocationBottomY = rowLocationTopY + room.row.height

                rowTopConstraintView.getLocationOnScreen(location)
                val rowTopConstraintViewBottomY = location[1] + rowTopConstraintView.height

                if (rowLocationBottomY > screenHeight) {
                    detailContainer.smoothScrollBy(0, rowLocationBottomY - screenHeight)
                } else if (rowLocationTopY < rowTopConstraintViewBottomY) {
                    detailContainer.smoothScrollBy(0, rowLocationTopY - rowTopConstraintViewBottomY)
                }
            })
        }
        resizeAnimator.start()
    }
}
