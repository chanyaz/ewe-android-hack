package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.PayLaterInfoWidget
import com.expedia.bookings.widget.SpecialNoticeWidget
import com.expedia.util.endlessObserver


public class HotelDetailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val hotelDetailView: HotelDetailView by bindView(R.id.hotel_detail)
    val hotelRenovationDesc: SpecialNoticeWidget by bindView(R.id.hotel_detail_desc)
    val hotelPayLaterInfo : PayLaterInfoWidget by bindView(R.id.hotel_pay_later_info)

    init {
        View.inflate(context, R.layout.widget_hotel_detail_presenter, this)
    }

    override fun onFinishInflate() {
        addTransition(detailToDescription)
        addTransition(detailToPayLaterInfo)
        addDefaultTransition(default)
        showDefault()
    }

    val default = object : Presenter.DefaultTransition(javaClass<HotelDetailView>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.setVisibility(View.GONE)
            hotelPayLaterInfo.setVisibility(View.GONE)
            hotelDetailView.setVisibility(View.VISIBLE)
        }
    }

    val detailToDescription = object : VisibilityTransition(this, javaClass<HotelDetailView>(), javaClass<SpecialNoticeWidget>()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.setVisibility(if (forward) View.VISIBLE else View.GONE)
            hotelDetailView.setVisibility(if (forward) View.GONE else View.VISIBLE)
        }
    }

    val hotelRenovationObserver = endlessObserver<Pair<String, String>> { text ->
        hotelRenovationDesc.setText(text)
        show(hotelRenovationDesc)
    }

    val detailToPayLaterInfo = object : VisibilityTransition(this, javaClass<HotelDetailView>(), javaClass<PayLaterInfoWidget>()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelPayLaterInfo.setVisibility(if (forward) View.VISIBLE else View.GONE)
            hotelDetailView.setVisibility(if (forward) View.GONE else View.VISIBLE)
        }
    }

    val hotelPayLaterInfoObserver = endlessObserver<String> { hotelCountryCode ->
        hotelPayLaterInfo.setText(hotelCountryCode)
        show(hotelPayLaterInfo)
    }

    fun showDefault() {
        show(hotelDetailView)
    }
}
