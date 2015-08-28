package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.cars.CarSearchParamsBuilder
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import kotlin.properties.Delegates


public class HotelConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name_view)
    val checkInOutDateTextView: TextView by bindView(R.id.check_in_out_dates)
    val addressL1TextView: TextView by bindView(R.id.address_line_one)
    val addressL2TextView: TextView by bindView(R.id.address_line_two)
    val itinNumberTextView: TextView by bindView(R.id.itin_text_view)
    val backgroundImageView: OptimizedImageView by bindView(R.id.background_image_view)
    val directionsToHotelBtn: TextView by bindView(R.id.direction_action_textView)
    val addToCalendarBtn: TextView by bindView(R.id.calendar_action_textView)
    val addCarBtn: TextView by bindView(R.id.add_car_textView)
    val addFlightBtn: TextView by bindView(R.id.add_flight_textView)
    val sendToEmailTextView: TextView by bindView(R.id.email_text)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    private var location: Location by Delegates.notNull()
    private var checkInDate: LocalDate by Delegates.notNull()
    private var checkOutDate: LocalDate by Delegates.notNull()
    private var response: HotelCheckoutResponse by Delegates.notNull()

    init {
        View.inflate(getContext(), R.layout.widget_hotel_confirmation, this)

        val res = getContext().getResources()
        dressAction(res, directionsToHotelBtn, R.drawable.car_directions)
        dressAction(res, addToCalendarBtn, R.drawable.add_to_calendar)
        dressAction(res, addCarBtn, R.drawable.hotel_car)
        dressAction(res, addFlightBtn, R.drawable.car_flights)

        directionsToHotelBtn.setOnClickListener { view ->
            getMapDirections()
        }
        addToCalendarBtn.setOnClickListener { view ->
            addToCalendar()
        }
        addCarBtn.setOnClickListener { view ->
            searchForCars()
        }
        addFlightBtn.setOnClickListener { view ->
            searchForFlights()
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        val navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp)!!.mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.setNavigationIcon(navIcon)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                NavUtils.goToItin(getContext())
                Events.post(Events.FinishActivity())
            }
        })
    }

    fun bind(response: HotelCheckoutResponse) {
        this.response = response
        val product = response.checkoutResponse.productResponse
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        checkInDate = dtf.parseLocalDate(product.checkInDate)
        checkOutDate = dtf.parseLocalDate(product.checkOutDate)

        PicassoHelper.Builder(backgroundImageView)
                .setError(R.drawable.cars_fallback)
                .fade()
                .fit()
                .centerCrop()
                .build()
                .load("https://media.expedia.com" + product.bigImageUrl)

        hotelNameTextView.setText(product.localizedHotelName)
        checkInOutDateTextView.setText(DateFormatUtils.formatDateRange(getContext(),checkInDate, checkOutDate, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH))

        addressL1TextView.setText(product.hotelAddress)
        addressL2TextView.setText(getContext().getResources().getString(R.string.stay_summary_TEMPLATE, product.hotelCity, product.hotelStateProvince))
        itinNumberTextView.setText(getContext().getResources().getString(R.string.successful_checkout_TEMPLATE, response.checkoutResponse.bookingResponse.itineraryNumber))
        addCarBtn.setText(getContext().getResources().getString(R.string.rent_a_car_TEMPLATE, product.hotelCity))
        addFlightBtn.setText(getContext().getResources().getString(R.string.flights_to_TEMPLATE, product.hotelCity))
        sendToEmailTextView.setText(response.checkoutResponse.bookingResponse.email)

        location = Location()
        location.setCity(product.hotelCity)
        location.setCountryCode(product.hotelCountry)
        location.setStateCode(product.hotelStateProvince)
        location.addStreetAddressLine(product.hotelAddress)
    }

    private fun dressAction(res: Resources, textView: widget.TextView, drawableResId: Int) {
        val drawable = res.getDrawable(drawableResId)
        drawable.setColorFilter(res.getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        FontCache.setTypeface(textView, FontCache.Font.ROBOTO_REGULAR)
    }

    private fun getMapDirections() {
        val uri = Uri.parse("http://maps.google.com/maps?daddr=" + location.toLongFormattedString())
        val intent = Intent(Intent.ACTION_VIEW, uri)
        getContext().startActivity(intent)
    }

    private fun addToCalendar() {
        // Go in reverse order, so that "check in" is shown to the user first
        getContext().startActivity(generateHotelCalendarIntent(false))
        getContext().startActivity(generateHotelCalendarIntent(true))

        OmnitureTracking.trackHotelConfirmationAddToCalendar()
    }

    private fun generateHotelCalendarIntent(checkIn: Boolean): Intent {
        val property = Property()
        property.setName(response.checkoutResponse.productResponse.localizedHotelName)
        property.setLocation(location)

        val date = if (checkIn)
            checkInDate
        else
            checkOutDate
        return AddToCalendarUtils.generateHotelAddToCalendarIntent(getContext(), property, date, checkIn, null, response.checkoutResponse.bookingResponse.itineraryNumber)
    }

    private fun searchForCars() {
        val builder = CarSearchParamsBuilder()

        val dateTimeBuilder = CarSearchParamsBuilder.DateTimeBuilder().startDate(checkInDate).endDate(checkOutDate)
        builder.origin(location.toShortFormattedString())
        builder.originDescription(location.toShortFormattedString())
        builder.dateTimeBuilder(dateTimeBuilder)
        NavUtils.goToCars(getContext(), null, builder.build(), NavUtils.FLAG_OPEN_SEARCH)
    }

    private fun searchForFlights() {
        val flightSearchParams = Db.getFlightSearch().getSearchParams()
        flightSearchParams.reset()

        val loc = Location()
        loc.setDestinationId(location.toShortFormattedString())
        flightSearchParams.setArrivalLocation(loc)
        flightSearchParams.setDepartureDate(checkInDate)
        flightSearchParams.setReturnDate(checkOutDate)

        // Go to flights
        NavUtils.goToFlights(getContext(), true)
    }
}
