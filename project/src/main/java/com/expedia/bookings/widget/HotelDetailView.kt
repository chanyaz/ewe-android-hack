package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import android.widget.HorizontalScrollView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.hotels.HotelOffersResponse
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
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: RatingBar by bindView(R.id.hotel_star_rating_bar)

    val gallery: RecyclerGallery by bindView(R.id.images_gallery)
    val galleryContainer: FrameLayout by bindView(R.id.gallery_container)

    val pricePerNight: TextView by bindView(R.id.price_per_night)
    val searchInfo: TextView by bindView(R.id.hotel_search_info)
    val userRating: TextView by bindView(R.id.user_rating)
    val numberOfReviews: TextView by bindView(R.id.number_of_reviews)
    val hotelDescription: TextView by bindView(R.id.body_text)
    val readMoreView: View by bindView(R.id.read_more)
    val fadeOverlay: View by bindView(R.id.body_text_fade_bottom)
    val mapView: MapView by bindView(R.id.map_view)
    val mapClickContainer: FrameLayout by bindView(R.id.map_click_container)

    val amenityScrollView: HorizontalScrollView by bindView(R.id.amenities_scroll_view)
    val amenityRow: TableRow by bindView(R.id.amenities_table_row)
    val noAmenityText: TextView by bindView(R.id.amenities_none_text)

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

    var viewmodel: HotelDetailViewModel by notNullAndObservable { vm ->
        vm.galleryObservable.subscribe { galleryUrls ->
            gallery.setDataSource(galleryUrls)
            gallery.scrollToPosition(0)
            gallery.setOnItemClickListener(vm)
            gallery.startFlipping()
        }

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

        mapClickContainer.subscribeOnClick(vm.mapClickContainer)
        hotelDescription.subscribeOnClick(vm.readMore)
        readMoreView.subscribeOnClick(vm.readMore)

        //getting the map
        mapView.onCreate(null)
        mapView.getMapAsync(this);
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
            var yOffset = detailContainer.getScrollY()
            var ratio: Float = parallaxScrollHeader(yOffset)
            toolBarBackground.setAlpha(ratio)
        }
    }

    public fun parallaxScrollHeader(scrollY: Int): Float {
        val ratio = (scrollY).toFloat() / (mainContainer.getTop() - offset)
        galleryContainer.setTranslationY(scrollY * 0.5f)
        return ratio
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
        toolBarBackground.setAlpha(0f)
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        detailContainer.getViewTreeObserver().addOnScrollChangedListener(scrollListener)
        offset = Ui.toolbarSizeWithStatusBar(getContext()).toFloat()
    }

}