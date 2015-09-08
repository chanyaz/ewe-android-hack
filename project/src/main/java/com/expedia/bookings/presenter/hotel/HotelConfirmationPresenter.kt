package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelConfirmationViewModel
import javax.inject.Inject
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

    var hotelConfirmationViewModel: HotelConfirmationViewModel by Delegates.notNull()
        @Inject set

    init {
        View.inflate(getContext(), R.layout.widget_hotel_confirmation, this)

        val res = getContext().getResources()
        dressAction(res, directionsToHotelBtn, R.drawable.car_directions)
        dressAction(res, addToCalendarBtn, R.drawable.add_to_calendar)
        dressAction(res, addCarBtn, R.drawable.hotel_car)
        dressAction(res, addFlightBtn, R.drawable.car_flights)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(getContext()).hotelComponent().inject(this);

        addFlightBtn.subscribeOnClick(hotelConfirmationViewModel.getAddFlightBtnObserver(getContext()))
        addCarBtn.subscribeOnClick(hotelConfirmationViewModel.getAddCarBtnObserver(getContext()))
        addToCalendarBtn.subscribeOnClick(hotelConfirmationViewModel.getAddToCalendarBtnObserver(getContext()))
        directionsToHotelBtn.subscribeOnClick(hotelConfirmationViewModel.getDirectionsToHotelBtnObserver(getContext()))

        hotelConfirmationViewModel.itineraryNumberLabel.subscribe(itinNumberTextView)
        hotelConfirmationViewModel.formattedCheckInOutDate.subscribe(checkInOutDateTextView)
        hotelConfirmationViewModel.bigImageUrl.subscribe { value ->
            PicassoHelper.Builder(backgroundImageView)
                    .setError(com.expedia.bookings.R.drawable.cars_fallback)
                    .fade()
                    .fit()
                    .centerCrop()
                    .build()
                    .load(value)
        }
        hotelConfirmationViewModel.hotelName.subscribe(hotelNameTextView)
        hotelConfirmationViewModel.addressLineOne.subscribe(addressL1TextView)
        hotelConfirmationViewModel.addressLineTwo.subscribe(addressL2TextView)
        hotelConfirmationViewModel.addFlightBtnText.subscribe(addFlightBtn)
        hotelConfirmationViewModel.addCarBtnText.subscribe(addCarBtn)
        hotelConfirmationViewModel.customerEmail.subscribe(sendToEmailTextView)

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

    private fun dressAction(res: Resources, textView: widget.TextView, drawableResId: Int) {
        val drawable = res.getDrawable(drawableResId)
        drawable.setColorFilter(res.getColor(R.color.cars_confirmation_icon_color), PorterDuff.Mode.SRC_IN)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        FontCache.setTypeface(textView, FontCache.Font.ROBOTO_REGULAR)
    }
}
