package com.expedia.bookings.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.*
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.expedia.vm.lastExpanded
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiata.android.FormatUtils
import rx.Observable
import rx.Observer
import java.util.*
import kotlin.properties.Delegates

object RoomSelected {
    var observer: Observer<HotelOffersResponse.HotelRoomResponse> by Delegates.notNull()
}

//scroll animation duration for select room button
val ANIMATION_DURATION = 500L

public class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {

    val MAP_ZOOM_LEVEL = 12f
    val screenSize by Delegates.lazy { Ui.getScreenSize(context) }

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: RatingBar by bindView(R.id.hotel_star_rating_bar)

    val gallery: RecyclerGallery by bindView(R.id.images_gallery)
    val galleryContainer: FrameLayout by bindView(R.id.gallery_container)

    val priceContainer: ViewGroup by bindView(R.id.price_widget)
    val pricePerNight: TextView by bindView(R.id.price_per_night)
    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    val selectRoomButton: Button by bindView(R.id.select_room_button)
    val userRating: TextView by bindView(R.id.user_rating)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val mapView: MapView by bindView(R.id.map_view)
    val mapClickContainer: FrameLayout by bindView(R.id.map_click_container)

    val etpRadioGroup: SlidingRadioGroup by bindView(R.id.radius_pay_options)
    val etpAndFreeCancellationMessagingContainer: View by bindView(R.id.etp_and_free_cancellation_messaging_container)
    val etpInfoText: TextView by bindView(R.id.etp_info_text)
    val freeCancellation: TextView by bindView(R.id.free_cancellation)
    val horizontalDividerBwEtpAndFreeCancellation: View by bindView(R.id.horizontal_divider_bw_etp_and_free_cancellation)
    val etpContainer: HotelEtpStickyHeaderLayout by bindView(R.id.etp_placeholder)
    val renovationContainer: ViewGroup by bindView(R.id.renovation_container)
    val shareHotelTextView: TextView by bindView(R.id.share_hotel_text)
    val shareHotelContainer: ViewGroup by bindView(R.id.share_hotel_container)

    val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    val amenityTitleText: TextView by bindView(R.id.amenities_none_text)

    val resortFeeWidget: ResortFeeWidget by bindView(R.id.resort_fee_widget)
    val commonAmenityText: TextView by bindView(R.id.common_amenities_text)

    val roomContainer: TableLayout by bindView(R.id.room_container)
    val propertyTextContainer: TableLayout by bindView(R.id.property_info_container)

    val detailContainer: ScrollView by bindView(R.id.detail_container)
    val mainContainer: ViewGroup by bindView(R.id.main_container)
    var statusBarHeight = 0
    var toolBarHeight = 0
    val toolBarBackground: View by bindView(R.id.toolbar_background)
    var hotelLatLng: DoubleArray by Delegates.notNull()
    var offset: Float by Delegates.notNull()
    var priceContainerLocation = IntArray(2)
    var roomContainerPosition = IntArray(2)
    var viewmodel: HotelDetailViewModel by notNullAndObservable { vm ->

        resetView()
        detailContainer.getViewTreeObserver().addOnScrollChangedListener(scrollListener)
        vm.galleryObservable.subscribe { galleryUrls ->
            gallery.setDataSource(galleryUrls)
            gallery.scrollToPosition(0)
            gallery.setOnItemClickListener(vm)
            gallery.startFlipping()
        }

        vm.amenityTitleTextObservable.subscribe(amenityTitleText)
        vm.amenitiesListObservable.subscribe { amenityList ->
            Amenity.addAmenity(amenityContainer, amenityList)
        }
        vm.commonAmenityTextObservable.subscribe { text ->
            commonAmenityText.setVisibility(View.VISIBLE)
            commonAmenityText.setText(Html.fromHtml(text))
        }

        vm.renovationObservable.subscribe { renovationContainer.setVisibility(View.VISIBLE) }
        vm.hotelResortFeeObservable.subscribe(resortFeeWidget.resortFeeText)

        vm.sectionBodyObservable.subscribe(hotelDescription)
        vm.hotelNameObservable.subscribe(toolbarTitle)
        vm.hotelRatingObservable.subscribe(toolBarRating)
        vm.pricePerNightObservable.subscribe(pricePerNight)
        vm.searchInfoObservable.subscribe(searchInfo)
        vm.userRatingObservable.subscribe(userRating)
        vm.numberOfReviewsObservable.subscribe(numberOfReviews)
        vm.hotelLatLngObservable.subscribe { values -> hotelLatLng = values }

        vm.roomResponseListObservable.subscribe { roomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            roomContainer.removeAllViews()
            roomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), roomContainer, RoomSelected.observer)
                view.viewmodel = HotelRoomRateViewModel(getContext(), roomList.first.get(roomResponseIndex), roomResponseIndex, roomList.second.get(roomResponseIndex))
                roomContainer.addView(view)
            }
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            lastExpanded = 0
        }

        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, {hasETP, hasFreeCancellation -> hasETP && hasFreeCancellation})
                .subscribeVisibility(horizontalDividerBwEtpAndFreeCancellation)
        Observable.zip(vm.hasETPObservable, vm.hasFreeCancellationObservable, {hasETP, hasFreeCancellation -> hasETP || hasFreeCancellation})
                .subscribeVisibility(etpAndFreeCancellationMessagingContainer)
        vm.hasETPObservable.subscribeVisibility(etpInfoText)
        vm.hasFreeCancellationObservable.subscribeVisibility(freeCancellation)

        vm.hasETPObservable.subscribeVisibility(etpContainer)

        vm.etpRoomResponseListObservable.subscribe { etpRoomList: Pair<List<HotelOffersResponse.HotelRoomResponse>, List<String>> ->
            roomContainer.removeAllViews()
            etpRoomList.first.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), roomContainer, RoomSelected.observer)
                view.viewmodel = HotelRoomRateViewModel(getContext(), etpRoomList.first.get(roomResponseIndex), roomResponseIndex, etpRoomList.second.get(roomResponseIndex))
                view.viewmodel.payLaterObserver.onNext(Unit)
                roomContainer.addView(view)
            }
            //setting first room in expanded state as some etp hotel offers are less compared to pay now offers
            lastExpanded = 0
        }
        
        ratingContainer.subscribeOnClick(vm.reviewsClickedSubject)
        mapClickContainer.subscribeOnClick(vm.mapClickedSubject)
        hotelDescription.subscribeOnClick(vm.readMore)
        etpRadioGroup.subscribeOnCheckedChange(etpContainerObserver)
        renovationContainer.subscribeOnClick(vm.renovationContainerClickObserver)
        etpAndFreeCancellationMessagingContainer.subscribeOnClick(vm.payLaterInfoContainerClickObserver)

        shareHotelContainer.subscribeOnClick(vm.shareHotelClickedSubject)

        vm.startMapWithIntentObservable.subscribe { intent -> getContext().startActivity(intent) }

        vm.propertyInfoListObservable.subscribe { infoList ->
            propertyTextContainer.removeAllViews()
            for (info in infoList) {
                val view = HotelInfoView(getContext())
                view.setText(info.name, info.content)
                propertyTextContainer.addView(view)
            }

        }

        //getting the map
        mapView.onCreate(null)
        mapView.getMapAsync(this);
    }

    fun resetView() {
        renovationContainer.setVisibility(View.GONE)
        etpRadioGroup.check(R.id.radius_pay_now)
        etpContainer.setVisibility(View.GONE)
        etpAndFreeCancellationMessagingContainer.setVisibility(View.GONE)
        detailContainer.scrollTo(0, 0)
        toolBarBackground.setAlpha(0f)
        priceViewAlpha(1f)
        resortFeeWidget.setVisibility(View.GONE)
        commonAmenityText.setVisibility(View.GONE)
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
        MapsInitializer.initialize(getContext())
        addMarker(googleMap)
        googleMap.getUiSettings().setMapToolbarEnabled(false)
        googleMap.getUiSettings().setMyLocationButtonEnabled(false)
        googleMap.getUiSettings().setZoomControlsEnabled(false)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(hotelLatLng[0], hotelLatLng[1]), MAP_ZOOM_LEVEL))
    }

    public fun addMarker(googleMap: GoogleMap) {
        val marker = MarkerOptions()
        marker.position(LatLng(hotelLatLng[0], hotelLatLng[1]))
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cars_pin))
        googleMap.addMarker(marker)
    }

    val scrollListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            var yoffset = detailContainer.getScrollY()
            mapView.setTranslationY(yoffset * 0.1f)

            priceContainer.getLocationOnScreen(priceContainerLocation)
            if (priceContainerLocation[1] <= 0) {
                toolBarBackground.setAlpha(1.0f)
            } else {
                toolBarBackground.setAlpha(0f)
            }

            var ratio = (priceContainerLocation[1]) / offset
            priceViewAlpha(ratio * 1.5f)

            if (shouldShowResortView()) {
                resortFeeWidget.setVisibility(View.VISIBLE)
            } else {
                resortFeeWidget.setVisibility(View.GONE)
            }
        }
    }

    fun priceViewAlpha(ratio: Float) {
        pricePerNight.setAlpha(ratio)
        searchInfo.setAlpha(ratio)
        selectRoomButton.setAlpha(ratio)
    }

    public fun shouldShowResortView(): Boolean {
        roomContainer.getLocationOnScreen(roomContainerPosition)
        if (roomContainerPosition[1] + roomContainer.getHeight() < offset) return false
        if ((viewmodel.hotelResortFeeObservable.getValue() != null) && roomContainerPosition[1] < screenSize.y / 2) return true
        else return false
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_detail, this)
        statusBarHeight = Ui.getStatusBarHeight(getContext())
        toolBarHeight = Ui.getToolbarSize(getContext())
        if (statusBarHeight > 0) {
            toolbar.setPadding(0, statusBarHeight, 0, 0)
        }
        Ui.showTransparentStatusBar(getContext())
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp))
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        toolBarBackground.getLayoutParams().height += statusBarHeight
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        offset = statusBarHeight.toFloat() + toolBarHeight
        toolbar.setNavigationOnClickListener { view ->
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        //share hotel listing text view set up drawable
        val shareIconDrawable = getResources().getDrawable(R.drawable.details_share).mutate()
        shareIconDrawable.setColorFilter(getResources().getColor(R.color.hotels_primary_color), PorterDuff.Mode.SRC_IN)
        shareHotelTextView.setCompoundDrawablesWithIntrinsicBounds(shareIconDrawable, null, null, null)
        selectRoomButton.setOnClickListener {
            val smoothScrollAnimation = ValueAnimator.ofInt(detailContainer.getScrollY(), roomContainer.getTop() + statusBarHeight)
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

    }

}
