package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.HotelRoomRateView
import com.expedia.bookings.widget.ScrollView
import com.expedia.bookings.widget.TextView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelRoomRateViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelDetailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    var hotelServices: HotelServices? = null
        @Inject set


    var downloadSubscription: Subscription? = null
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    var hotel: Hotel by Delegates.notNull()
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val INTRO_PARAGRAPH_CUTOFF = 120
    val MAP_ZOOM_LEVEL = 12f

    var isSectionExpanded = false

    val toolbarTitle: TextView by bindView(R.id.hotel_name_text)
    val toolBarRating: RatingBar by bindView(R.id.hotel_star_rating_bar)
    val headerImage: ImageView by bindView(R.id.header_image)
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
    var statusBarHeight = 0
    var toolBarHeight = 0
    val toolBarBackground: View by bindView(R.id.toolbar_background)

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(context, R.layout.widget_hotel_detail, this)
    }

    override fun onFinishInflate() {
        // add the view of same height as of status bar
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
        toolbar.setNavigationOnClickListener { view -> back() }
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)

        detailContainer.getViewTreeObserver().addOnScrollChangedListener(scrollListener)
        amenityScrollView.setHorizontalScrollBarEnabled(false)
        amenityScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER)
    }

    private val scrollListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            var yOffset = detailContainer.getScrollY()
            headerImage.setTranslationY(-yOffset * 0.75f)
            var ratio: Float = (yOffset.toFloat() / (headerImage.getHeight()).toFloat()) * 1.51f
            if (detailContainer.getScrollY() == 0) ratio = 0f
            toolBarBackground.setAlpha(ratio)
        }
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    fun getDetail(params: Hotel) {
        hotel = params
        downloadSubscription = hotelServices?.details(hotelSearchParams, params.hotelId, downloadListener)
        toolbarTitle.setText(params.localizedName)
        toolBarRating.setRating(params.hotelStarRating)
        pricePerNight.setText(Phrase.from(getContext().getResources(), R.string.per_nt_TEMPLATE).put("price", params.lowRateInfo.nightlyRateTotal.toString()).format())
        var text = Phrase.from(getContext(), R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(hotelSearchParams.checkIn)).put("enddate", DateUtils.localDateToMMMd(hotelSearchParams.checkOut)).put("guests", hotelSearchParams.getGuestString()).format()
        searchInfo.setText(text)
        userRating.setText(params.hotelGuestRating.toString())
        numberOfReviews.setText(params.totalReviews.toString() + " Reviews")
        mapView.onCreate(null)
        mapView.getMapAsync(this);
    }

    val downloadListener: Observer<HotelOffersResponse> = object : Observer<HotelOffersResponse> {
        override fun onNext(hotelOffersResponse: HotelOffersResponse) {
            bindDetails(hotelOffersResponse);
            Log.d("Hotel Detail Next")
        }

        override fun onCompleted() {
            Log.d("Hotel Detail Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Detail Error")
        }
    }

    private fun bindDetails(hotelOffersResponse: HotelOffersResponse) {
        val url = Images.getHotelImage(hotelOffersResponse, 0)
        PicassoHelper.Builder(headerImage)
                .setError(R.drawable.cars_fallback)
                .fade()
                .build()
                .load(url);

        setHotelDescription(hotelOffersResponse);

        amenityRow.setVisibility(View.GONE)
        noAmenityText.setVisibility(View.VISIBLE)

        // Add rooms and rates info
        roomContainer.removeAllViews()
        var roomResponseList: List<HotelOffersResponse.HotelRoomResponse> = hotelOffersResponse.hotelRoomResponse

        roomResponseList.forEachIndexed { roomResponseIndex, room ->
            val view = HotelRoomRateView(getContext(), roomContainer)
            view.viewmodel = HotelRoomRateViewModel(getContext(), roomResponseList.get(roomResponseIndex), roomResponseIndex)
            roomContainer.addView(view)
        }
        //set amenities description
        amenities_text_header.setText(hotelOffersResponse.hotelAmenitiesText.name)
        amenities_text.setText(Html.fromHtml(hotelOffersResponse.hotelAmenitiesText.content))
    }

    private fun setHotelDescription(hotelOffersResponse: HotelOffersResponse) {
        if (hotelOffersResponse.firstHotelOverview != null) {
            var sectionBody: String = Html.fromHtml(hotelOffersResponse.firstHotelOverview).toString()

            // Add "read more" button if the intro paragraph is too long
            if (sectionBody.length() > INTRO_PARAGRAPH_CUTOFF) {
                val untruncated = sectionBody
                readMoreView.setVisibility(View.VISIBLE)
                fadeOverlay.setVisibility(View.VISIBLE)

                hotelDescription.setOnClickListener { view -> expandSection(untruncated, sectionBody) }
                readMoreView.setOnClickListener { view -> expandSection(untruncated, sectionBody) }

                sectionBody = Phrase.from(getContext(), R.string.hotel_ellipsize_text_template).put("text", sectionBody.substring(0, Strings.cutAtWordBarrier(sectionBody, INTRO_PARAGRAPH_CUTOFF))).format().toString()
            }

            hotelDescription.setText(sectionBody)
        }
    }

    public fun expandSection(untruncated: String, sectionBody: String) {
        if (!isSectionExpanded) {
            hotelDescription.setText(untruncated)
            readMoreView.setVisibility(View.GONE)
            fadeOverlay.setVisibility(View.GONE)
            isSectionExpanded = true
        } else {
            hotelDescription.setText(sectionBody)
            readMoreView.setVisibility(View.VISIBLE)
            fadeOverlay.setVisibility(View.VISIBLE)
            isSectionExpanded = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(getContext())
        addMarker(hotel, googleMap)
        googleMap.getUiSettings().setMapToolbarEnabled(false)
        googleMap.getUiSettings().setMyLocationButtonEnabled(false)
        googleMap.getUiSettings().setZoomControlsEnabled(false)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(hotel.latitude, hotel.longitude), MAP_ZOOM_LEVEL))

        mapClickContainer.setOnClickListener { view ->
            val uri = "geo:" + hotel.latitude + "," + hotel.longitude
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            getContext().startActivity(intent)
        }
    }

    public fun addMarker(hotel: Hotel, googleMap: GoogleMap) {
        val marker = MarkerOptions()
        marker.position(LatLng(hotel.latitude, hotel.longitude))
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cars_pin))
        googleMap.addMarker(marker)
    }

}
