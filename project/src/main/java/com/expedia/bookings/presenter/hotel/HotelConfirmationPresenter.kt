package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.HotelConfirmationViewModel
import com.squareup.phrase.Phrase

class HotelConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val hotelNameTextView: TextView by bindView(R.id.hotel_name_view)
    val checkInOutDateTextView: TextView by bindView(R.id.check_in_out_dates)
    val addressL1TextView: TextView by bindView(R.id.address_line_one)
    val addressL2TextView: TextView by bindView(R.id.address_line_two)
    val itinNumberTextView: TextView by bindView(R.id.itin_text_view)
    val backgroundImageView: OptimizedImageView by bindView(R.id.background_image_view)
    val directionsToHotelBtn: TextView by bindView(R.id.direction_action_textView)
    val addToCalendarBtn: TextView by bindView(R.id.calendar_action_textView)
    val callSupportBtn: TextView by bindView(R.id.call_support_action_textView)
    val addCarBtn: TextView by bindView(R.id.add_car_textView)
    val addFlightBtn: TextView by bindView(R.id.add_flight_textView)
    val addLXBtn: TextView by bindView(R.id.add_lx_textView)
    val sendToEmailTextView: TextView by bindView(R.id.email_text)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    var hotelConfirmationViewModel: HotelConfirmationViewModel by notNullAndObservable {
        hotelConfirmationViewModel.showCarCrossSell.subscribeVisibility(addCarBtn)
        hotelConfirmationViewModel.showFlightCrossSell.subscribeVisibility(addFlightBtn)
        hotelConfirmationViewModel.itineraryNumberLabel.subscribeText(itinNumberTextView)
        hotelConfirmationViewModel.formattedCheckInOutDate.subscribeText(checkInOutDateTextView)
        hotelConfirmationViewModel.showAddToCalendar.subscribeVisibility(addToCalendarBtn)
        hotelConfirmationViewModel.bigImageUrl.subscribe { value ->
            if (!Strings.isEmpty(value)) {
                PicassoHelper.Builder(backgroundImageView)
                        .setError(R.drawable.room_fallback)
                        .fade()
                        .fit()
                        .centerCrop()
                        .build()
                        .load(value)
            } else {
                backgroundImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.room_fallback))
            }
        }
        hotelConfirmationViewModel.hotelName.subscribeText(hotelNameTextView)
        hotelConfirmationViewModel.addressLineOne.subscribeText(addressL1TextView)
        hotelConfirmationViewModel.addressLineTwo.subscribeText(addressL2TextView)
        hotelConfirmationViewModel.addFlightBtnText.subscribeText(addFlightBtn)
        hotelConfirmationViewModel.addCarBtnText.subscribeText(addCarBtn)
        hotelConfirmationViewModel.addLXBtn.subscribeTextAndVisibility(addLXBtn)
        hotelConfirmationViewModel.customerEmail.subscribeText(sendToEmailTextView)

        addFlightBtn.subscribeOnClick(hotelConfirmationViewModel.getAddFlightBtnObserver(getContext()))
        addCarBtn.subscribeOnClick(hotelConfirmationViewModel.getAddCarBtnObserver(getContext()))
        addLXBtn.subscribeOnClick(hotelConfirmationViewModel.getAddLXBtnObserver(getContext()))
        addToCalendarBtn.subscribeOnClick(hotelConfirmationViewModel.getAddToCalendarBtnObserver(getContext()))
        callSupportBtn.subscribeOnClick(hotelConfirmationViewModel.getCallSupportBtnObserver(getContext()))
        directionsToHotelBtn.subscribeOnClick(hotelConfirmationViewModel.getDirectionsToHotelBtnObserver(getContext()))

    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_confirmation, this)
        dressAction(directionsToHotelBtn, R.drawable.car_directions)
        dressAction(addToCalendarBtn, R.drawable.add_to_calendar)
        dressAction(addCarBtn, R.drawable.hotel_car)
        dressAction(addFlightBtn, R.drawable.car_flights)
        dressAction(addLXBtn, R.drawable.ic_activity_attach)
        dressAction(callSupportBtn, R.drawable.hotel_phone)
        callSupportBtn.text = Phrase.from(context, R.string.call_customer_support_TEMPLATE).put("brand", BuildConfig.brand).format()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val navIcon = ContextCompat.getDrawable(context, R.drawable.ic_close_white_24dp).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationContentDescription(R.string.hotel_confirmation_toolbar_close_cont_desc)
        toolbar.setNavigationOnClickListener({
            NavUtils.goToItin(context)
            Events.post(Events.FinishActivity())
        })
        val paddingTop = Ui.getStatusBarHeight(context) - (5 * resources.displayMetrics.density).toInt()
        toolbar.setPadding(0, paddingTop, 0, 0)
    }

    private fun dressAction(textView: TextView, drawableResId: Int) {
        val drawable = ContextCompat.getDrawable(context, drawableResId)
        drawable.setColorFilter(ContextCompat.getColor(context, R.color.confirmation_screen_action_icon_color), PorterDuff.Mode.SRC_IN)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        FontCache.setTypeface(textView, FontCache.Font.ROBOTO_REGULAR)
    }
}
