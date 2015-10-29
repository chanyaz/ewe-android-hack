package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeBackgroundResource
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeRating
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import rx.Observable
import rx.Observer
import java.util.ArrayList
import kotlin.properties.Delegates

val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

public class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {

    val MAP_ZOOM_LEVEL = 12f
    var bottomMargin = 0
    val ANIMATION_DURATION = 200L
    var resortViewHeight = 0
    var selectRoomContainerHeight = 0
    val screenSize by lazy { Ui.getScreenSize(context) }

    var initialScrollTop = 0
    var galleryHeight = 0

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    var toolBarRating: StarRatingBar by Delegates.notNull()
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    var navIcon: ArrowXDrawable

    val gallery: RecyclerGallery by bindView(R.id.images_gallery)

    val galleryContainer: FrameLayout by bindView(R.id.gallery_container)

    val priceContainer: ViewGroup by bindView(R.id.price_widget)
    val strikeThroughPrice: TextView by bindView(R.id.strike_through_price)
    val price: TextView by bindView(R.id.price)
    val perNight: TextView by bindView(R.id.per_night)

    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    val selectRoomButton: Button by bindView(R.id.select_room_button)
    val stickySelectRoomContainer: ViewGroup by bindView(R.id.sticky_select_room_container)
    val stickySelectRoomButton: Button by bindView(R.id.sticky_select_room)
    val userRating: TextView by bindView(R.id.user_rating)
    val noGuestRating: TextView by bindView(R.id.no_guest_rating)
    val userRatingRecommendationText: TextView by bindView(R.id.user_rating_recommendation_text)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val readMoreView: ImageButton by bindView(R.id.read_more)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val hotelDescriptionContainer: ViewGroup by bindView(R.id.hotel_description_container)
    val miniMapView: MapView by bindView(R.id.mini_map_view)
    val transparentViewOverMiniMap: View by bindView(R.id.transparent_view_over_mini_map)
    val gradientHeight = context.getResources().getDimension(R.dimen.hotel_detail_gradient_height)

    val hotelMessagingContainer: RelativeLayout by bindView(R.id.promo_messaging_container)
    val discountPercentage: TextView by bindView(R.id.discount_percentage)
    val vipAccessMessage: TextView by bindView(R.id.vip_access_message)
    val promoMessage: TextView by bindView(R.id.promo_text)

    val payNowButton: Button by bindView(R.id.radius_pay_now)
    val payLaterButton: Button by bindView(R.id.radius_pay_later)
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
    val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    val commonAmenityDivider: View by bindView(R.id.common_amenities_divider)
    var googleMap: GoogleMap? = null
    val roomContainer: TableLayout by bindView(R.id.room_container)
    val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)

    val detailContainer: NewHotelDetailsScrollView by bindView(R.id.detail_container)
    var statusBarHeight = 0
    var toolBarHeight = 0
    val toolBarBackground: View by bindView(R.id.toolbar_background)
    val toolBarGradient: View by bindView(R.id.hotel_details_gradient)
    var hotelLatLng: DoubleArray by Delegates.notNull()
    var offset: Float by Delegates.notNull()
    var priceContainerLocation = IntArray(2)
    var urgencyContainerLocation = IntArray(2)
    var roomContainerPosition = IntArray(2)

    var resortInAnimator: ObjectAnimator by Delegates.notNull()
    var resortOutAnimator: ObjectAnimator by Delegates.notNull()
    var selectRoomInAnimator: ObjectAnimator by Delegates.notNull()
    var selectRoomOutAnimator: ObjectAnimator by Delegates.notNull()

    var hotelDetailsGalleryImageViews = ArrayList<HotelDetailsGalleryImageView>()

    var viewmodel: HotelDetailViewModel by notNullAndObservable { vm ->
        detailContainer.getViewTreeObserver().addOnScrollChangedListener(scrollListener)
        detailContainer.setOnTouchListener(touchListener)
        vm.galleryObservable.subscribe { galleryUrls ->
            gallery.setDataSource(galleryUrls)
            gallery.scrollToPosition(0)
            gallery.setOnItemClickListener(vm)
            gallery.startFlipping()
            gallery.setOnItemChangeListener(vm)

            val galleryItemCount = gallery.adapter.itemCount
            if (galleryItemCount > 0) {
                val indicatorWidth = screenSize.x / galleryItemCount
                val lp = hotelGalleryIndicator.layoutParams
                lp.width = indicatorWidth
                hotelGalleryIndicator.layoutParams = lp
            }
        }

        vm.scrollToRoom.subscribe {
            scrollToRoom(false)
        }

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
            commonAmenityText.setVisibility(View.VISIBLE)
            commonAmenityText.setText(Html.fromHtml(text))
            commonAmenityDivider.setVisibility(View.VISIBLE)
        }

        vm.galleryItemChangeObservable.subscribe { galleryDescriptionBar: Pair<Int, String> ->
            hotelGalleryIndicator.animate().translationX((galleryDescriptionBar.first * hotelGalleryIndicator.width).toFloat()).
                    setInterpolator(LinearInterpolator()).start()
            hotelGalleryDescription.setText(galleryDescriptionBar.second)

        }

        transparentViewOverMiniMap.subscribeOnClick(vm.mapClickedSubject)

        vm.renovationObservable.subscribe { renovationContainer.setVisibility(View.VISIBLE) }
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
        vm.hotelNameObservable.subscribeText(toolbarTitle)
        vm.hotelRatingObservable.subscribeRating(toolBarRating)
        vm.hotelRatingObservableVisibility.subscribeVisibility(toolBarRating)
        vm.strikeThroughPriceObservable.subscribeText(strikeThroughPrice)
        vm.hasDiscountPercentageObservable.subscribeVisibility(strikeThroughPrice)
        vm.pricePerNightObservable.subscribeText(price)
        vm.searchInfoObservable.subscribeText(searchInfo)
        vm.userRatingObservable.subscribeText(userRating)
        vm.roomPriceToShowCustomer.subscribeText(price)
        vm.onlyShowTotalPrice.subscribeInverseVisibility(perNight)
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRating)
        vm.isUserRatingAvailableObservable.subscribeVisibility(userRatingRecommendationText)
        vm.isUserRatingAvailableObservable.map { !it }.subscribeVisibility(noGuestRating)
        vm.numberOfReviewsObservable.subscribeText(numberOfReviews)
        vm.hotelLatLngObservable.subscribe {
            values ->
            hotelLatLng = values
            googleMap?.clear()
            addMarker()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(hotelLatLng[0], hotelLatLng[1]), MAP_ZOOM_LEVEL))
        }
        vm.showBookByPhoneObservable.subscribe { showPayByPhone ->
            if (showPayByPhone) {
                payByPhoneContainer.visibility = View.VISIBLE
            } else {
                payByPhoneContainer.visibility = View.GONE
            }
            spaceAboveSelectARoom();
        }
        vm.discountPercentageObservable.subscribeText(discountPercentage)
        vm.discountPercentageBackgroundObservable.subscribeBackgroundResource(discountPercentage)
        vm.hasDiscountPercentageObservable.subscribeVisibility(discountPercentage)
        vipAccessMessage.subscribeOnClick(vm.vipAccessInfoObservable)
        vm.hasVipAccessObservable.subscribeVisibility(vipAccessMessage)
        vm.promoMessageObservable.subscribeText(promoMessage)
        Observable.zip(vm.hasDiscountPercentageObservable, vm.hasVipAccessObservable, vm.promoMessageObservable,
                {
                    hasDiscount, hasVipAccess, promoMessage ->
                    hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage)
                }).subscribeVisibility(hotelMessagingContainer)

        val rowTopConstraintViewObservable: Observable<View> = vm.hasETPObservable.map { hasETP ->
            when {
                hasETP -> etpContainer
                else -> toolbar
            }
        }

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(roomList.first.size())

            roomContainer.removeAllViews()
            roomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), detailContainer, rowTopConstraintViewObservable, vm.roomSelectedObserver)
                view.viewmodel = HotelRoomRateViewModel(getContext(), roomList.first.get(roomResponseIndex), roomList.second.get(roomResponseIndex), roomResponseIndex, vm)
                roomContainer.addView(view)
                hotelRoomRateViewModels.add(view.viewmodel)
            }
            vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            vm.lastExpandedRowObservable.onNext(0)

        }

        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.hasETPObservable.subscribe { visible ->
            if (visible) {
                payNowLaterSelectionChanged(true)
            }
            etpContainer.visibility = if (visible) View.VISIBLE else View.GONE
        }

        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, { hasETP, hasFreeCancellation -> hasETP && hasFreeCancellation })
                .subscribe { showETPAndFreeCancellation ->
                    if (showETPAndFreeCancellation) {
                        freeCancellationAndETPMessaging.visibility = View.VISIBLE
                        singleMessageContainer.visibility = View.GONE
                    } else {
                        freeCancellationAndETPMessaging.visibility = View.GONE
                        singleMessageContainer.visibility = View.VISIBLE
                    }
                }
        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, { hasETP, hasFreeCancellation, hasBestPriceGuarantee -> hasETP || hasFreeCancellation || hasBestPriceGuarantee })
                .subscribeVisibility(etpAndFreeCancellationMessagingContainer)
        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, vm.hasBestPriceGuaranteeObservable, vm.isUserRatingAvailableObservable, { hasETP, hasFreeCancellation, hasBestPriceGuarantee, hasUserReviews -> !hasETP && !hasFreeCancellation && hasBestPriceGuarantee && !hasUserReviews })
                .subscribe { showBestPriceGuarantee ->
                    if (showBestPriceGuarantee) {
                        bestPriceGuarantee.visibility = View.VISIBLE
                    } else {
                        bestPriceGuarantee.visibility = View.GONE
                    }
                }

        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(etpRoomList.first.size())

            roomContainer.removeAllViews()
            etpRoomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), detailContainer, rowTopConstraintViewObservable, vm.roomSelectedObserver)
                view.viewmodel = HotelRoomRateViewModel(getContext(), etpRoomList.first.get(roomResponseIndex).payLaterOffer, etpRoomList.second.get(roomResponseIndex), roomResponseIndex, vm)
                view.viewmodel.payLaterObserver.onNext(Unit)
                roomContainer.addView(view)
                hotelRoomRateViewModels.add(view.viewmodel)
            }
            vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            vm.lastExpandedRowObservable.onNext(0)
        }

        vm.isUserRatingAvailableObservable.subscribe {
            if (it) {
                ratingContainer.subscribeOnClick(vm.reviewsClickedSubject)
                ratingContainer.background = resources.getDrawable(R.drawable.hotel_detail_ripple)
            } else {
                ratingContainer.unsubscribeOnClick()
                ratingContainer.background = null
            }
        }

        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        etpInfoTextSmall.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        galleryContainer.subscribeOnClick(vm.galleryClickedSubject)

        vm.mapClickedSubject.subscribe {
            HotelV2Tracking().trackLinkHotelV2DetailMapClick()
        }

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            for (info in infoList) {
                val view = HotelInfoView(getContext())
                view.setText(info.name, info.content)
                propertyTextContainer.addView(view)
            }
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
        vm.galleryClickedSubject.subscribe { detailContainer.animateScrollY(detailContainer.getScrollY(), -initialScrollTop, 500) }
        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)

        //getting the map
        miniMapView.onCreate(null)
        miniMapView.getMapAsync(this);
    }

    fun resetViews() {
        AnimUtils.reverseRotate(readMoreView)
        hotelDescription.maxLines = HOTEL_DESC_COLLAPSE_LINES
        renovationContainer.setVisibility(View.GONE)
        etpContainer.setVisibility(View.GONE)
        etpContainerDropShadow.setVisibility(View.GONE)
        etpAndFreeCancellationMessagingContainer.setVisibility(View.GONE)
        toolBarBackground.setAlpha(0f)
        toolBarGradient.setTranslationY(0f)
        priceViewAlpha(1f)
        urgencyViewAlpha(1f)
        hotelGalleryDescriptionContainer.setAlpha(0f)
        resortFeeWidget.setVisibility(View.GONE)
        commonAmenityText.setVisibility(View.GONE)
        commonAmenityDivider.setVisibility(View.GONE)
        hideResortandSelectRoom()
        freeCancellationAndETPMessaging.visibility = View.GONE
        singleMessageContainer.visibility = View.GONE
        viewmodel.onGalleryItemScrolled(0)
    }

    private fun hideResortandSelectRoom() {
        stickySelectRoomContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bottomMargin = (stickySelectRoomContainer.measuredHeight - resources.getDimension(R.dimen.breakdown_text_margin)).toInt()
        resortFeeWidget.animate().translationY(resortViewHeight.toFloat()).setInterpolator(LinearInterpolator()).setDuration(ANIMATION_DURATION).start()
        stickySelectRoomContainer.animate().translationY(selectRoomContainerHeight.toFloat()).setInterpolator(DecelerateInterpolator()).start()
    }

    fun spaceAboveSelectARoom() {
        val params = space.getLayoutParams()
        params.height = bottomMargin
        space.setLayoutParams(params)
    }

    val payNowObserver: Observer<Unit> = endlessObserver {
        //pay now show all the offers
        payNowLaterSelectionChanged(true)
        viewmodel.roomResponseListObservable.onNext(Pair(viewmodel.hotelOffersResponse.hotelRoomResponse, viewmodel.uniqueValueAddForRooms))
        HotelV2Tracking().trackPayNowContainerClick()
    }

    val payLaterObserver: Observer<Unit> = endlessObserver {
        //pay later show only etp offers
        payNowLaterSelectionChanged(false)
        viewmodel.etpRoomResponseListObservable.onNext(Pair(viewmodel.etpOffersList, viewmodel.etpUniqueValueAddForRooms))
        HotelV2Tracking().trackPayLaterContainerClick()
    }

    fun payNowLaterSelectionChanged(payNowSelected: Boolean) {
        payNowButton.setSelected(payNowSelected)
        payLaterButton.setSelected(!payNowSelected)
        if (payNowSelected) {
            payNowButton.unsubscribeOnClick()
            payLaterButton.subscribeOnClick(payLaterObserver)
        } else {
            payLaterButton.unsubscribeOnClick()
            payNowButton.subscribeOnClick(payNowObserver)
        }

        // Scroll to the top room in case of change in ETP selection
        val etpLocation = etpContainer.y + etpContainer.height
        val offsetToETP = if (commonAmenityText.visibility == View.VISIBLE) commonAmenityText.y else roomContainer.y
        val offset = offsetToETP - etpLocation
        detailContainer.smoothScrollBy(0, offset.toInt())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        MapsInitializer.initialize(getContext())
        googleMap.getUiSettings().setMapToolbarEnabled(false)
        googleMap.getUiSettings().setMyLocationButtonEnabled(false)
        googleMap.getUiSettings().setZoomControlsEnabled(false)
    }

    public fun addMarker() {
        googleMap ?: return
        val marker = MarkerOptions()
        marker.position(LatLng(hotelLatLng[0], hotelLatLng[1]))
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.hotels_pin))
        googleMap?.addMarker(marker)
    }

    val scrollListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            setViewVisibilities()
        }
    }

    val touchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            val action = event.action;
            if (action == MotionEvent.ACTION_UP) {
                detailContainer.post { updateGallery(true) }
            }
            return false
        }
    }

    private fun setViewVisibilities() {
        var yoffset = detailContainer.scrollY
        updateGalleryChildrenHeights()
        miniMapView.translationY = yoffset * 0.15f
        transparentViewOverMiniMap.translationY = miniMapView.translationY
        priceContainer.getLocationOnScreen(priceContainerLocation)
        hotelMessagingContainer.getLocationOnScreen(urgencyContainerLocation)

        if (priceContainerLocation[1] + priceContainer.height <= offset) {
            toolBarBackground.alpha = 1.0f
            toolbarShadow.visibility = View.VISIBLE
        } else {
            toolBarBackground.alpha = 0f
            toolbarShadow.visibility = View.GONE
        }

        showToolbarGradient()

        var ratio = (priceContainerLocation[1] - (offset / 2)) / offset
        priceViewAlpha(ratio * 1.5f)

        var urgencyRatio = (urgencyContainerLocation[1] - (offset / 2)) / offset
        urgencyViewAlpha(urgencyRatio * 1.5f)

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
            navIcon.parameter = 1 - arrowRatio
            hotelGalleryDescriptionContainer.alpha = 1 - arrowRatio
        }
    }

    fun priceViewAlpha(ratio: Float) {
        perNight.alpha = ratio
        price.alpha = ratio
        searchInfo.alpha = ratio
        selectRoomButton.alpha = ratio
        strikeThroughPrice.alpha = ratio
    }

    fun urgencyViewAlpha(ratio: Float) {
        discountPercentage.alpha = ratio
        vipAccessMessage.alpha = ratio
        promoMessage.alpha = ratio
    }

    public fun showToolbarGradient() {
        priceContainer.getLocationOnScreen(priceContainerLocation)

        if (priceContainerLocation[1] < gradientHeight) {
            toolBarGradient.translationY = (-(gradientHeight - priceContainerLocation[1]))
        } else
            toolBarGradient.translationY = 0f
    }

    public fun shouldShowResortView(): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        val isOutOfView = roomContainerPosition[1] + roomContainer.height < offset
        val isInView = roomContainerPosition[1] < screenSize.y / 2
        if (viewmodel.hotelResortFeeObservable.value != null && isInView && !isOutOfView) {
            return true
        }
        return false
    }

    public fun shouldShowStickySelectRoomView() {
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

    public fun shouldShowETPContainer() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        if (roomContainerPosition[1] + roomContainer.height < offset + etpContainer.height) {
            etpContainer.setEnabled(false)
        } else
            etpContainer.setEnabled(true)
    }

    public fun scrollToRoom(animate: Boolean) {
        roomContainer.getLocationOnScreen(roomContainerPosition)

        var scrollToAmount = roomContainerPosition[1] - offset + detailContainer.getScrollY()
        if (etpContainer.getVisibility() == View.VISIBLE) scrollToAmount -= etpContainer.getHeight()
        if (commonAmenityText.getVisibility() == View.VISIBLE) scrollToAmount -= (commonAmenityText.getHeight() + getResources().getDimension(R.dimen.hotel_detail_divider_margin))
        val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.getScrollY(), scrollToAmount.toInt())
        smoothScrollAnimation.setDuration(if (animate) ANIMATION_DURATION else 0)
        smoothScrollAnimation.setInterpolator(DecelerateInterpolator())
        smoothScrollAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val scrollTo = animation.getAnimatedValue() as Int
                detailContainer.scrollTo(0, scrollTo)
            }
        })

        smoothScrollAnimation.start()
        HotelV2Tracking().trackLinkHotelV2DetailSelectRoom()
    }


    init {
        View.inflate(getContext(), R.layout.widget_hotel_detail, this)
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        Ui.showTransparentStatusBar(getContext())
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        toolBarBackground.getLayoutParams().height += statusBarHeight
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)

        if (shouldShowCircleForRatings()) {
            toolBarRating = findViewById(R.id.hotel_circle_rating_bar) as StarRatingBar
        } else {
            toolBarRating = findViewById(R.id.hotel_star_rating_bar) as StarRatingBar
        }
        toolBarRating.visibility = View.VISIBLE

        offset = statusBarHeight.toFloat() + toolBarHeight

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN!!)
        toolbar.setNavigationIcon(navIcon)

        toolbar.setNavigationOnClickListener { view ->
            if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
                updateGallery(false)
            } else
                (getContext() as Activity).onBackPressed()
        }

        //share hotel listing text view set up drawable
        val phoneIconDrawable = getResources().getDrawable(R.drawable.detail_phone).mutate()
        phoneIconDrawable.setColorFilter(getResources().getColor(R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)
        selectRoomButton.setOnClickListener { scrollToRoom(true) }
        stickySelectRoomButton.setOnClickListener { scrollToRoom(true) }
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
        resetGallery()

        gallery.addImageViewCreatedListener(object : RecyclerGallery.IImageViewBitmapLoadedListener {
            override fun onImageViewBitmapLoaded(hotelDetailsGalleryImageView: HotelDetailsGalleryImageView) {
                if (!hotelDetailsGalleryImageViews.contains(hotelDetailsGalleryImageView)) {
                    hotelDetailsGalleryImageViews.add(hotelDetailsGalleryImageView)
                }
                hotelDetailsGalleryImageView.setIntermediateValue(height - initialScrollTop, height,
                        detailContainer.scrollY.toFloat() / initialScrollTop)
            }
        })
    }

    public fun resetGallery() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                val lp = galleryContainer.layoutParams
                lp.height = height
                galleryContainer.layoutParams = lp

                galleryHeight = resources.getDimensionPixelSize(R.dimen.gallery_height)
                initialScrollTop = height - (resources.getDimensionPixelSize(R.dimen.gallery_height))

                detailContainer.post {
                    detailContainer.scrollTo(0, initialScrollTop)
                    showToolbarGradient()
                }
            }
        })
    }

    private fun updateGalleryChildrenHeights() {
        for (hotelDetailsGalleryImageView in hotelDetailsGalleryImageViews) {
            hotelDetailsGalleryImageView.setIntermediateValue(height - initialScrollTop, height,
                    detailContainer.scrollY.toFloat() / initialScrollTop)
        }
    }

    public fun updateGallery(toFullScreen: Boolean) {
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

    public fun getArrowRotationRatio(scrollY: Int): Float {
        return scrollY.toFloat() / (initialScrollTop)
    }
}
