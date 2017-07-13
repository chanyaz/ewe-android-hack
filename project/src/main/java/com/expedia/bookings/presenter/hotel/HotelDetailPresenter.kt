package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.DepositTermsInfoWidget
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.bookings.widget.HotelMapView
import com.expedia.bookings.widget.PayLaterInfoWidget
import com.expedia.bookings.widget.SpecialNoticeWidget
import com.expedia.bookings.widget.VIPAccessInfoWidget
import com.expedia.util.endlessObserver
import com.google.android.gms.maps.GoogleMap
import com.expedia.vm.ShopWithPointsViewModel
import javax.inject.Inject

class HotelDetailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val hotelDetailView: HotelDetailView by bindView(R.id.hotel_detail)
    val hotelRenovationDesc: SpecialNoticeWidget by bindView(R.id.hotel_detail_desc)
    val hotelPayLaterInfo : PayLaterInfoWidget by bindView(R.id.hotel_pay_later_info)
    val hotelDepositInfo : DepositTermsInfoWidget by bindView(R.id.hotel_deposit_info)
    val hotelVIPAccessInfo : VIPAccessInfoWidget by bindView(R.id.hotel_vip_access_info)
    val hotelMapView: HotelMapView by bindView(R.id.hotel_map_view)
    var searchTop = 0

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set


    val hotelDepositInfoObserver = endlessObserver<Pair<String, HotelOffersResponse.HotelRoomResponse>> { pair ->
        hotelDepositInfo.setText(pair)
        show(hotelDepositInfo)
    }

    init {
        View.inflate(context, R.layout.widget_hotel_detail_presenter, this)
    }

    override fun onFinishInflate() {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        hotelDetailView.contentView.shopWithPointsViewModel = shopWithPointsViewModel
        addTransition(detailToDescription)
        addTransition(detailToPayLaterInfo)
        addTransition(detailToDepositInfo)
        addTransition(detailToVIPAccessInfo)
        addTransition(detailToMap)
        addDefaultTransition(default)
        show(hotelDetailView)
    }

    val default = object : Presenter.DefaultTransition(HotelDetailView::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            hotelRenovationDesc.visibility = View.GONE
            hotelPayLaterInfo.visibility = View.GONE
            hotelVIPAccessInfo.visibility = View.GONE
            hotelMapView.visibility = View.GONE
            hotelDetailView.visibility = View.VISIBLE
        }
    }

    fun showDefault() {
        show(hotelDetailView)
    }

    private val detailToMap = object: ScaleTransition(this, HotelDetailView::class.java, HotelMapView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            hotelMapView.viewmodel.resetCameraPosition.onNext(Unit)
            if (forward) {
                hotelMapView.googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            } else {
                hotelMapView.googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
            }
        }
    }

    val detailToDescription = object : ScaleTransition(this, HotelDetailView::class.java, SpecialNoticeWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            hotelRenovationDesc.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    val hotelRenovationObserver = endlessObserver<Pair<String, String>> { text ->
        hotelRenovationDesc.setText(text)
        show(hotelRenovationDesc)
    }

    val detailToVIPAccessInfo = object : ScaleTransition(this, HotelDetailView::class.java, VIPAccessInfoWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            hotelVIPAccessInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    val hotelDetailsEmbeddedMapClickObserver = endlessObserver<Unit> {
        show(hotelMapView)
        hotelDetailView.viewmodel.trackHotelDetailMapViewClick()
    }

    val detailToPayLaterInfo = object : ScaleTransition(this, HotelDetailView::class.java, PayLaterInfoWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            hotelPayLaterInfo.visibility = if (forward) View.VISIBLE else View.GONE
            hotelDetailView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    val detailToDepositInfo = object : ScaleTransition(this, HotelDetailView::class.java, DepositTermsInfoWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
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

    fun animationStart(): Float {
        searchTop = hotelDetailView.hotelDetailsToolbar.toolbarTitle.top
        hotelDetailView.hotelDetailsToolbar.visibility = View.VISIBLE
        hotelDetailView.hotelDetailsToolbar.toolbarTitle.translationY = searchTop.toFloat()
        hotelDetailView.hotelDetailsToolbar.toolBarRating.translationY = searchTop.toFloat()
        return hotelDetailView.hotelDetailsToolbar.alpha
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        val yTrans = if (forward) -(searchTop * -f) else (searchTop * (1 - f))
        hotelDetailView.hotelDetailsToolbar.toolbarTitle.translationY = yTrans
        hotelDetailView.hotelDetailsToolbar.toolBarRating.translationY = yTrans
    }

    fun animationFinalize() {
        hotelDetailView.hotelDetailsToolbar.visibility = View.VISIBLE
        hotelDetailView.hotelDetailsToolbar.visibility = View.VISIBLE
        hotelDetailView.hotelDetailsToolbar.toolbarTitle.translationY = 0f
        hotelDetailView.hotelDetailsToolbar.toolBarRating.translationY = 0f
    }

    override fun back(): Boolean {
        if (hotelDetailView.hotelDetailsToolbar.navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type) {
            hotelDetailView.collapseGallery()
            return true
        }
        return super.back()
    }

}
