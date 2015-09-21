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
    var searchTop = 0

    init {
        View.inflate(context, R.layout.widget_hotel_detail_presenter, this)
    }

    override fun onFinishInflate() {
        addTransition(detailToDescription)
        addTransition(detailToPayLaterInfo)
        addDefaultTransition(default)
        showDefault()
    }

    val default = object : Presenter.DefaultTransition(HotelDetailView::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.setVisibility(View.GONE)
            hotelPayLaterInfo.setVisibility(View.GONE)
            hotelDetailView.setVisibility(View.VISIBLE)
        }
    }

    val detailToDescription = object : VisibilityTransition(this, HotelDetailView::class.java, SpecialNoticeWidget::class.java) {
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

    val detailToPayLaterInfo = object : VisibilityTransition(this, HotelDetailView::class.java, PayLaterInfoWidget::class.java) {
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

    public fun animationStart(): Float {
        searchTop = hotelDetailView.toolbarTitle.getTop()
        hotelDetailView.toolbar.setVisibility(View.VISIBLE)
        hotelDetailView.toolbarTitle.setTranslationY(searchTop.toFloat())
        hotelDetailView.toolBarRating.setTranslationY(searchTop.toFloat())
        return hotelDetailView.toolbar.getAlpha()
    }

    public fun animationUpdate(f: Float, forward: Boolean) {
        val yTrans = if (forward) -(searchTop * -f) else (searchTop * (1 - f))
        hotelDetailView.toolbarTitle.setTranslationY(yTrans)
        hotelDetailView.toolBarRating.setTranslationY(yTrans)
    }

    public fun animationFinalize() {
        hotelDetailView.toolbar.setVisibility(View.VISIBLE)
        hotelDetailView.toolbar.setVisibility(View.VISIBLE)
        hotelDetailView.toolbarTitle.setTranslationY(0f)
        hotelDetailView.toolBarRating.setTranslationY(0f)
    }

}
