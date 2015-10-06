package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.graphics.drawable.DrawableCompat
import android.graphics.PointF
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TableLayout
import android.widget.TableRow
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import android.graphics.Rect
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeOnCheckedChange
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeVisibility
import com.expedia.util.unsubscribeOnCheckedChange
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.util.subscribeRating
import com.expedia.util.subscribeInverseVisibility
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
import java.util.ArrayList
import rx.Observable
import rx.Observer
import kotlin.properties.Delegates

object RoomSelected {
    var observer: Observer<HotelOffersResponse.HotelRoomResponse> by Delegates.notNull()
}

//scroll animation duration for select room button
val ANIMATION_DURATION = 500L
val DESCRIPTION_ANIMATION = 150L
val HOTEL_DESC_COLLAPSE_LINES = 2

public class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {

    val MAP_ZOOM_LEVEL = 12f
    var bottomMargin = 0
    var resortViewHeight = 0
    val screenSize by lazy { Ui.getScreenSize(context) }

    var initialScrollTop = 0
    var galleryScroll: GalleryScrollView.SegmentedLinearInterpolator? = null
    var galleryHeight = 0
    var hasBeenTouched = false

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: RatingBar by bindView(R.id.hotel_star_rating_bar)
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
    val stickySelectRoomContainer : ViewGroup by bindView(R.id.sticky_select_room_container)
    val stickySelectRoomButton: Button by bindView(R.id.sticky_select_room)
    val stickySelectRoomShadow: View by bindView(R.id.sticky_select_room_shadow)
    val userRating: TextView by bindView(R.id.user_rating)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val readMoreView : ImageButton by bindView(R.id.read_more)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val hotelDescriptionContainer : ViewGroup by bindView(R.id.hotel_description_container)
    val mapView: MapView by bindView(R.id.map_view)
    val mapClickContainer: FrameLayout by bindView(R.id.map_click_container)
    val gradientHeight = context.getResources().getDimension(R.dimen.hotel_detail_gradient_height)

    val hotelMessagingContainer: LinearLayout by bindView(R.id.promo_messaging_container)
    val discountPercentage: TextView by bindView(R.id.discount_percentage)
    val vipAccessMessage: TextView by bindView(R.id.vip_access_message)
    val promoMessage: TextView by bindView(R.id.promo_text)

    val etpRadioGroup: SlidingRadioGroup by bindView(R.id.radius_pay_options)
    val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)
    val etpInfoText: TextView by bindView(R.id.etp_info_text)
    val freeCancellation: TextView by bindView(R.id.free_cancellation)
    val horizontalDividerBwEtpAndFreeCancellation: View by bindView(R.id.horizontal_divider_bw_etp_and_free_cancellation)
    val etpContainer: HotelEtpStickyHeaderLayout by bindView(R.id.etp_placeholder)
    val renovationContainer : ViewGroup by bindView(R.id.renovation_container)
    val payByPhoneTextView: TextView by bindView(R.id.book_by_phone_text)
    val payByPhoneContainer: ViewGroup by bindView(R.id.book_by_phone_container)

    val hotelGalleryDescriptionContainer: LinearLayout by bindView(R.id.hotel_gallery_description_container)
    val hotelGalleryIndicatorContainer: LinearLayout by bindView(R.id.hotel_gallery_indicator_container)
    val hotelGalleryDescription: TextView by bindView(R.id.hotel_gallery_description)

    val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    val amenityDivider : View by bindView(R.id.etp_and_free_cancellation_divider)

    val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)
    val commonAmenityText: TextView by bindView(R.id.common_amenities_text)
    val commonAmenityDivider : View by bindView(R.id.common_amenities_divider)
    var googleMap : GoogleMap? = null
    val roomContainer: TableLayout by bindView(R.id.room_container)
    val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)

    val detailContainer: NewHotelDetailsScrollView by bindView(R.id.detail_container)
    val mainContainer: ViewGroup by bindView(R.id.main_container)
    var statusBarHeight = 0
    var toolBarHeight = 0
    val toolBarBackground: View by bindView(R.id.toolbar_background)
    val toolBarGradient : View by bindView(R.id.hotel_details_gradient)
    var hotelLatLng: DoubleArray by Delegates.notNull()
    var offset: Float by Delegates.notNull()
    var priceContainerLocation = IntArray(2)
    var urgencyContainerLocation = IntArray(2)
    var roomContainerPosition = IntArray(2)
    var galleryIndicatorPosition = IntArray(2)

    var viewmodel: HotelDetailViewModel by notNullAndObservable { vm ->
        detailContainer.getViewTreeObserver().addOnScrollChangedListener(scrollListener)
        vm.galleryObservable.subscribe { galleryUrls ->
            detailContainer.scrollTo(0, initialScrollTop)
            gallery.setDataSource(galleryUrls)
            gallery.scrollToPosition(0)

            gallery.setOnItemClickListener(vm)
            gallery.startFlipping()
            gallery.setOnItemChangeListener(vm)

            hotelGalleryIndicatorContainer.removeAllViews()
            val galleryItemCount = gallery.adapter.itemCount
            if (galleryItemCount > 0) {
                val indicatorWidth = screenSize.x / galleryItemCount
                val inflater = LayoutInflater.from(context)
                for (position in 0..galleryItemCount - 1) {
                    val galleryIndicator = inflater.inflate(R.layout.widget_hotel_gallery_indicator, hotelGalleryIndicatorContainer, false)
                    val lp = galleryIndicator.layoutParams
                    lp.width = indicatorWidth
                    galleryIndicator.layoutParams = lp
                    hotelGalleryIndicatorContainer.addView(galleryIndicator)
                }
            }
            detailContainer.postDelayed(runnable { setViewVisibilities() }, 400L)
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

            var bounds = Rect();
            var width = hotelGalleryIndicatorContainer.getChildAt(galleryDescriptionBar.first).width
            val galleryItemCount = gallery.getAdapter().getItemCount()
            val galleryDescriptionPadding = (context.resources.getDimension(R.dimen.hotel_gallery_description_padding)).toInt()

            for (indicatorPosition in 0..galleryItemCount - 1) {
                hotelGalleryIndicatorContainer.getChildAt(indicatorPosition).setVisibility(View.INVISIBLE)
            }
            hotelGalleryIndicatorContainer.getChildAt(galleryDescriptionBar.first).setVisibility(View.VISIBLE)
            hotelGalleryDescription.setText(galleryDescriptionBar.second)
            hotelGalleryIndicatorContainer.getChildAt(galleryDescriptionBar.first).getLocationOnScreen(galleryIndicatorPosition)
            hotelGalleryDescription.paint.getTextBounds(galleryDescriptionBar.second, 0, galleryDescriptionBar.second.length(), bounds)

            if (galleryDescriptionBar.first < galleryItemCount / 2 )
                hotelGalleryDescription.setPadding(galleryIndicatorPosition[0], galleryDescriptionPadding,
                        galleryDescriptionPadding, galleryDescriptionPadding)
            else
                hotelGalleryDescription.setPadding(galleryIndicatorPosition[0] + width - bounds.width(),
                        galleryDescriptionPadding, 0, galleryDescriptionPadding)

        }

        vm.renovationObservable.subscribe { renovationContainer.setVisibility(View.VISIBLE) }
        vm.hotelResortFeeObservable.subscribeText(resortFeeWidget.resortFeeText)

        vm.sectionBodyObservable.subscribeText(hotelDescription)
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
        vm.numberOfReviewsObservable.subscribe{ text ->
            numberOfReviews.text = text
            ratingContainer.visibility = View.VISIBLE
        }
        vm.ratingContainerObservable.subscribe {
            ratingContainer.visibility = View.GONE
        }
        vm.hotelLatLngObservable.subscribe {
            values -> hotelLatLng = values
            googleMap?.clear()
            addMarker()
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(hotelLatLng[0], hotelLatLng[1]), MAP_ZOOM_LEVEL))
        }
        vm.showBookByPhoneObservable.subscribe { showPayByPhone ->
            if (showPayByPhone) {
                payByPhoneContainer.visibility = View.VISIBLE
                marginForSelectRoom(payByPhoneContainer)
            } else {
                payByPhoneContainer.visibility = View.GONE
                marginForSelectRoom(propertyTextContainer)
            }
        }
        vm.discountPercentageObservable.subscribeText(discountPercentage)
        vm.hasDiscountPercentageObservable.subscribeVisibility(discountPercentage)
        vm.hasVipAccessObservable.subscribeVisibility(vipAccessMessage)
        vm.promoMessageObservable.subscribeText(promoMessage)
        Observable.zip(vm.hasDiscountPercentageObservable, vm.hasVipAccessObservable, vm.promoMessageObservable,
                {
                    hasDiscount, hasVipAccess, promoMessage -> hasDiscount || hasVipAccess || Strings.isNotEmpty(promoMessage)
                }).subscribeVisibility(hotelMessagingContainer)

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(roomList.first.size())

            roomContainer.removeAllViews()
            roomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), detailContainer, etpContainer, RoomSelected.observer)
                view.viewmodel = HotelRoomRateViewModel(getContext(), roomList.first.get(roomResponseIndex), roomList.second.get(roomResponseIndex), roomResponseIndex, vm)
                roomContainer.addView(view)
                hotelRoomRateViewModels.add(view.viewmodel)
            }
            vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            vm.lastExpandedRowObservable.onNext(0)

        }

        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, { hasETP, hasFreeCancellation -> hasETP && hasFreeCancellation })
                .subscribe { showETPAndFreeCancellation ->
                    if (showETPAndFreeCancellation) {
                        horizontalDividerBwEtpAndFreeCancellation.visibility = View.VISIBLE
                        etpInfoText.setTextAppearance(context, R.style.ETPInfoTextSmall)
                        freeCancellation.setTextAppearance(context, R.style.HotelDetailsInfoSmall)
                    }else{
                        horizontalDividerBwEtpAndFreeCancellation.visibility = View.GONE
                    }
                }
        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, { hasETP, hasFreeCancellation -> hasETP || hasFreeCancellation })
                .subscribeVisibility(etpAndFreeCancellationMessagingContainer)
        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.hasETPObservable.subscribe { visible ->
            if (visible) {
                etpRadioGroup.subscribeOnCheckedChange(etpContainerObserver)
            }
            etpContainer.visibility = if (visible) View.VISIBLE else View.GONE
        }


        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            val hotelRoomRateViewModels = ArrayList<HotelRoomRateViewModel>(etpRoomList.first.size())

            roomContainer.removeAllViews()
            etpRoomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), detailContainer, etpContainer, RoomSelected.observer)
                view.viewmodel = HotelRoomRateViewModel(getContext(), etpRoomList.first.get(roomResponseIndex).payLaterOffer, etpRoomList.second.get(roomResponseIndex), roomResponseIndex, vm)
                view.viewmodel.payLaterObserver.onNext(Unit)
                roomContainer.addView(view)
                hotelRoomRateViewModels.add(view.viewmodel)
            }
            vm.hotelRoomRateViewModelsObservable.onNext(hotelRoomRateViewModels)
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            vm.lastExpandedRowObservable.onNext(0)

        }

        ratingContainer.subscribeOnClick(vm.reviewsClickedSubject)
        mapClickContainer.subscribeOnClick(vm.mapClickedSubject)
        etpInfoText.subscribeOnClick(vm.payLaterInfoContainerClickObserver)
        galleryContainer.subscribeOnClick(vm.galleryClickedSubject)

        vm.startMapWithIntentObservable.subscribe { intent -> getContext().startActivity(intent) }

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

        vm.sectionImageObservable.subscribe{isExpanded ->
            if (isExpanded) AnimUtils.rotate(readMoreView) else AnimUtils.reverseRotate(readMoreView)
        }
        vm.galleryClickedSubject.subscribe { detailContainer.animateScrollY(detailContainer.getScrollY(), -initialScrollTop,500) }
        hotelDescriptionContainer.subscribeOnClick(vm.hotelDescriptionContainerObserver)
        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        resortFeeWidget.subscribeOnClick(vm.resortFeeContainerClickObserver)
        payByPhoneContainer.subscribeOnClick(vm.bookByPhoneContainerClickObserver)

        //getting the map
        mapView.onCreate(null)
        mapView.getMapAsync(this);
    }

    fun resetViews() {
        AnimUtils.reverseRotate(readMoreView)
        hotelDescription.maxLines = HOTEL_DESC_COLLAPSE_LINES
        etpRadioGroup.unsubscribeOnCheckedChange()
        renovationContainer.setVisibility(View.GONE)
        etpRadioGroup.check(R.id.radius_pay_now)
        etpContainer.setVisibility(View.GONE)
        etpAndFreeCancellationMessagingContainer.setVisibility(View.GONE)
        etpInfoText.setTextAppearance(context, R.style.ETPInfoText)
        freeCancellation.setTextAppearance(context, R.style.HotelDetailsInfo)
        toolBarBackground.setAlpha(0f)
        toolBarGradient.setTranslationY(0f)
        priceViewAlpha(1f)
        urgencyViewAlpha(1f)
        hotelGalleryDescriptionContainer.setAlpha(0f)
        resortFeeWidget.setVisibility(View.GONE)
        commonAmenityText.setVisibility(View.GONE)
        commonAmenityDivider.setVisibility(View.GONE)
        hideResortandSelectRoom()
    }

    private fun hideResortandSelectRoom() {
        stickySelectRoomContainer.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bottomMargin = stickySelectRoomContainer.measuredHeight
        resortFeeWidget.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        resortViewHeight = resortFeeWidget.measuredHeight

        resortFeeWidget.animate().translationY(resortViewHeight.toFloat()).setInterpolator(LinearInterpolator()).setDuration(200).start()
        stickySelectRoomContainer.animate().translationY(bottomMargin.toFloat()).setInterpolator(DecelerateInterpolator()).start()
    }

    fun marginForSelectRoom(viewGroup: ViewGroup) {
        val layoutParams = viewGroup.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = bottomMargin
        viewGroup.layoutParams = layoutParams
    }

    val etpContainerObserver: Observer<Int> = endlessObserver { checkedId ->
        if (checkedId == R.id.radius_pay_now) {
            //pay now show all the offers
            viewmodel.roomResponseListObservable.onNext(Pair(viewmodel.hotelOffersResponse.hotelRoomResponse, viewmodel.uniqueValueAddForRooms))
        } else {
            //pay later show only etp offers
            viewmodel.etpRoomResponseListObservable.onNext(Pair(viewmodel.etpOffersList, viewmodel.etpUniqueValueAddForRooms))
        }
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

    private fun setViewVisibilities() {
        var yoffset = detailContainer.scrollY
        doCounterscroll()
        mapView.translationY = yoffset * 0.15f
        priceContainer.getLocationOnScreen(priceContainerLocation)
        hotelMessagingContainer.getLocationOnScreen(urgencyContainerLocation)

        if (priceContainerLocation[1] + priceContainer.height <= offset) {
            toolBarBackground.alpha = 1.0f
            toolbarShadow.visibility = View.VISIBLE
        } else {
            toolBarBackground.alpha = 0f
            toolbarShadow.visibility = View.GONE
        }

        if (priceContainerLocation[1] < gradientHeight) {
            toolBarGradient.translationY = (-(gradientHeight - priceContainerLocation[1]))
        }

        var ratio = (priceContainerLocation[1] - (offset / 2)) / offset
        priceViewAlpha(ratio * 1.5f)

        var urgencyRatio = (urgencyContainerLocation[1] - (offset / 2)) / offset
        urgencyViewAlpha(urgencyRatio * 1.5f)

        if (shouldShowResortView()) {
            resortFeeWidget.animate().translationY(0f).setInterpolator(LinearInterpolator()).setDuration(200).start()
        } else {
            resortFeeWidget.animate().translationY((resortViewHeight).toFloat()).setInterpolator(LinearInterpolator()).setDuration(200).start()
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

    fun urgencyViewAlpha(ratio : Float) {
        discountPercentage.alpha = ratio
        vipAccessMessage.alpha = ratio
        promoMessage.alpha = ratio
    }

    public fun shouldShowResortView(): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        if (roomContainerPosition[1] + roomContainer.getHeight() < offset) return false
        if ((viewmodel.hotelResortFeeObservable.getValue() != null) && roomContainerPosition[1] < screenSize.y / 2) return true
        else return false
    }

    public fun shouldShowStickySelectRoomView() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        var selectRoomButtonOffset = offset

        if (etpContainer.visibility == View.VISIBLE) {
            selectRoomButtonOffset = (offset + (etpContainer.height) / 2)
        }

        if (roomContainerPosition[1] + roomContainer.height < selectRoomButtonOffset ) {
            stickySelectRoomContainer.animate().translationY(0f).setInterpolator(LinearInterpolator()).setDuration(200).start()
        } else {
            stickySelectRoomContainer.animate().translationY((stickySelectRoomContainer.height).toFloat()).setInterpolator(LinearInterpolator()).setDuration(200).start()
        }
    }

    public fun shouldShowETPContainer() {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        if (roomContainerPosition[1] + roomContainer.height < offset + etpContainer.height) {
            etpContainer.setEnabled(false)
        } else
            etpContainer.setEnabled(true)
    }

    public fun scrollToRoom() {
        roomContainer.getLocationOnScreen(roomContainerPosition)

        var scrollToAmount = roomContainerPosition[1] - offset + detailContainer.getScrollY()
        if (etpContainer.getVisibility() == View.VISIBLE) scrollToAmount -= etpContainer.getHeight()
        if (commonAmenityText.getVisibility() == View.VISIBLE) scrollToAmount -= (commonAmenityText.getHeight() + getResources().getDimension(R.dimen.hotel_detail_divider_margin))
        val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.getScrollY(), scrollToAmount.toInt())
        smoothScrollAnimation.setDuration(ANIMATION_DURATION)
        smoothScrollAnimation.setInterpolator(DecelerateInterpolator())
        smoothScrollAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val scrollTo = animation.getAnimatedValue() as Int
                detailContainer.scrollTo(0, scrollTo)
            }
        })

        smoothScrollAnimation.start()
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
        DrawableCompat.setTint(toolBarRating.getProgressDrawable(), getResources().getColor(R.color.hotelsv2_detail_star_color))
        offset = statusBarHeight.toFloat() + toolBarHeight

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN!!)
        toolbar.setNavigationIcon(navIcon)
        toolbar.inflateMenu(R.menu.menu_hotel_details)

        toolbar.setNavigationOnClickListener { view ->
            if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
                toggleFullScreenGallery()
            } else
                (getContext() as Activity).onBackPressed()
        }

        offset = statusBarHeight.toFloat() + toolBarHeight

        //share hotel listing text view set up drawable
        val phoneIconDrawable = getResources().getDrawable(R.drawable.detail_phone).mutate()
        phoneIconDrawable.setColorFilter(getResources().getColor(R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        payByPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(phoneIconDrawable, null, null, null)
        selectRoomButton.setOnClickListener { scrollToRoom() }
        stickySelectRoomButton.setOnClickListener { scrollToRoom() }
        strikeThroughPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
        hideResortandSelectRoom()
    }

    public fun doCounterscroll() {
        val t = detailContainer.getScrollY()
        galleryCounterscroll(t)
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (this.visibility == View.VISIBLE && visibility == View.VISIBLE) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    galleryScroll = null
                    val lp = galleryContainer.layoutParams
                    lp.height = height
                    galleryContainer.layoutParams = lp

                    galleryHeight = resources.getDimensionPixelSize(R.dimen.gallery_height)
                    initialScrollTop = height - (resources.getDimensionPixelSize(R.dimen.gallery_height))

                    detailContainer.scrollTo(0, initialScrollTop)
                    doCounterscroll()
                }
            })

        }
    }

    private fun galleryCounterscroll(parentScroll: Int) {
        // Setup interpolator for Gallery counterscroll (if needed)
        if (galleryScroll == null) {
            val screenHeight = getHeight()
            val p1 = PointF(0f, (screenHeight - galleryHeight / 2).toFloat())
            val p2 = PointF(galleryHeight.toFloat(), (screenHeight - galleryHeight).toFloat())
            val p3 = PointF(screenHeight.toFloat(), ((screenHeight - galleryHeight ) / 2).toFloat())
            galleryScroll = GalleryScrollView.SegmentedLinearInterpolator(p1, p2, p3)
        }

        // The number of y-pixels available to the gallery
        val availableHeight = getHeight() - parentScroll

        val counterscroll = galleryScroll?.get(availableHeight.toFloat())?.toInt()

        galleryContainer.setPivotX((getWidth() / 2).toFloat())
        galleryContainer.setPivotY(counterscroll!!.toFloat())

        galleryContainer.scrollTo(0, -counterscroll!!)
    }

    public fun toggleFullScreenGallery() {
        val from = scrollY
        val to = if (from == 0) initialScrollTop else 0
        detailContainer.animateScrollY(from, to, 500)
    }

    public fun getArrowRotationRatio(scrollY: Int): Float {
        return scrollY.toFloat() / (initialScrollTop)
    }

}

