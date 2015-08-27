package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TableLayout
import android.widget.TableRow
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Amenity
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import rx.Observer
import kotlin.properties.Delegates

/**
 * Created by mohsharma on 8/6/15.
 */
object RoomSelected {
    var observer: Observer<HotelOffersResponse.HotelRoomResponse> by Delegates.notNull()
}
public class HotelDetailView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), OnMapReadyCallback {

    val MAP_ZOOM_LEVEL = 12f
    val screenSize by Delegates.lazy { Ui.getScreenSize(context) }

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: RatingBar by bindView(R.id.hotel_star_rating_bar)

    val gallery: RecyclerGallery by bindView(R.id.images_gallery)
    val galleryContainer: FrameLayout by bindView(R.id.gallery_container)

    val priceContainer : ViewGroup by bindView(R.id.price_widget)
    val pricePerNight: TextView by bindView(R.id.price_per_night)
    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val ratingContainer: LinearLayout by bindView(R.id.rating_container)
    val selectRoomButton: Button by bindView(R.id.select_room_button)
    val userRating: TextView by bindView(R.id.user_rating)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val readMoreView: View by bindView(R.id.read_more)
    val fadeOverlay: View by bindView(R.id.body_text_fade_bottom)
    val mapView: MapView by bindView(R.id.map_view)
    val mapClickContainer: FrameLayout by bindView(R.id.map_click_container)

    val amenityContainer: TableRow by bindView(R.id.amenities_table_row)
    val amenityTitleText: TextView by bindView(R.id.amenities_none_text)

    val resortFeeWidget : ResortFeeWidget by bindView(R.id.resort_fee_widget)

    val commonAmenityContainer : ViewGroup by bindView(R.id.common_amenities_container)
    val commonAmenityText : TextView by bindView(R.id.common_amenities_text)

    val roomContainer: TableLayout by bindView(R.id.room_container)
    val amenities_text: TextView by bindView(R.id.amenities_text)
    val amenities_text_header: TextView by bindView(R.id.amenities_text_header)

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
            commonAmenityContainer.setVisibility(View.VISIBLE)
            commonAmenityText.setText(text)
        }

        vm.hotelResortFeeObservable.subscribe(resortFeeWidget.resortFeeText)

        vm.amenityHeaderTextObservable.subscribe(amenities_text_header)
        vm.amenityTextObservable.subscribe(amenities_text)
        vm.sectionBodyObservable.subscribe(hotelDescription)
        vm.hotelNameObservable.subscribe(toolbarTitle)
        vm.hotelRatingObservable.subscribe(toolBarRating)
        vm.pricePerNightObservable.subscribe(pricePerNight)
        vm.searchInfoObservable.subscribe(searchInfo)
        vm.userRatingObservable.subscribe(userRating)
        vm.numberOfReviewsObservable.subscribe(numberOfReviews)
        vm.hotelLatLngObservable.subscribe { values -> hotelLatLng = values }
        vm.showReadMoreObservable.subscribe { isVisible ->
            fadeOverlay.setVisibility(if (isVisible) View.VISIBLE else View.GONE)
            readMoreView.setVisibility(if (isVisible) View.VISIBLE else View.GONE)
        }

        vm.roomResponseListObservable.subscribe { roomList ->
            roomContainer.removeAllViews()
            roomList.forEachIndexed { roomResponseIndex, room ->
                val view = HotelRoomRateView(getContext(), roomContainer, RoomSelected.observer)
                view.viewmodel = HotelRoomRateViewModel(getContext(), roomList.get(roomResponseIndex), roomResponseIndex)
                roomContainer.addView(view)
            }

        }
        ratingContainer.subscribeOnClick(vm.reviewsClickObserver)
        mapClickContainer.subscribeOnClick(vm.mapClickContainer)
        hotelDescription.subscribeOnClick(vm.readMore)
        readMoreView.subscribeOnClick(vm.readMore)

        //getting the map
        mapView.onCreate(null)
        mapView.getMapAsync(this);
    }

    fun resetView() {
        detailContainer.scrollTo(0, 0)
        toolBarBackground.setAlpha(0f)
        priceViewAlpha(1f)
        resortFeeWidget.setVisibility(View.GONE)
        commonAmenityContainer.setVisibility(View.GONE)
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
            priceContainer.getLocationOnScreen(priceContainerLocation)
            if (priceContainerLocation[1] <= 0) {
                toolBarBackground.setAlpha(1.0f)
            }
            else {
                toolBarBackground.setAlpha(0f)
            }

            var ratio = (priceContainerLocation[1]) / offset
            priceViewAlpha(ratio * 1.5f)

            if (shouldShowResortView()) {
                resortFeeWidget.setVisibility(View.VISIBLE)
            }
            else {
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
        offset = Ui.toolbarSizeWithStatusBar(getContext()).toFloat() + Ui.getToolbarSize(getContext())
    }

}