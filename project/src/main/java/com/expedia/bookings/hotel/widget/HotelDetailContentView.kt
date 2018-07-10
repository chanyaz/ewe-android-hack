package com.expedia.bookings.hotel.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Handler
import android.support.annotation.VisibleForTesting
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.Html
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
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
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.subscribeBackground
import com.expedia.bookings.extensions.subscribeBackgroundResource
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.extensions.unsubscribeOnClick
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.activity.HotelGalleryActivity
import com.expedia.bookings.hotel.activity.HotelGalleryGridActivity
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.data.HotelGalleryAnalyticsData
import com.expedia.bookings.hotel.data.HotelGalleryConfig
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.fragment.ChangeDatesDialogFragment
import com.expedia.bookings.hotel.map.HotelMapLiteWidget
import com.expedia.bookings.hotel.vm.HotelDetailViewModel
import com.expedia.bookings.hotel.vm.HotelReviewsSummaryBoxRatingViewModel
import com.expedia.bookings.hotel.vm.HotelReviewsSummaryViewModel
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.DESCRIPTION_ANIMATION
import com.expedia.bookings.widget.HOTEL_DESC_COLLAPSE_LINES
import com.expedia.bookings.widget.HotelInfoView
import com.expedia.bookings.widget.HotelRoomCardView
import com.expedia.bookings.widget.HotelRoomDetailView
import com.expedia.bookings.widget.HotelRoomHeaderView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.HotelRoomDetailViewModel
import com.expedia.vm.HotelRoomHeaderViewModel
import com.google.android.gms.maps.model.LatLng
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

class HotelDetailContentView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    val requestFocusOnRoomsSubject = PublishSubject.create<Unit>()

    @VisibleForTesting val hotelMessagingContainer: ConstraintLayout by bindView(R.id.promo_messaging_container)
    @VisibleForTesting val promoMessage: TextView by bindView(R.id.promo_text)
    val memberOnlyDealTag: ImageView by bindView(R.id.member_only_deal_tag)
    @VisibleForTesting val discountPercentage: TextView by bindView(R.id.discount_percentage)
    @VisibleForTesting val airAttachImage: ImageView by bindView(R.id.air_attach_image)
    @VisibleForTesting val addOnAttachImage: ImageView by bindView(R.id.add_on_attach_image)
    @VisibleForTesting val vipAccessMessageContainer: LinearLayout by bindView(R.id.vip_access_message_container)

    private val vipLoyaltyMessage: TextView by bindView(R.id.vip_loyalty_message_details)
    private val regularLoyaltyMessage: TextView by bindView(R.id.regular_loyalty_applied)

    @VisibleForTesting val priceContainer: ViewGroup by bindView(R.id.price_widget)
    @VisibleForTesting val detailsSoldOut: TextView by bindView(R.id.details_sold_out)

    private val hotelPriceContainer: View by bindView(R.id.hotel_price_container)
    @VisibleForTesting val price: TextView by bindView(R.id.price)
    private val pricePerDescriptor: TextView by bindView(R.id.price_per_descriptor)
    @VisibleForTesting val detailedPriceType: TextView by bindView(R.id.detailed_price_type)
    @VisibleForTesting val detailedPriceIncludesMessage: TextView by bindView(R.id.detailed_price_includes_taxes)

    @VisibleForTesting val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    @VisibleForTesting val searchInfo: TextView by bindView(R.id.hotel_search_info)
    private val searchInfoGuests: TextView by bindView(R.id.hotel_search_info_guests)
    private val earnMessage: TextView by bindView(R.id.earn_message)

    private val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    private val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    private val userRating: TextView by bindView(R.id.user_rating)
    private val userRatingRecommendationText: TextView by bindView(R.id.user_rating_recommendation_text)
    private val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    private val barRatingView: HotelReviewsBarRatingView by bindView(R.id.reviews_bar_rating_view)

    private val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)

    @VisibleForTesting val singleMessageContainer: ViewGroup by bindView(R.id.single_message_container)
    private val freeCancellation: TextView by bindView(R.id.free_cancellation)
    private val etpInfoText: TextView by bindView(R.id.etp_info_text)
    private val etpInfoTextSmall: TextView by bindView(R.id.etp_info_text_small)

    @VisibleForTesting val freeCancellationAndETPMessaging: ViewGroup by bindView(R.id.free_cancellation_etp_messaging)
    private val hotelDescriptionContainer: ViewGroup by bindView(R.id.hotel_description_container)
    private val hotelDescription: TextView by bindView(R.id.body_text)
    private val readMoreView: ImageButton by bindView(R.id.read_more)
    private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    private val amenityEtpDivider: View by bindView(R.id.etp_and_free_cancellation_divider)
    private val liteMapView: HotelMapLiteWidget by bindView(R.id.hotel_lite_details_map)

    private val roomRateHeader: LinearLayout by bindView(R.id.room_rate_header)
    private val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    private val roomRateRegularLoyaltyAppliedView: LinearLayout by bindView(R.id.room_rate_regular_loyalty_applied_container)
    private val roomRateVIPLoyaltyAppliedContainer: View by bindView(R.id.room_rate_vip_loyalty_applied_container)
    private val commonAmenityDivider: View by bindView(R.id.common_amenities_divider)

    @VisibleForTesting val roomContainer: LinearLayout by bindView(R.id.room_container)

    private val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)
    private val renovationContainer: ViewGroup by bindView(R.id.renovation_container)
    private val renovationBottomDivider: View by bindView(R.id.renovation_bottom_divider)

    private val payNowPayLaterTabs: PayNowPayLaterTabs by bindView(R.id.pay_now_pay_later_tabs)
    private val payByPhoneContainer: ViewGroup by bindView(R.id.book_by_phone_container)
    private val payByPhoneTextView: TextView by bindView(R.id.book_by_phone_text)

    private val space: Space by bindView(R.id.bottom_bar_spacer)

    private var isHotelDescriptionExpanded = false
    private var dialogFragment: ChangeDatesDialogFragment? = null

    private var priceContainerLocation = IntArray(2)
    private var roomContainerPosition = IntArray(2)
    private var urgencyContainerLocation = IntArray(2)

    private val ANIMATION_DURATION_ROOM_CONTAINER = if (ExpediaBookingApp.isAutomation()) 0L else 250L

    var reviewsSummaryViewModel: HotelReviewsSummaryViewModel by notNullAndObservable { vm ->
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelUGCReviewsBoxRatingDesign)) {
            vm.reviewSummarySubject.subscribe(barRatingView.viewModel.reviewsSummaryObserver)
            vm.noReviewSummarySubject.subscribe(barRatingView.viewModel.noReviewsSummaryObserver)
        } else {
            barRatingView.visibility = View.GONE
        }
    }

    init {
        View.inflate(context, R.layout.hotel_detail_content_view, this)

        val phoneIconDrawable = ContextCompat.getDrawable(context, R.drawable.detail_phone)!!.mutate()
        phoneIconDrawable.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)

        AccessibilityUtil.appendRoleContDesc(etpInfoTextSmall, etpInfoTextSmall.text.toString(), R.string.accessibility_cont_desc_role_button)

        hotelDescriptionContainer.setAccessibilityDelegate(object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
                super.onInitializeAccessibilityNodeInfo(host, info)

                if (readMoreView.visibility == View.VISIBLE) {
                    val description: String
                    if (isHotelDescriptionExpanded) {
                        description = context.resources.getString(R.string.show_less)
                    } else {
                        setHotelDescriptionContainerA11y()
                        description = context.resources.getString(R.string.show_more)
                    }
                    val customClick = AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, description)
                    info?.addAction(customClick)
                }
            }
        })

        payNowPayLaterTabs.payNowClickedSubject.subscribe { payNowClicked() }
        payNowPayLaterTabs.payLaterClickedSubject.subscribe { payLaterClicked() }

        barRatingView.viewModel = HotelReviewsSummaryBoxRatingViewModel(context)
    }

    var viewModel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        vm.hotelSoldOut.subscribeVisibility(detailsSoldOut)
        vm.hotelSoldOut.subscribeInverseVisibility(price)
        vm.hotelSoldOut.subscribeInverseVisibility(roomContainer)

        vm.noAmenityObservable.subscribe {
            amenityContainer.visibility = View.GONE
            amenityEtpDivider.visibility = View.GONE
        }

        vm.amenitiesListObservable.subscribe { amenityList ->
            amenityContainer.visibility = View.VISIBLE
            amenityEtpDivider.visibility = View.VISIBLE
            addAmenities(amenityList)
        }

        vm.commonAmenityTextObservable.subscribe { text ->
            displayRoomRateHeader()
            commonAmenityText.visibility = View.VISIBLE
            commonAmenityText.text = HtmlCompat.fromHtml(text)
        }

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(roomList.first)) {
                return@subscribe
            }
            updateRooms(roomList.first, false)
        }

        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(etpRoomList.first)) {
                return@subscribe
            }
            updateRooms(etpRoomList.first, true)
        }

        vm.hasVipLoyaltyPointsAppliedObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        }

        vm.hasRegularLoyaltyPointsAppliedObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        liteMapView.hotelMapClickedSubject.subscribe(vm.mapClickedSubject)
        vm.renovationObservable.subscribeVisibility(renovationContainer)
        vm.renovationObservable.subscribeVisibility(renovationBottomDivider)

        vm.sectionBodyObservable.subscribe { htmlBodyText -> setHotelDescriptionText(htmlBodyText) }

        vm.strikeThroughPriceObservable.subscribeText(strikeThroughPrice)
        vm.strikeThroughPriceVisibility.subscribeVisibility(strikeThroughPrice)
        vm.priceToShowCustomerObservable.subscribeText(price)
        vm.roomPriceToShowCustomer.subscribeText(price)
        vm.searchInfoObservable.subscribeText(searchInfo)
        vm.searchInfoTextColorObservable.subscribeTextColor(searchInfo)
        vm.searchInfoGuestsObservable.subscribeText(searchInfoGuests)
        vm.perNightVisibility.subscribeVisibility(pricePerDescriptor)
        pricePerDescriptor.text = vm.pricePerDescriptor()

        detailedPriceType.visibility = if (vm.shouldDisplayDetailedPricePerDescription()) View.VISIBLE else View.GONE
        detailedPriceIncludesMessage.visibility = if (vm.shouldDisplayDetailedPricePerDescription() && PointOfSale.getPointOfSale().supportsPackagesHSRIncludesHeader()) View.VISIBLE else View.GONE

        vm.hotelPriceContentDesc.subscribeContentDescription(hotelPriceContainer)

        setUpReviewsSubscriptions(vm)

        vm.hotelLatLngObservable.subscribe { values ->
            liteMapView.setLocation(LatLng(values[0], values[1]))
        }

        vm.payByPhoneContainerVisibility.subscribeVisibility(payByPhoneContainer)
        vm.discountPercentageObservable.subscribe { discountPercentageTextAndContentDescPair ->
            discountPercentage.text = discountPercentageTextAndContentDescPair.first
            discountPercentage.contentDescription = discountPercentageTextAndContentDescPair.second
        }
        vm.memberOnlyDealTagVisibilityObservable.subscribeVisibility(memberOnlyDealTag)
        vm.discountPercentageBackgroundObservable.subscribeBackgroundResource(discountPercentage)
        vm.discountPercentageTextColorObservable.subscribeTextColor(discountPercentage)

        vm.showDiscountPercentageObservable.subscribeVisibility(discountPercentage)
        vm.showAirAttachedObservable.subscribeVisibility(airAttachImage)
        vm.showGenericAttachedObservable.subscribeVisibility(addOnAttachImage)

        vipAccessMessageContainer.subscribeOnClick(vm.vipAccessInfoObservable)

        vm.hasVipAccessObservable.subscribeVisibility(vipAccessMessageContainer)
        vm.hasVipLoyaltyPointsAppliedObservable.subscribeVisibility(vipLoyaltyMessage)
        vm.hasRegularLoyaltyPointsAppliedObservable.subscribeVisibility(regularLoyaltyMessage)
        vm.promoMessageObservable.subscribeText(promoMessage)
        vm.earnMessageObservable.subscribeText(earnMessage)
        vm.earnMessageVisibilityObservable.subscribeVisibility(earnMessage)

        vm.hotelMessagingContainerVisibility.subscribeVisibility(hotelMessagingContainer)

        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.hasETPObservable.filter { it == true }.subscribe { payNowLaterSelectionChanged(true) }
        vm.etpContainerVisibility.subscribeVisibility(payNowPayLaterTabs)

        ObservableOld.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut ->
            hasETP && hasFreeCancellation && !hotelSoldOut
        }.subscribeVisibility(freeCancellationAndETPMessaging)

        ObservableOld.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut ->
            !(hasETP && hasFreeCancellation) && !hotelSoldOut
        }.subscribeVisibility(singleMessageContainer)

        ObservableOld.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut ->
            (hasETP || hasFreeCancellation) && !hotelSoldOut
        }.subscribeVisibility(etpAndFreeCancellationMessagingContainer)

        vm.ratingContainerBackground.subscribeBackground(ratingContainer)
        vm.isUserRatingAvailableObservable.filter { it }.subscribe {
            ratingContainer.contentDescription = context.resources.getString(R.string.accessibility_cont_desc_user_rating_button)
            ratingContainer.subscribeOnClick(vm.reviewsClickObserver)
        }
        vm.isUserRatingAvailableObservable.filter { !it }.subscribe { ratingContainer.unsubscribeOnClick() }

        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        etpInfoTextSmall.subscribeOnClick(vm.payLaterInfoContainerClickObserver)

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            infoList.forEach { propertyTextContainer.addView(HotelInfoView(context).setText(it.name, it.content)) }
        }

        vm.isDatelessObservable.subscribeInverseVisibility(priceContainer)

        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)

        if (vm.isChangeDatesEnabled()) {
            searchInfo.setTintedDrawable(context.getDrawable(R.drawable.ic_edit_icon), ContextCompat.getColor(context, R.color.hotel_search_info_selectable_color))
            searchInfo.compoundDrawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
            searchInfo.setOnClickListener {
                showChangeDatesDialog()
                HotelTracking.trackInfositeChangeDateClick()
            }
        }
    }

    fun resetViews() {
        priceViewAlpha(1f)
        urgencyViewAlpha(1f)

        AnimUtils.reverseRotate(readMoreView)
        hotelDescription.maxLines = HOTEL_DESC_COLLAPSE_LINES
        renovationContainer.visibility = View.GONE
        payNowPayLaterTabs.visibility = View.GONE
        etpAndFreeCancellationMessagingContainer.visibility = View.GONE

        roomRateHeader.visibility = View.GONE
        commonAmenityText.visibility = View.GONE
        roomRateRegularLoyaltyAppliedView.visibility = View.GONE
        roomRateVIPLoyaltyAppliedContainer.visibility = View.GONE
        commonAmenityDivider.visibility = View.GONE

        freeCancellationAndETPMessaging.visibility = View.GONE
        singleMessageContainer.visibility = View.GONE

        payNowPayLaterTabs.unsubscribeClicks()

        roomContainer.removeAllViews()
        recycleRoomImageViews()
        liteMapView.reset()
    }

    fun handleScrollWithOffset(toolbarOffset: Float) {
        priceContainer.getLocationOnScreen(priceContainerLocation)
        val priceAlpha = AlphaCalculator.fadeOutAlpha(startPoint = toolbarOffset, endPoint = (toolbarOffset / 2),
                currentPoint = priceContainerLocation[1].toFloat())
        priceViewAlpha(priceAlpha)

        hotelMessagingContainer.getLocationOnScreen(urgencyContainerLocation)
        val urgencyAlpha = AlphaCalculator.fadeOutAlpha(startPoint = toolbarOffset, endPoint = toolbarOffset / 2,
                currentPoint = urgencyContainerLocation[1].toFloat())
        urgencyViewAlpha(urgencyAlpha)

        if (payNowPayLaterTabs.visibility == View.VISIBLE) {
            payNowPayLaterTabs.isEnabled = areRoomsOffScreenAboveETPToolbar(toolbarOffset)
        }
    }

    fun getPriceContainerYScreenLocation(): Int {
        if (hotelMessagingContainer.visibility == View.VISIBLE) {
            hotelMessagingContainer.getLocationOnScreen(priceContainerLocation)
        } else {
            priceContainer.getLocationOnScreen(priceContainerLocation)
        }
        return priceContainerLocation[1]
    }

    fun isRoomContainerInBounds(bottom: Float, top: Float): Boolean {
        val offScreenAboveTop = isRoomContainerAbove(top)

        roomContainer.getLocationOnScreen(roomContainerPosition)
        val onScreenAboveBottom = roomContainerPosition[1] < bottom

        return onScreenAboveBottom && !offScreenAboveTop
    }

    fun isRoomContainerAbove(top: Float): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        return roomContainerPosition[1] + roomContainer.height < top
    }

    fun getRoomContainerScrollPosition(): Int {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        var scrollToAmount = roomContainerPosition[1]
        if (payNowPayLaterTabs.visibility == View.VISIBLE) scrollToAmount -= payNowPayLaterTabs.height
        if (roomRateHeader.visibility == View.VISIBLE) scrollToAmount -= roomRateHeader.height
        return scrollToAmount
    }

    fun updateSpacer(bottomMargin: Int) {
        val params = space.layoutParams
        params.height = bottomMargin
        space.layoutParams = params
    }

    fun focusRoomsForAlly() {
        Handler().postDelayed({
            if (roomContainer.childCount >= 0) {
                val roomRateView = roomContainer.getChildAt(0)
                roomRateView?.let { roomView ->
                    val view = roomView.findViewById<View>(R.id.room_type_text_view)
                    view?.clearFocus()
                    view?.requestFocus()
                }
            }
        }, 400L)
    }

    fun showChangeDatesDialog() {
        if (dialogFragment?.isShowInitiated == true) {
            return
        }
        dialogFragment = ChangeDatesDialogFragment()

        val isDateless = viewModel.isDatelessObservable.value == true
        dialogFragment!!.datesChangedSubject.subscribe { stayDates ->
            val startDate = stayDates.getStartDate()
            val endDate = stayDates.getEndDate()
            if (startDate != null && endDate != null) {
                (viewModel as? HotelDetailViewModel)?.changeDates(startDate, endDate)
                if (isDateless) {
                    viewModel.newDatesSelected.onNext(Pair(startDate, endDate))
                }
            }
        }
        val fragmentManager = (context as FragmentActivity).supportFragmentManager

        if (!isDateless) {
            dialogFragment!!.presetDates(HotelStayDates(viewModel.checkInDate, viewModel.checkOutDate))
        }
        dialogFragment!!.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    private fun setUpReviewsSubscriptions(vm: BaseHotelDetailViewModel) {
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRating)
        vm.userRatingObservable.subscribeText(userRating)
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRatingRecommendationText)
        vm.userRatingRecommendationTextObservable.subscribeText(userRatingRecommendationText)
        vm.isUserRatingAvailableObservable.subscribeInverseVisibility(noGuestRating)

        vm.numberOfReviewsObservable.subscribeText(numberOfReviews)
        vm.isUserRatingAvailableObservable.subscribeVisibility(numberOfReviews)
    }

    private fun areRoomsOffScreenAboveETPToolbar(toolbarOffset: Float): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        return roomContainerPosition[1] + roomContainer.height >= toolbarOffset + payNowPayLaterTabs.height
    }

    private fun updateRooms(roomList: List<HotelOffersResponse.HotelRoomResponse>, payLater: Boolean) {
        val fadeRoomsOutAnimation = AlphaAnimation(1f, 0f)
        fadeRoomsOutAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER
        fadeRoomsOutAnimation.setAnimationListener(getGroupedRoomAnimationListener(roomList, payLater))
        roomContainer.startAnimation(fadeRoomsOutAnimation)
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

        val groupedRooms = viewModel.groupAndSortRoomList(roomListToUse)
        val viewModels = ArrayList<HotelRoomDetailViewModel>()
        var roomOptionCount = 0
        for ((_, roomResponses) in groupedRooms) {
            if (roomResponses.count() >= 0) {
                val cardView = Ui.inflate<HotelRoomCardView>(R.layout.hotel_room_card_view, roomContainer, false)
                var roomCount = if (roomResponses.count() > 1) 0 else -1

                val roomResponse = roomResponses[0]
                val header = getRoomHeaderView(roomResponse, roomCount)

                header.roomImageClickedSubject.subscribe(RoomImageClickObserver(roomResponse.roomGroupingKey()))
                cardView.addViewToContainer(header)

                for (roomResp in roomResponses) {
                    val hasETP = viewModel.hasETPObservable.value
                    val hotelId = viewModel.hotelOffersResponse.hotelId
                    val detail = getRoomDetailView(roomResp, hotelId, roomOptionCount, roomCount, hasETP)
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
        viewModel.hotelRoomDetailViewModelsObservable.onNext(viewModels)
    }

    private inner class RoomImageClickObserver(private val roomCode: String) : DisposableObserver<Unit>() {

        override fun onNext(t: Unit) {
            viewModel.trackHotelDetailRoomGalleryClick()

            var intent = Intent(context, HotelGalleryActivity::class.java)
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelImageGrid)) {
                intent = Intent(context, HotelGalleryGridActivity::class.java)
            }
            val galleryConfig = HotelGalleryConfig(viewModel.hotelNameObservable.value,
                    viewModel.hotelRatingObservable.value, roomCode,
                    showDescription = false, startIndex = 0)
            intent.putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig)
            val analyticsData = HotelGalleryAnalyticsData(System.currentTimeMillis(), viewModel.hotelOffersResponse.isPackage, viewModel.hotelOffersResponse.hotelId)
            intent.putExtra(HotelExtras.GALLERY_ANALYTICS_DATA, analyticsData)
            val bundle = ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle()
            context.startActivity(intent, bundle)
        }

        override fun onError(e: Throwable) {
        }

        override fun onComplete() {
        }
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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            roomTextView.text = Html.fromHtml(roomInfo, Html.FROM_HTML_MODE_COMPACT);
//        } else {
//            roomTextView.text = Html.fromHtml(roomInfo);
//        }
        roomTextView.text = roomInfo

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.room_description_title)
        builder.setView(roomTextView)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, _ ->
            dialog.dismiss()
        })

        val dialog = builder.create()
        dialog.show()
    }

    private fun getRoomDetailView(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, hotelId: String,
                                  rowIndex: Int, roomCount: Int, hasETP: Boolean): HotelRoomDetailView {

        val detailViewModel = HotelRoomDetailViewModel(context, hotelRoomResponse, hotelId, rowIndex, roomCount, hasETP)
        val detail = HotelRoomDetailView(context, detailViewModel)

        detail.hotelRoomRowClickedSubject.subscribe {
            viewModel.roomSelectedSubject.onNext(detail.viewModel.hotelRoomResponse)
            viewModel.selectedRoomIndex = detail.viewModel.rowIndex

            if (detail.viewModel.hotelRoomResponse.isPackage) {
                PackagesTracking().trackHotelRoomBookClick()
            } else {
                HotelTracking.trackLinkHotelRoomBookClick(detail.viewModel.hotelRoomResponse, detail.viewModel.hasETP)
            }

            if (detail.viewModel.hotelRoomResponse.rateInfo.chargeableRateInfo?.airAttached == true) {
                HotelTracking.trackLinkHotelAirAttachEligible(detail.viewModel.hotelRoomResponse, detail.viewModel.hotelId)
            }
        }

        detail.depositTermsClickedSubject.subscribe {
            viewModel.depositInfoContainerClickObservable.onNext(Pair(viewModel.hotelOffersResponse.hotelCountry, detail.viewModel.hotelRoomResponse))
        }

        return detail
    }

    private fun addViewToRoomContainer(roomView: View) {
        val parent = roomView.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(roomView)
        }
        roomContainer.addView(roomView)
    }

    private fun recycleRoomImageViews() {
        for (index in 0..(roomContainer.childCount - 1)) {
            val header = roomContainer.getChildAt(index) as? HotelRoomHeaderView
            header?.recycleImageView()
        }
    }

    private fun priceViewAlpha(ratio: Float) {
        pricePerDescriptor.alpha = ratio
        price.alpha = ratio
        searchInfo.alpha = ratio
        searchInfoGuests.alpha = ratio
        strikeThroughPrice.alpha = ratio
        earnMessage.alpha = ratio
        roomRateRegularLoyaltyAppliedView.alpha = ratio
        roomRateVIPLoyaltyAppliedContainer.alpha = ratio
    }

    private fun urgencyViewAlpha(ratio: Float) {
        discountPercentage.alpha = ratio
        vipAccessMessageContainer.alpha = ratio
        promoMessage.alpha = ratio
    }

    private fun displayRoomRateHeader() {
        roomRateHeader.visibility = View.VISIBLE
        commonAmenityDivider.visibility = View.VISIBLE
    }

    private fun setHotelDescriptionText(text: String) {
        hotelDescription.text = text
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

    private fun toggleHotelDescriptionContainer() {
        isHotelDescriptionExpanded = !isHotelDescriptionExpanded

        val values = if (hotelDescription.maxLines == HOTEL_DESC_COLLAPSE_LINES) hotelDescription.lineCount else HOTEL_DESC_COLLAPSE_LINES
        val animation = ObjectAnimator.ofInt(hotelDescription, "maxLines", values)

        animation.setDuration(DESCRIPTION_ANIMATION).start()

        if (isHotelDescriptionExpanded) {
            AnimUtils.rotate(readMoreView)
        } else {
            AnimUtils.reverseRotate(readMoreView)
        }
        setHotelDescriptionContainerA11y()
    }

    private fun setHotelDescriptionContainerA11y() {
        if (hotelDescription.layout != null && readMoreView.visibility == View.VISIBLE && !isHotelDescriptionExpanded) {
            val start = hotelDescription.layout.getLineStart(0)
            val end = hotelDescription.layout.getLineEnd(HOTEL_DESC_COLLAPSE_LINES - 1)

            val contentDescription = hotelDescription.text.toString().substring(start, end)

            hotelDescriptionContainer.contentDescription = contentDescription
        } else {
            hotelDescriptionContainer.contentDescription = hotelDescription.text
        }
    }

    private fun payNowClicked() {
        //pay now show all the offers
        payNowLaterSelectionChanged(true)
        viewModel.roomResponseListObservable.onNext(Pair(viewModel.hotelOffersResponse.hotelRoomResponse,
                viewModel.uniqueValueAddForRooms))

        if (viewModel.hasVipLoyaltyPointsAppliedObservable.value) {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        } else if (viewModel.hasRegularLoyaltyPointsAppliedObservable.value) {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        HotelTracking.trackPayNowContainerClick()
    }

    private fun payLaterClicked() {
        //pay later show only etp offers
        payNowLaterSelectionChanged(false)
        viewModel.etpRoomResponseListObservable.onNext(Pair(viewModel.etpOffersList,
                viewModel.etpUniqueValueAddForRooms))

        roomRateVIPLoyaltyAppliedContainer.visibility = View.GONE
        roomRateRegularLoyaltyAppliedView.visibility = View.GONE
        HotelTracking.trackPayLaterContainerClick()
    }

    private fun payNowLaterSelectionChanged(payNowSelected: Boolean) {
        if (payNowSelected) {
            payNowPayLaterTabs.selectPayNowTab()
        } else {
            payNowPayLaterTabs.selectPayLaterTab()
        }
    }

    private fun addAmenities(amenityList: List<Amenity>) {
        amenityContainer.removeAllViews()
        val srcColor = ContextCompat.getColor(context, R.color.hotelsv2_amenity_icon_color)
        val mode = PorterDuff.Mode.SRC_ATOP
        val filter = PorterDuffColorFilter(srcColor, mode)
        val paint = Paint()
        paint.colorFilter = filter

        for (index in 0..amenityList.size - 1) {

            val amenityTextView = com.mobiata.android.util.Ui.inflate<android.widget.TextView>(R.layout.new_amenity_row, amenityContainer, false)
            amenityTextView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            val amenityStr = context.getString(amenityList[index].propertyDescriptionId)
            amenityTextView.text = amenityStr
            val topDrawable = ContextCompat.getDrawable(context, amenityList[index].drawableRes)!!
            topDrawable.setBounds(0, 0, topDrawable.minimumWidth, topDrawable.minimumHeight)
            amenityTextView.setCompoundDrawables(null, topDrawable, null, null)
            amenityContainer.addView(amenityTextView)
        }
        amenityContainer.scheduleLayoutAnimation()
    }
}
