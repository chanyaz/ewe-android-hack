package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.HotelMapView
import com.expedia.bookings.widget.PayLaterInfoWidget
import com.expedia.bookings.widget.SpecialNoticeWidget
import com.expedia.bookings.widget.VIPAccessInfoWidget
import com.expedia.bookings.widget.DepositTermsInfoWidget
import com.expedia.util.endlessObserver
import com.google.android.gms.maps.GoogleMap

public class HotelDetailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val hotelDetailView: HotelDetailView by bindView(R.id.hotel_detail)
    val hotelRenovationDesc: SpecialNoticeWidget by bindView(R.id.hotel_detail_desc)
    val hotelPayLaterInfo : PayLaterInfoWidget by bindView(R.id.hotel_pay_later_info)
    val hotelDepositInfo : DepositTermsInfoWidget by bindView(R.id.hotel_deposit_info)
    val hotelVIPAccessInfo : VIPAccessInfoWidget by bindView(R.id.hotel_vip_access_info)
    val hotelMapView: HotelMapView by bindView(R.id.hotel_map_view)
    var searchTop = 0

    val hotelDepositInfoObserver = endlessObserver<Pair<String, HotelOffersResponse.HotelRoomResponse>> { pair ->
        hotelDepositInfo.setText(pair)
        show(hotelDepositInfo)
    }

    init {
        View.inflate(context, R.layout.widget_hotel_detail_presenter, this)
    }

    override fun onFinishInflate() {
        addTransition(detailToDescription)
        addTransition(detailToPayLaterInfo)
        addTransition(detailToDepositInfo)
        addTransition(detailToVIPAccessInfo)
        addTransition(detailToMap)
        addDefaultTransition(default)
        show(hotelDetailView)
    }

    val default = object : Presenter.DefaultTransition(HotelDetailView::class.java.getName()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelRenovationDesc.visibility = View.GONE
            hotelPayLaterInfo.visibility = View.GONE
            hotelVIPAccessInfo.visibility = View.GONE
            hotelMapView.visibility = View.GONE
            hotelDetailView.visibility = View.VISIBLE
        }
    }

    public fun showDefault() {
        show(hotelDetailView)
    }

    private val detailToMap = object: ScaleTransition(this, HotelDetailView::class.java, HotelMapView::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelMapView.viewmodel.resetCameraPosition.onNext(Unit)
            if (forward) {
                hotelMapView.googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            } else {
                hotelMapView.googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
            }
        }
    }

    val detailToDescription = object : ScaleTransition(this, HotelDetailView::class.java, SpecialNoticeWidget::class.java) {
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

    val detailToVIPAccessInfo = object : ScaleTransition(this, HotelDetailView::class.java, VIPAccessInfoWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelVIPAccessInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
            if (!forward) {
                ViewCompat.jumpDrawablesToCurrentState(hotelDetailView.vipAccessMessage)
            }

        }
    }

    val hotelDetailsEmbeddedMapClickObserver = endlessObserver<Unit> {
        show(hotelMapView)
        HotelV2Tracking().trackHotelV2DetailMapView()
    }

    val detailToPayLaterInfo = object : ScaleTransition(this, HotelDetailView::class.java, PayLaterInfoWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelPayLaterInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
            if (!forward) {
                ViewCompat.jumpDrawablesToCurrentState(hotelDetailView.etpInfoText)
            }
        }
    }

    val detailToDepositInfo = object : ScaleTransition(this, HotelDetailView::class.java, DepositTermsInfoWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            hotelDepositInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    val hotelPayLaterInfoObserver = endlessObserver<Pair<String, List<HotelOffersResponse.HotelRoomResponse>>> { pair ->
        hotelPayLaterInfo.setText(pair)
        show(hotelPayLaterInfo)
    }

    val hotelVIPAccessInfoObserver = endlessObserver<Unit> {
        show(hotelVIPAccessInfo)
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
