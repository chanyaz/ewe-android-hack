package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeBackground
import com.expedia.util.subscribeBackgroundColor
import com.expedia.util.subscribeBackgroundResource
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeGalleryColorFilter
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomDetailViewModel
import com.expedia.vm.HotelRoomHeaderViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.mobiata.android.util.AndroidUtils
import rx.Observable
import rx.Observer
import java.util.ArrayList
import kotlin.properties.Delegates

val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var hotelId: String by Delegates.notNull()
    private var bottomMargin = 0
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

    private val priceContainer: ViewGroup by bindView(R.id.price_widget)
    val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    val price: TextView by bindView(R.id.price)
    private val perDescriptor: TextView by bindView(R.id.per_night)
    val detailsSoldOut: TextView by bindView(R.id.details_sold_out)
    private val searchDatesInfo: TextView by bindView(R.id.search_dates_info)
    private val hotelPriceContainer: View by bindView(R.id.hotel_price_container)

    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    private val userRating: TextView by bindView(R.id.user_rating)
    private val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    private val userRatingRecommendationText: TextView by bindView(R.id.user_rating_recommendation_text)
    private val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    private val readMoreView: ImageButton by bindView(R.id.read_more)
    private val hotelDescription: TextView by bindView(R.id.body_text)
    private val hotelDescriptionContainer: ViewGroup by bindView(R.id.hotel_description_container)
    private val miniMapView: LocationMapImageView by bindView(R.id.mini_map_view)
    private val transparentViewOverMiniMap: View by bindView(R.id.transparent_view_over_mini_map)
    private val gradientHeight = context.resources.getDimension(R.dimen.hotel_detail_gradient_height)

    val hotelMessagingContainer: RelativeLayout by bindView(R.id.promo_messaging_container)
    val discountPercentage: TextView by bindView(R.id.discount_percentage)
    private val airAttachSWPImage: ImageView by bindView(R.id.air_attach_swp_image_details)
    val vipAccessMessageContainer: LinearLayout by bindView(R.id.vip_access_message_container)
    val vipLoyaltyMessage: TextView by bindView(R.id.vip_loyalty_message_details)
    val regularLoyaltyMessage: TextView by bindView(R.id.regular_loyalty_applied)
    val promoMessage: TextView by bindView(R.id.promo_text)
    private val earnMessage: TextView by bindView(R.id.earn_message)

    private val payLaterButtonContainer: FrameLayout by bindView(R.id.radius_pay_later_container)
    private val payNowButtonContainer: FrameLayout by bindView(R.id.radius_pay_now_container)
    private val payNowButton: TextView by bindView(R.id.radius_pay_now)
    private val payLaterButton: TextView by bindView(R.id.radius_pay_later)
    val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)
    val etpInfoText: TextView by bindView(R.id.etp_info_text)
    private val etpInfoTextSmall: TextView by bindView(R.id.etp_info_text_small)
    private val freeCancellation: TextView by bindView(R.id.free_cancellation)
    private val bestPriceGuarantee: TextView by bindView(R.id.best_price_guarantee)
    private val singleMessageContainer: ViewGroup by bindView(R.id.single_message_container)
    private val freeCancellationAndETPMessaging: ViewGroup by bindView(R.id.free_cancellation_etp_messaging)
    val etpContainer: HotelEtpStickyHeaderLayout by bindView(R.id.etp_placeholder)
    private val etpContainerDropShadow: View by bindView(R.id.pay_later_drop_shadow)
    private val renovationContainer: ViewGroup by bindView(R.id.renovation_container)
    private val payByPhoneTextView: TextView by bindView(R.id.book_by_phone_text)
    val payByPhoneContainer: ViewGroup by bindView(R.id.book_by_phone_container)
    private val space: Space by bindView(R.id.spacer)

    private val hotelGalleryDescriptionContainer: LinearLayout by bindView(R.id.hotel_gallery_description_container)
    private val hotelGalleryIndicator: View by bindView(R.id.hotel_gallery_indicator)
    private val hotelGalleryDescription: TextView by bindView(R.id.hotel_gallery_description)
    private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    private val amenityDivider: View by bindView(R.id.etp_and_free_cancellation_divider)

    val bottomButtonWidget: HotelBottomButtonWidget by bindView(R.id.bottom_button_widget)

    private val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)
    val roomRateHeader: LinearLayout by bindView(R.id.room_rate_header)
    private val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    val roomRateRegularLoyaltyAppliedView: LinearLayout by bindView(R.id.room_rate_regular_loyalty_applied_container)
    val roomRateVIPLoyaltyAppliedContainer: View by bindView(R.id.room_rate_vip_loyalty_applied_container)
    private val commonAmenityDivider: View by bindView(R.id.common_amenities_divider)
    val roomContainer: LinearLayout by bindView(R.id.room_container)
    private val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)

    private val detailContainer: NewHotelDetailsScrollView by bindView(R.id.detail_container)
    private var statusBarHeight = 0
    private var toolbarHeightOffset: Float by Delegates.notNull()
    private var priceContainerLocation = IntArray(2)
    private var urgencyContainerLocation = IntArray(2)
    private var roomContainerPosition = IntArray(2)

    private var resortInAnimator: ObjectAnimator by Delegates.notNull()
    private var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonInAnimator: ObjectAnimator by Delegates.notNull()
    private var bottomButtonOutAnimator: ObjectAnimator by Delegates.notNull()

    private val ANIMATION_DURATION_ROOM_CONTAINER = if (ExpediaBookingApp.isAutomation()) 0L else 250L

    private var isHotelDescriptionExpanded = false

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
        vm.hotelSoldOut.subscribeVisibility(detailsSoldOut)
        vm.hotelSoldOut.subscribeInverseVisibility(price)
        vm.hotelSoldOut.subscribeInverseVisibility(roomContainer)
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
        vm.hotelSearchInfoText.subscribeTextColor(searchInfo)

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
            commonAmenityText.text = HtmlCompat.fromHtml(text)
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
                        hotelDescriptionContainer.setOnClickListener {
                            toggleHotelDescriptionContainer()
                        }
                    }
                    setHotelDescriptionContainerA11y()
                }
            })
        }

        vm.strikeThroughPriceObservable.subscribeText(strikeThroughPrice)
        vm.strikeThroughPriceVisibility.subscribeVisibility(strikeThroughPrice)
        vm.priceToShowCustomerObservable.subscribeText(price)
        vm.roomPriceToShowCustomer.subscribeText(price)
        vm.searchInfoObservable.subscribeText(searchInfo)
        vm.perNightVisibility.subscribeInverseVisibility(perDescriptor)
        perDescriptor.text = vm.pricePerDescriptor()

        vm.hotelPriceContentDesc.subscribeContentDescription(hotelPriceContainer)
        vm.searchDatesObservable.subscribeText(searchDatesInfo)

        vm.isUserRatingAvailableObservable.subscribeVisibility(userRating)
        vm.userRatingObservable.subscribeText(userRating)
        vm.userRatingBackgroundColorObservable.subscribeBackground(userRating)
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRatingRecommendationText)
        vm.userRatingRecommendationTextObservable.subscribeText(userRatingRecommendationText)
        vm.isUserRatingAvailableObservable.map { !it }.subscribeVisibility(noGuestRating)

        vm.numberOfReviewsObservable.subscribeText(numberOfReviews)
        vm.hotelLatLngObservable.subscribe { values ->
            miniMapView.setLocation(LatLong(values[0], values[1]))
        }
        vm.payByPhoneContainerVisibility.subscribe { spaceAboveSelectARoom() }
        vm.payByPhoneContainerVisibility.subscribeVisibility(payByPhoneContainer)
        vm.discountPercentageObservable.subscribe { discountPercentageTextAndContentDescPair ->
            discountPercentage.text = discountPercentageTextAndContentDescPair.first
            discountPercentage.contentDescription = discountPercentageTextAndContentDescPair.second
        }
        vm.discountPercentageBackgroundObservable.subscribeBackgroundResource(discountPercentage)
        vm.showDiscountPercentageObservable.subscribeVisibility(discountPercentage)
        vm.showAirAttachSWPImageObservable.subscribeVisibility(airAttachSWPImage)
        vipAccessMessageContainer.subscribeOnClick(vm.vipAccessInfoObservable)
        vm.hasVipAccessObservable.subscribeVisibility(vipAccessMessageContainer)
        vm.hasVipAccessLoyaltyObservable.subscribeVisibility(vipLoyaltyMessage)
        vm.hasRegularLoyaltyPointsAppliedObservable.subscribeVisibility(regularLoyaltyMessage)
        vm.promoMessageObservable.subscribeText(promoMessage)
        vm.earnMessageObservable.subscribeText(earnMessage)
        vm.promoImageObservable.subscribe { promoImage ->
            promoMessage.setCompoundDrawablesWithIntrinsicBounds(promoImage, 0, 0, 0)
        }

        vm.earnMessageVisibilityObservable.subscribeVisibility(earnMessage)
        vm.hotelMessagingContainerVisibility.subscribeVisibility(hotelMessagingContainer)

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(roomList.first)) {
                return@subscribe
            }
            updateRooms(roomList.first, roomList.second, false)
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
            updateRooms(etpRoomList.first, etpRoomList.second, true)
        }

        vm.ratingContainerBackground.subscribeBackground(ratingContainer)
        vm.isUserRatingAvailableObservable.filter { it }.subscribe { ratingContainer.subscribeOnClick(vm.reviewsClickedSubject) }
        vm.isUserRatingAvailableObservable.filter { !it }.subscribe { ratingContainer.unsubscribeOnClick() }

        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        etpInfoTextSmall.subscribeOnClick(vm.payLaterInfoContainerClickObserver)

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            infoList.forEach { propertyTextContainer.addView(HotelInfoView(context).setText(it.name, it.content)) }
        }

        vm.galleryClickedSubject.subscribe {
            detailContainer.animateScrollY(detailContainer.scrollY, -initialScrollTop, 500)
        }

        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)
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
        //share hotel listing text view set up drawable
        val phoneIconDrawable = ContextCompat.getDrawable(context, R.drawable.detail_phone).mutate()
        phoneIconDrawable.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)

        bottomButtonWidget.selectRoomClickedSubject.subscribe {
            scrollToRoom(true)
            trackSelectRoomClick(true)
        }

        resortFeeWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        resortViewHeight = resortFeeWidget.measuredHeight
        resortInAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", resortViewHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        resortOutAnimator = ObjectAnimator.ofFloat(resortFeeWidget, "translationY", 0f, resortViewHeight.toFloat()).setDuration(ANIMATION_DURATION)
        bottomButtonWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bottomButtonContainerHeight = bottomButtonWidget.measuredHeight

        bottomButtonInAnimator = ObjectAnimator.ofFloat(bottomButtonWidget, "translationY", bottomButtonContainerHeight.toFloat(), 0f).setDuration(ANIMATION_DURATION)
        bottomButtonOutAnimator = ObjectAnimator.ofFloat(bottomButtonWidget, "translationY", 0f, bottomButtonContainerHeight.toFloat()).setDuration(ANIMATION_DURATION)

        hideResortAndSelectRoom()
        FontCache.setTypeface(payNowButton, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(payLaterButton, FontCache.Font.ROBOTO_REGULAR)

        AccessibilityUtil.appendRoleContDesc(etpInfoTextSmall, etpInfoTextSmall.text.toString(), R.string.accessibility_cont_desc_role_button)

        hotelDescriptionContainer.setAccessibilityDelegate(object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
                super.onInitializeAccessibilityNodeInfo(host, info)

                if (readMoreView.visibility == View.VISIBLE) {
                    val description: String
                    if (isHotelDescriptionExpanded) {
                        description = context.resources.getString(R.string.show_less)
                    } else {
                        description = context.resources.getString(R.string.show_more)
                    }
                    val customClick = AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, description)
                    info?.addAction(customClick)
                }
            }
        })
    }

    private val payNowClickObserver: Observer<Unit> = endlessObserver {
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

        HotelTracking.trackPayNowContainerClick()
    }

    private val payLaterClickObserver: Observer<Unit> = endlessObserver {
        //pay later show only etp offers
        payNowLaterSelectionChanged(false)
        viewmodel.etpRoomResponseListObservable.onNext(Pair(viewmodel.etpOffersList, viewmodel.etpUniqueValueAddForRooms))
        roomRateVIPLoyaltyAppliedContainer.visibility = View.GONE
        roomRateRegularLoyaltyAppliedView.visibility = View.GONE
        HotelTracking.trackPayLaterContainerClick()
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
        hideResortAndSelectRoom()
        freeCancellationAndETPMessaging.visibility = View.GONE
        singleMessageContainer.visibility = View.GONE
        viewmodel.onGalleryItemScrolled(0)
        payNowButtonContainer.unsubscribeOnClick()
        payLaterButtonContainer.unsubscribeOnClick()
        gallery.setDataSource(emptyList())
        recycleRoomImageViews()
        roomContainer.removeAllViews()
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

    private fun toggleHotelDescriptionContainer() {
        isHotelDescriptionExpanded = !isHotelDescriptionExpanded

        val values = if (hotelDescription.maxLines == HOTEL_DESC_COLLAPSE_LINES) hotelDescription.lineCount else HOTEL_DESC_COLLAPSE_LINES
        var animation = ObjectAnimator.ofInt(hotelDescription, "maxLines", values)

        animation.setDuration(DESCRIPTION_ANIMATION).start()

        if (isHotelDescriptionExpanded) {
            AnimUtils.rotate(readMoreView)
        } else {
            AnimUtils.reverseRotate(readMoreView)
        }
        setHotelDescriptionContainerA11y()
    }

    private fun setHotelDescriptionContainerA11y() {
        if (readMoreView.visibility == View.VISIBLE && !isHotelDescriptionExpanded) {
            val start = hotelDescription.layout.getLineStart(0)
            val end = hotelDescription.layout.getLineEnd(HOTEL_DESC_COLLAPSE_LINES - 1)

            var contentDescription = hotelDescription.text.toString().substring(start, end)

            hotelDescriptionContainer.contentDescription = contentDescription
        } else {
            hotelDescriptionContainer.contentDescription = hotelDescription.text
        }
    }

    private fun spaceAboveSelectARoom() {
        val params = space.layoutParams
        params.height = bottomMargin
        space.layoutParams = params
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

    private fun priceViewAlpha(ratio: Float) {
        perDescriptor.alpha = ratio
        price.alpha = ratio
        searchDatesInfo.alpha = ratio
        searchInfo.alpha = ratio
        strikeThroughPrice.alpha = ratio
        searchInfo.alpha = ratio
        earnMessage.alpha = ratio
        roomRateRegularLoyaltyAppliedView.alpha = ratio
        roomRateVIPLoyaltyAppliedContainer.alpha = ratio
        hotelDetailsToolbar.toolBarBackground.alpha = -ratio
    }

    private fun urgencyViewAlpha(ratio: Float) {
        discountPercentage.alpha = ratio
        vipAccessMessageContainer.alpha = ratio
        promoMessage.alpha = ratio
    }

    private fun showToolbarGradient() {
        if (hotelMessagingContainer.visibility == View.VISIBLE)
            hotelMessagingContainer.getLocationOnScreen(priceContainerLocation)
        else
            priceContainer.getLocationOnScreen(priceContainerLocation)

        if (priceContainerLocation[1] < gradientHeight) {
            hotelDetailsToolbar.toolBarGradient.translationY = (-(gradientHeight - priceContainerLocation[1]))
        } else
            hotelDetailsToolbar.toolBarGradient.translationY = 0f
    }

    private fun roomsVisible(): Boolean {
        roomContainer.getLocationOnScreen((roomContainerPosition))
        val roomsOffScreenAboveToolbar = roomContainerPosition[1] + roomContainer.height < toolbarHeightOffset
        val roomsOnScreenAboveCenter = roomContainerPosition[1] < screenSize.y / 2
        return roomsOnScreenAboveCenter && !roomsOffScreenAboveToolbar
    }

    private fun shouldShowResortFee(): Boolean {

        if (viewmodel.hotelResortFeeObservable.value != null && roomsVisible()) {
            return true
        }
        return false
    }

    private fun hideResortAndSelectRoom() {
        val activity = context as Activity
        bottomMargin = (bottomButtonWidget.measuredHeight - resources.getDimension(R.dimen.hotel_sticky_bottom_shadow_height)).toInt()
        if (!activity.intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            resortFeeWidget.animate().translationY(resortViewHeight.toFloat()).setInterpolator(LinearInterpolator()).setDuration(ANIMATION_DURATION).start()
        } else {
            resortFeeWidget.translationY = resortViewHeight.toFloat()
        }
    }

    private fun shouldShowETPContainer() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        etpContainer.isEnabled = roomContainerPosition[1] + roomContainer.height >= toolbarHeightOffset + etpContainer.height
    }

    private fun scrollToRoom(animate: Boolean) {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        var scrollToAmount = roomContainerPosition[1] - toolbarHeightOffset + detailContainer.scrollY
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
        Handler().postDelayed({
            if (roomContainer.childCount >= 0) {
                if (viewmodel.shouldGroupAndSortRoom()) {
                    roomContainer.getChildAt(0).requestFocus()
                } else {
                    (roomContainer.getChildAt(0) as HotelRoomRateView).row.requestFocus()
                }
            }
        }, 400L)
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
        // Hotel gallery collapsed
        if (yoffset == initialScrollTop) {
            (gallery.layoutManager as RecyclerGallery.A11yLinearLayoutManager).setCanA11yScroll(false)
            hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_cont_desc)
            gallery.prepareCollapseState(true)
            bottomButtonWidget.visibility = View.VISIBLE
            if (!bottomButtonInAnimator.isRunning && bottomButtonWidget.translationY != 0f) {
                bottomButtonInAnimator.start()
            }
        }
        // Hotel gallery expanded
        if (yoffset == 0) {
            (gallery.layoutManager as RecyclerGallery.A11yLinearLayoutManager).setCanA11yScroll(true)
            hotelDetailsToolbar.toolbar.navigationContentDescription = context.getString(R.string.toolbar_nav_icon_close_gallery_cont_desc)
            gallery.prepareCollapseState(false)
            if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat()) {
                bottomButtonOutAnimator.start()
            } else {
                bottomButtonWidget.visibility = View.GONE
            }
        }

        if (previousYOffset == 0 && yoffset >= 10) {
            detailContainer.stopNestedScroll()
            detailContainer.smoothScrollTo(0, initialScrollTop)
        }

        miniMapView.translationY = yoffset * 0.15f
        transparentViewOverMiniMap.translationY = miniMapView.translationY

        priceContainer.getLocationOnScreen(priceContainerLocation)
        var ratio = (priceContainerLocation[1] - (toolbarHeightOffset / 2)) / toolbarHeightOffset
        priceViewAlpha(ratio * 1.5f)

        hotelMessagingContainer.getLocationOnScreen(urgencyContainerLocation)
        var urgencyRatio = (urgencyContainerLocation[1] - (toolbarHeightOffset / 2)) / toolbarHeightOffset
        urgencyViewAlpha(urgencyRatio * 1.5f)

        showToolbarGradient()
        val shouldShowResortFee = shouldShowResortFee()
        if (shouldShowResortFee && !resortInAnimator.isRunning && resortFeeWidget.translationY != 0f) {
            resortFeeWidget.visibility = View.VISIBLE
            resortInAnimator.start()
        } else if (!shouldShowResortFee && !resortOutAnimator.isRunning && resortFeeWidget.translationY != resortViewHeight.toFloat()) {
            resortOutAnimator.start()
        }

        if (!bottomButtonInAnimator.isRunning && bottomButtonWidget.translationY != 0f && !roomsVisible() && !viewmodel.hotelSoldOut.value) {
            bottomButtonInAnimator.start()
        } else if (!bottomButtonOutAnimator.isRunning && bottomButtonWidget.translationY != bottomButtonContainerHeight.toFloat() && roomsVisible() && !viewmodel.hotelSoldOut.value) {
            bottomButtonOutAnimator.start()
        }

        if (etpContainer.visibility == View.VISIBLE) {
            shouldShowETPContainer()
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

    private fun payNowLaterSelectionChanged(payNowSelected: Boolean) {
        payNowButtonContainer.isSelected = payNowSelected
        payLaterButtonContainer.isSelected = !payNowSelected

        val checkMarkIcon = ContextCompat.getDrawable(context, R.drawable.sliding_radio_selector_left)
        if (payNowSelected) {
            payNowButton.setCompoundDrawablesWithIntrinsicBounds(checkMarkIcon, null, null, null)
            payLaterButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            payNowButtonContainer.unsubscribeOnClick()
            payLaterButtonContainer.subscribeOnClick(payLaterClickObserver)
        } else {
            payNowButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            payLaterButton.setCompoundDrawablesWithIntrinsicBounds(checkMarkIcon, null, null, null)
            payLaterButtonContainer.unsubscribeOnClick()
            payNowButtonContainer.subscribeOnClick(payNowClickObserver)
        }

        // Scroll to the top room in case of change in ETP selection when ETP container is sticked
        if (etpContainerDropShadow.visibility == View.VISIBLE) {
            val etpLocation = etpContainer.y + etpContainer.height
            val offsetToETP = if (roomRateHeader.visibility == View.VISIBLE) roomRateHeader.y else roomContainer.y
            val offset = offsetToETP - etpLocation
            detailContainer.smoothScrollBy(0, offset.toInt())
        }
    }

    private fun displayRoomRateHeader() {
        roomRateHeader.visibility = View.VISIBLE
        commonAmenityDivider.visibility = View.VISIBLE
    }

    private fun updateRooms(roomList: List<HotelOffersResponse.HotelRoomResponse>, topValueAddList: List<String>,
                            payLater: Boolean) {
        val fadeRoomsOutAnimation = AlphaAnimation(1f, 0f)
        fadeRoomsOutAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER
        if (viewmodel.shouldGroupAndSortRoom()) {
            fadeRoomsOutAnimation.setAnimationListener(getGroupedRoomAnimationListener(roomList, payLater))
        } else {
            fadeRoomsOutAnimation.setAnimationListener(getRoomAnimationListener(roomList, topValueAddList, payLater))
        }
        roomContainer.startAnimation(fadeRoomsOutAnimation)
    }

    private fun getRoomHeaderView(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, roomCount: Int): HotelRoomHeaderView {
        val headerViewModel = HotelRoomHeaderViewModel(context, hotelRoomResponse, roomCount)

        val header = HotelRoomHeaderView(context, headerViewModel)

        header.roomInfoClickedSubject.subscribe {
            showRoomDescriptionDialog(headerViewModel.roomDescriptionString)
        }

        return header
    }

    private fun showRoomDescriptionDialog(roomInfo: String?) {
        if (roomInfo.isNullOrBlank()) {
            return
        }
        val roomTextView = View.inflate(context, R.layout.room_description_dialog, null) as android.widget.TextView
        roomTextView.text = roomInfo

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.room_description_title)
        builder.setView(roomTextView)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, which ->
            dialog.dismiss()
        })

        val dialog = builder.create()
        dialog.show()
    }

    private fun getRoomDetailView(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String, rowIndex: Int, roomCount: Int, hasETP: Boolean): HotelRoomDetailView {


        val detailViewModel = HotelRoomDetailViewModel(context, hotelRoomResponse, hotelId, rowIndex, roomCount, hasETP)

        val detail = HotelRoomDetailView(context, detailViewModel)

        detail.hotelRoomRowClickedSubject.subscribe {
            viewmodel.roomSelectedSubject.onNext(detail.viewModel.hotelRoomResponse)
            viewmodel.selectedRoomIndex = detail.viewModel.rowIndex

            if (detail.viewModel.hotelRoomResponse.isPackage) {
                PackagesTracking().trackHotelRoomBookClick()
            } else {
                HotelTracking.trackLinkHotelRoomBookClick(detail.viewModel.hotelRoomResponse, detail.viewModel.hasETP)
            }

            if (detail.viewModel.hotelRoomResponse.rateInfo.chargeableRateInfo?.airAttached ?: false) {
                HotelTracking.trackLinkHotelAirAttachEligible(detail.viewModel.hotelRoomResponse, detail.viewModel.hotelId)
            }
        }

        detail.depositTermsClickedSubject.subscribe {
            viewmodel.depositInfoContainerClickObservable.onNext(Pair(viewmodel.hotelOffersResponse.hotelCountry, detail.viewModel.hotelRoomResponse))
        }

        return detail
    }

    private fun getGroupedRoomAnimationListener(roomList: List<HotelOffersResponse.HotelRoomResponse>, payLater: Boolean): Animation.AnimationListener {
        val fadeOutRoomListener = object : AnimationListenerAdapter() {
            override fun onAnimationEnd(p0: Animation?) {
                createGroupedRoomViews(roomList, payLater)
            }
        }
        return fadeOutRoomListener
    }

    private fun createGroupedRoomViews(roomList: List<HotelOffersResponse.HotelRoomResponse>, payLater: Boolean) {
        val fadeInRoomsAnimation = AlphaAnimation(0f, 1f)
        fadeInRoomsAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER

        recycleRoomImageViews()
        roomContainer.removeAllViews()

        val roomListToUse = ArrayList<HotelOffersResponse.HotelRoomResponse>()

        roomList.forEach { room ->
            if (payLater) {
                roomListToUse.add(room.payLaterOffer)
            } else {
                roomListToUse.add(room)
            }
        }

        val groupedRooms = viewmodel.groupAndSortRoomList(roomListToUse)
        val viewModels = ArrayList<HotelRoomDetailViewModel>()
        var roomOptionCount = 0
        for ((roomType, roomResponses) in groupedRooms) {
            if (roomResponses.count() >= 0) {
                val cardView = Ui.inflate<HotelRoomCardView>(R.layout.hotel_room_card_view, roomContainer, false)
                var roomCount = if (roomResponses.count() > 1) 0 else -1

                val header = getRoomHeaderView(roomResponses[0], roomCount)
                cardView.addViewToContainer(header)

                for (roomResponse in roomResponses) {
                    val hasETP = viewmodel.hasETPObservable.value
                    val hotelId = viewmodel.hotelOffersResponse.hotelId
                    val detail = getRoomDetailView(roomResponse, hotelId, roomOptionCount, roomCount, hasETP)
                    viewModels.add(detail.viewModel)
                    cardView.addViewToContainer(detail)
                    roomOptionCount++
                    roomCount++
                }

                addViewToRoomContainer(cardView)

                View.inflate(context, R.layout.grey_divider_bar, roomContainer)
            }
        }

        roomContainer.startAnimation(fadeInRoomsAnimation)
        viewmodel.hotelRoomDetailViewModelsObservable.onNext(viewModels)
    }

    private fun getRoomAnimationListener(roomList: List<HotelOffersResponse.HotelRoomResponse>, topValueAddList: List<String>,
                                         payLater: Boolean): Animation.AnimationListener {
        val fadeOutRoomListener = object : AnimationListenerAdapter() {
            override fun onAnimationEnd(p0: Animation?) {
                createRoomViews(roomList, topValueAddList, payLater)
            }
        }
        return fadeOutRoomListener
    }

    private fun createRoomViews(roomList: List<HotelOffersResponse.HotelRoomResponse>, topValueAddList: List<String>, payLater: Boolean) {
        val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(roomList.size)
        val fadeInRoomsAnimation = AlphaAnimation(0f, 1f)
        fadeInRoomsAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER

        fadeInRoomsAnimation.setAnimationListener(object : AnimationListenerAdapter() {
            override fun onAnimationStart(animation: Animation?) {
                if (!Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelRoomRateExpanded)) {
                    hotelRoomRateViewModels.first().expandRoomObservable.onNext(Unit)
                    hotelRoomRateViewModels.drop(1).forEach { vm -> vm.collapseRoomObservable.onNext(Unit) }
                } else {
                    hotelRoomRateViewModels.forEach { vm -> vm.expandRoomObservable.onNext(Unit) }
                }
            }
        })

        recycleRoomImageViews()
        roomContainer.removeAllViews()

        roomList.forEachIndexed { roomResponseIndex, room ->
            val roomOffer = if (payLater) room.payLaterOffer else room
            val view = getHotelRoomRowView(roomResponseIndex, roomOffer, topValueAddList[roomResponseIndex])
            addViewToRoomContainer(view)
            hotelRoomRateViewModels.add(view.viewModel)
        }
        viewmodel.lastExpandedRowIndexObservable.onNext(-1)
        viewmodel.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
        roomContainer.startAnimation(fadeInRoomsAnimation)

        //set focus on first room row for accessibility
        (roomContainer.getChildAt(0) as HotelRoomRateView).row.isFocusableInTouchMode = true
    }

    private fun recycleRoomImageViews() {
        val groupedRoom = viewmodel.shouldGroupAndSortRoom()
        for (index in 0..(roomContainer.childCount - 1)) {
            if (groupedRoom) {
                val header = roomContainer.getChildAt(index) as? HotelRoomHeaderView
                header?.recycleImageView()
            } else {
                val room = roomContainer.getChildAt(index) as HotelRoomRateView
                recycleImageView(room.roomHeaderImage)
            }
        }
    }

    private fun recycleImageView(imageView: ImageView) {
        imageView.drawable?.callback = null
        imageView.setImageDrawable(null)
    }

    private fun addViewToRoomContainer(roomView: View) {
        var parent = roomView.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(roomView)
        }
        roomContainer.addView(roomView)
    }

    private fun getHotelRoomRowView(roomIndex: Int, roomResponse: HotelOffersResponse.HotelRoomResponse,
                                    uniqueValueAdd: String): HotelRoomRateView {
        val hasETP = viewmodel.hasETPObservable.value
        val view = HotelRoomRateView(context)
        view.viewModel = HotelRoomRateViewModel(context, viewmodel.hotelOffersResponse.hotelId,
                roomResponse, uniqueValueAdd, roomIndex,
                viewmodel.rowExpandingObservable, hasETP, viewmodel.getLOB())
        view.animateRoom.subscribe(rowAnimation)
        view.viewModel.depositTermsClickedObservable.subscribe {
            viewmodel.depositInfoContainerClickObservable.onNext(Pair(viewmodel.hotelOffersResponse.hotelCountry, roomResponse))
        }
        view.viewModel.roomSelectedObservable.subscribe { roomPair ->
            val (index, roomResponse) = roomPair
            viewmodel.roomSelectedSubject.onNext(roomResponse)
            viewmodel.selectedRoomIndex = index
        }
        return view
    }

    private val rowAnimation = endlessObserver<Pair<HotelRoomRateView, Boolean>> { pair ->
        val room = pair.first
        val animate = pair.second
        val resizeAnimator = ResizeHeightAnimator(if (animate) ANIMATION_DURATION else 0)
        resizeAnimator.addViewSpec(room.roomHeaderImageContainer, room.roomHeaderImageHeight)
        resizeAnimator.addViewSpec(room.roomInfoHeader, room.roomInfoHeaderTextHeight)
        resizeAnimator.addViewSpec(room.roomInfoDivider, room.roomInfoDividerHeight)
        resizeAnimator.addViewSpec(room.roomInfoChevron, room.roomInfoChevronHeight)
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

    private fun trackSelectRoomClick(isStickyButton: Boolean) {
        viewmodel.trackHotelDetailSelectRoomClick(isStickyButton)
    }
}
