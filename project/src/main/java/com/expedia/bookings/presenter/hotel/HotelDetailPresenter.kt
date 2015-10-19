package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.ArrowXDrawableUtil
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
        show(hotelDetailView)
    }

    val default = object : Presenter.DefaultTransition(HotelDetailView::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.visibility = View.GONE
            hotelPayLaterInfo.visibility = View.GONE
            hotelDetailView.visibility = View.VISIBLE
        }
    }

    public fun showDefault() {
        show(hotelDetailView)
    }

    val detailToDescription = object : VisibilityTransition(this, HotelDetailView::class.java, SpecialNoticeWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    val hotelRenovationObserver = endlessObserver<Pair<String, String>> { text ->
        hotelRenovationDesc.setText(text)
        show(hotelRenovationDesc)
    }

    val detailToPayLaterInfo = object : VisibilityTransition(this, HotelDetailView::class.java, PayLaterInfoWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelPayLaterInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
            if (!forward) {
                ViewCompat.jumpDrawablesToCurrentState(hotelDetailView.etpInfoText)
            }

        }
    }

    val hotelPayLaterInfoObserver = endlessObserver<String> { hotelCountryCode ->
        hotelPayLaterInfo.setText(hotelCountryCode)
        show(hotelPayLaterInfo)
    }

    public fun animationStart(): Float {
        searchTop = hotelDetailView.toolbarTitle.top
        hotelDetailView.toolbar.visibility = View.VISIBLE
        hotelDetailView.toolbarTitle.translationY = searchTop.toFloat()
        hotelDetailView.toolBarRating.translationY = searchTop.toFloat()
        return hotelDetailView.toolbar.alpha
    }

    public fun animationUpdate(f: Float, forward: Boolean) {
        val yTrans = if (forward) -(searchTop * -f) else (searchTop * (1 - f))
        hotelDetailView.toolbarTitle.translationY = yTrans
        hotelDetailView.toolBarRating.translationY = yTrans
    }

    public fun animationFinalize() {
        hotelDetailView.toolbar.visibility = View.VISIBLE
        hotelDetailView.toolbar.visibility = View.VISIBLE
        hotelDetailView.toolbarTitle.translationY = 0f
        hotelDetailView.toolBarRating.translationY = 0f
    }

    override fun back(): Boolean {
        if (hotelDetailView.navIcon.getParameter().toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
            hotelDetailView.updateGallery(false)
            return true
        }
        return super.back()
    }
}
