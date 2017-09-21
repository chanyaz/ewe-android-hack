package com.expedia.bookings.hotel.widget

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Handler
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
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
import com.expedia.bookings.data.cars.LatLong
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.animation.AlphaCalculator
import com.expedia.bookings.hotel.fragment.ChangeDatesDialogFragment
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.DESCRIPTION_ANIMATION
import com.expedia.bookings.widget.HOTEL_DESC_COLLAPSE_LINES
import com.expedia.bookings.widget.HotelInfoView
import com.expedia.bookings.widget.HotelRoomCardView
import com.expedia.bookings.widget.HotelRoomDetailView
import com.expedia.bookings.widget.HotelRoomHeaderView
import com.expedia.bookings.widget.HotelRoomRateView
import com.expedia.bookings.widget.LocationMapImageView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeBackground
import com.expedia.util.subscribeBackgroundResource
import com.expedia.util.subscribeContentDescription
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
import rx.Observable
import rx.subjects.PublishSubject
import java.util.ArrayList

class HotelDetailContentView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    val requestFocusOnRoomsSubject = PublishSubject.create<Unit>()

    @VisibleForTesting val hotelMessagingContainer: RelativeLayout by bindView(R.id.promo_messaging_container)
    @VisibleForTesting val promoMessage: TextView by bindView(R.id.promo_text)
    @VisibleForTesting val discountPercentage: TextView by bindView(R.id.discount_percentage)
    @VisibleForTesting val airAttachSWPImage: ImageView by bindView(R.id.air_attach_swp_image_details)
    @VisibleForTesting val vipAccessMessageContainer: LinearLayout by bindView(R.id.vip_access_message_container)

    private val vipLoyaltyMessage: TextView by bindView(R.id.vip_loyalty_message_details)
    private val regularLoyaltyMessage: TextView by bindView(R.id.regular_loyalty_applied)

    private val priceContainer: ViewGroup by bindView(R.id.price_widget)
    @VisibleForTesting val detailsSoldOut: TextView by bindView(R.id.details_sold_out)

    private val hotelPriceContainer: View by bindView(R.id.hotel_price_container)
    @VisibleForTesting val price: TextView by bindView(R.id.price)
    private val pricePerDescriptor: TextView by bindView(R.id.price_per_descriptor)
    @VisibleForTesting val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    @VisibleForTesting val searchInfo: TextView by bindView(R.id.hotel_search_info)
    private val earnMessage: TextView by bindView(R.id.earn_message)
    private val taxFeeDescriptor: TextView by bindView(R.id.tax_fee_descriptor)

    private val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    private val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    private val userRating: TextView by bindView(R.id.user_rating)
    private val userRatingRecommendationText: TextView by bindView(R.id.user_rating_recommendation_text)
    private val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    private val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)

    @VisibleForTesting val singleMessageContainer: ViewGroup by bindView(R.id.single_message_container)
    private val freeCancellation: TextView by bindView(R.id.free_cancellation)
    private val bestPriceGuarantee: TextView by bindView(R.id.best_price_guarantee)
    private val etpInfoText: TextView by bindView(R.id.etp_info_text)
    private val etpInfoTextSmall: TextView by bindView(R.id.etp_info_text_small)

    @VisibleForTesting val freeCancellationAndETPMessaging: ViewGroup by bindView(R.id.free_cancellation_etp_messaging)
    private val hotelDescriptionContainer: ViewGroup by bindView(R.id.hotel_description_container)
    private val hotelDescription: TextView by bindView(R.id.body_text)
    private val readMoreView: ImageButton by bindView(R.id.read_more)
    private val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    private val amenityEtpDivider: View by bindView(R.id.etp_and_free_cancellation_divider)


    private val miniMapView: LocationMapImageView by bindView(R.id.mini_map_view)
    private val transparentViewOverMiniMap: View by bindView(R.id.transparent_view_over_mini_map)

    private val roomRateHeader: LinearLayout by bindView(R.id.room_rate_header)
    private val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    private val roomRateRegularLoyaltyAppliedView: LinearLayout by bindView(R.id.room_rate_regular_loyalty_applied_container)
    private val roomRateVIPLoyaltyAppliedContainer: View by bindView(R.id.room_rate_vip_loyalty_applied_container)
    private val commonAmenityDivider: View by bindView(R.id.common_amenities_divider)

    @VisibleForTesting val roomContainer: LinearLayout by bindView(R.id.room_container)

    private val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)
    private val renovationContainer: ViewGroup by bindView(R.id.renovation_container)

    private val payNowPayLaterTabs: PayNowPayLaterTabs by bindView(R.id.pay_now_pay_later_tabs)
    private val payByPhoneContainer: ViewGroup by bindView(R.id.book_by_phone_container)
    private val payByPhoneTextView: TextView by bindView(R.id.book_by_phone_text)

    private val space: Space by bindView(R.id.bottom_bar_spacer)

    private var isHotelDescriptionExpanded = false

    private var priceContainerLocation = IntArray(2)
    private var roomContainerPosition = IntArray(2)
    private var urgencyContainerLocation = IntArray(2)

    private val ANIMATION_DURATION_ROOM_CONTAINER = if (ExpediaBookingApp.isAutomation()) 0L else 250L
    private val ANIMATION_DURATION = 200L

    init {
        View.inflate(context, R.layout.hotel_detail_content_view, this)


        val phoneIconDrawable = ContextCompat.getDrawable(context, R.drawable.detail_phone).mutate()
        phoneIconDrawable.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)

        AccessibilityUtil.appendRoleContDesc(etpInfoTextSmall, etpInfoTextSmall.text.toString(), R.string.accessibility_cont_desc_role_button)

        hotelDescriptionContainer.setAccessibilityDelegate(object: AccessibilityDelegate() {
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

        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_dateless_infosite)) {
            searchInfo.setOnClickListener {
                showDialog()
            }
        }

        payNowPayLaterTabs.payNowClickedSubject.subscribe { payNowClicked() }
        payNowPayLaterTabs.payLaterClickedSubject.subscribe { payLaterClicked() }
    }

    var viewModel: BaseHotelDetailViewModel by notNullAndObservable { vm ->
        vm.hotelSoldOut.subscribeVisibility(detailsSoldOut)
        vm.hotelSoldOut.subscribeInverseVisibility(price)
        vm.hotelSoldOut.subscribeInverseVisibility(roomContainer)

        vm.hotelSearchInfoText.subscribeTextColor(searchInfo)

        vm.noAmenityObservable.subscribe {
            amenityContainer.visibility = View.GONE
            amenityEtpDivider.visibility = View.GONE
        }

        vm.amenitiesListObservable.subscribe { amenityList ->
            amenityContainer.visibility = View.VISIBLE
            amenityEtpDivider.visibility = View.VISIBLE
            Amenity.addHotelAmenity(amenityContainer, amenityList)
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
            updateRooms(roomList.first, roomList.second, false)
        }

        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            if (CollectionUtils.isEmpty(etpRoomList.first)) {
                return@subscribe
            }
            updateRooms(etpRoomList.first, etpRoomList.second, true)
        }

        vm.hasVipAccessLoyaltyObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        }

        vm.hasRegularLoyaltyPointsAppliedObservable.filter { it }.subscribe {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        transparentViewOverMiniMap.subscribeOnClick(vm.mapClickedSubject)
        vm.renovationObservable.subscribeVisibility(renovationContainer)

        vm.sectionBodyObservable.subscribe { htmlBodyText -> setHotelDescriptionText(htmlBodyText) }

        vm.strikeThroughPriceObservable.subscribeText(strikeThroughPrice)
        vm.strikeThroughPriceVisibility.subscribeVisibility(strikeThroughPrice)
        vm.priceToShowCustomerObservable.subscribeText(price)
        vm.roomPriceToShowCustomer.subscribeText(price)
        vm.searchInfoObservable.subscribeText(searchInfo)
        vm.perNightVisibility.subscribeVisibility(pricePerDescriptor)
        vm.pricePerDescriptorObservable.subscribeText(pricePerDescriptor)

        vm.hotelPriceContentDesc.subscribeContentDescription(hotelPriceContainer)

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
        vm.earnMessageVisibilityObservable.subscribeVisibility(earnMessage)
        vm.taxFeeDescriptorObservable.subscribeText(taxFeeDescriptor)
        vm.taxFeeDescriptorVisibilityObservable.subscribeVisibility(taxFeeDescriptor)

        vm.promoImageObservable.subscribe { promoImage ->
            promoMessage.setCompoundDrawablesWithIntrinsicBounds(promoImage, 0, 0, 0)
        }

        vm.hotelMessagingContainerVisibility.subscribeVisibility(hotelMessagingContainer)

        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.hasETPObservable.filter { it == true }.subscribe { payNowLaterSelectionChanged(true) }
        vm.etpContainerVisibility.subscribeVisibility(payNowPayLaterTabs)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut ->
            hasETP && hasFreeCancellation && !hotelSoldOut
        }.subscribeVisibility(freeCancellationAndETPMessaging)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hotelSoldOut ->
            !(hasETP && hasFreeCancellation) && !hotelSoldOut
        }.subscribeVisibility(singleMessageContainer)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hasBestPriceGuarantee, hotelSoldOut ->
            (hasETP || hasFreeCancellation || hasBestPriceGuarantee) && !hotelSoldOut
        }.subscribeVisibility(etpAndFreeCancellationMessagingContainer)

        Observable.combineLatest(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, vm.isUserRatingAvailableObservable, vm.hotelSoldOut) { hasETP, hasFreeCancellation, hasBestPriceGuarantee, hasUserReviews, hotelSoldOut ->
            !hasETP && !hasFreeCancellation && hasBestPriceGuarantee && !hasUserReviews && !hotelSoldOut
        }.subscribeVisibility(bestPriceGuarantee)


        vm.ratingContainerBackground.subscribeBackground(ratingContainer)
        vm.isUserRatingAvailableObservable.filter { it }.subscribe { ratingContainer.subscribeOnClick(vm.reviewsClickedSubject) }
        vm.isUserRatingAvailableObservable.filter { !it }.subscribe { ratingContainer.unsubscribeOnClick() }

        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        etpInfoTextSmall.subscribeOnClick(vm.payLaterInfoContainerClickObserver)

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            infoList.forEach { propertyTextContainer.addView(HotelInfoView(context).setText(it.name, it.content)) }
        }

        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)
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
    }

    fun handleScrollWithOffset(scrollOffset: Int, toolbarOffset: Float) {
        miniMapView.translationY = scrollOffset * 0.15f
        transparentViewOverMiniMap.translationY = miniMapView.translationY

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

    fun getPriceContainerYScreenLocation() : Int {
        if (hotelMessagingContainer.visibility == View.VISIBLE) {
            hotelMessagingContainer.getLocationOnScreen(priceContainerLocation)
        } else {
            priceContainer.getLocationOnScreen(priceContainerLocation)
        }
        return priceContainerLocation[1]
    }

    fun isRoomContainerInBounds(bottom: Float, top: Float) : Boolean {
        val offScreenAboveTop = isRoomContainerAbove(top)

        roomContainer.getLocationOnScreen(roomContainerPosition)
        val onScreenAboveBottom = roomContainerPosition[1] < bottom

        return onScreenAboveBottom && !offScreenAboveTop
    }

    fun isRoomContainerAbove(top: Float) : Boolean {
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
                    if (viewModel.shouldGroupAndSortRoom()) {
                        roomView.requestFocus()
                    } else {
                        (roomView as HotelRoomRateView).row.requestFocus()
                    }
                }
            }
        }, 400L)
    }

    private fun showDialog() {
        val dialogFragment = ChangeDatesDialogFragment()
        val fragmentManager = (context as FragmentActivity).supportFragmentManager

        dialogFragment.presetDates(viewModel.checkInDate, viewModel.checkOutDate)
        dialogFragment.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    private fun areRoomsOffScreenAboveETPToolbar(toolbarOffset: Float): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        return roomContainerPosition[1] + roomContainer.height >= toolbarOffset + payNowPayLaterTabs.height
    }

    private fun updateRooms(roomList: List<HotelOffersResponse.HotelRoomResponse>, topValueAddList: List<String>,
                            payLater: Boolean) {
        val fadeRoomsOutAnimation = AlphaAnimation(1f, 0f)
        fadeRoomsOutAnimation.duration = ANIMATION_DURATION_ROOM_CONTAINER
        if (viewModel.shouldGroupAndSortRoom()) {
            fadeRoomsOutAnimation.setAnimationListener(getGroupedRoomAnimationListener(roomList, payLater))
        } else {
            fadeRoomsOutAnimation.setAnimationListener(getRoomAnimationListener(roomList, topValueAddList, payLater))
        }
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
        for ((roomType, roomResponses) in groupedRooms) {
            if (roomResponses.count() >= 0) {
                val cardView = Ui.inflate<HotelRoomCardView>(R.layout.hotel_room_card_view, roomContainer, false)
                var roomCount = if (roomResponses.count() > 1) 0 else -1

                val header = getRoomHeaderView(roomResponses[0], roomCount)
                cardView.addViewToContainer(header)

                for (roomResponse in roomResponses) {
                    val hasETP = viewModel.hasETPObservable.value
                    val hotelId = viewModel.hotelOffersResponse.hotelId
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
        viewModel.hotelRoomDetailViewModelsObservable.onNext(viewModels)
    }

    private fun getRoomHeaderView(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse, roomCount: Int) : HotelRoomHeaderView {
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

            if (detail.viewModel.hotelRoomResponse.rateInfo.chargeableRateInfo?.airAttached ?: false) {
                HotelTracking.trackLinkHotelAirAttachEligible(detail.viewModel.hotelRoomResponse, detail.viewModel.hotelId)
            }
        }

        detail.depositTermsClickedSubject.subscribe {
            viewModel.depositInfoContainerClickObservable.onNext(Pair(viewModel.hotelOffersResponse.hotelCountry, detail.viewModel.hotelRoomResponse))
        }

        return detail
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
                if (!AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelRoomRateExpanded)) {
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
        viewModel.lastExpandedRowIndexObservable.onNext(-1)
        viewModel.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
        roomContainer.startAnimation(fadeInRoomsAnimation)

        //set focus on first room row for accessibility
        (roomContainer.getChildAt(0) as HotelRoomRateView).row.isFocusableInTouchMode = true
    }

    private fun getHotelRoomRowView(roomIndex: Int, roomResponse: HotelOffersResponse.HotelRoomResponse,
                                    uniqueValueAdd: String): HotelRoomRateView {
        val hasETP = viewModel.hasETPObservable.value
        val view = HotelRoomRateView(context)
        view.viewModel = HotelRoomRateViewModel(context, viewModel.hotelOffersResponse.hotelId,
                roomResponse, uniqueValueAdd, roomIndex,
                viewModel.rowExpandingObservable, hasETP, viewModel.getLOB())
        view.animateRoom.subscribe(rowAnimation)
        view.viewModel.depositTermsClickedObservable.subscribe {
            viewModel.depositInfoContainerClickObservable.onNext(Pair(viewModel.hotelOffersResponse.hotelCountry, roomResponse))
        }
        view.viewModel.roomSelectedObservable.subscribe { roomPair ->
            val (index, roomResponse) = roomPair
            viewModel.roomSelectedSubject.onNext(roomResponse)
            viewModel.selectedRoomIndex = index
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
        resizeAnimator.start()
    }

    private fun addViewToRoomContainer(roomView: View) {
        val parent = roomView.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(roomView)
        }
        roomContainer.addView(roomView)
    }

    private fun recycleRoomImageViews() {
        val groupedRoom = viewModel.shouldGroupAndSortRoom()
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

    private fun priceViewAlpha(ratio: Float) {
        pricePerDescriptor.alpha = ratio
        price.alpha = ratio
        searchInfo.alpha = ratio
        strikeThroughPrice.alpha = ratio
        searchInfo.alpha = ratio
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
        if (readMoreView.visibility == View.VISIBLE && !isHotelDescriptionExpanded) {
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

        if (viewModel.hasVipAccessLoyaltyObservable.value) {
            displayRoomRateHeader()
            roomRateVIPLoyaltyAppliedContainer.visibility = View.VISIBLE
        } else if (viewModel.hasRegularLoyaltyPointsAppliedObservable.value) {
            displayRoomRateHeader()
            roomRateRegularLoyaltyAppliedView.visibility = View.VISIBLE
        }

        HotelTracking.trackPayNowContainerClick()
    }

    private fun payLaterClicked()  {
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
}