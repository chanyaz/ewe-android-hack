package com.expedia.bookings.launch.widget

import android.support.v7.widget.CardView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.meso.model.MesoHotelAdResponse
import com.expedia.bookings.meso.vm.MesoHotelAdViewModel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.bindView
import rx.Observer

class MesoHotelAdViewHolder(private var mesoAdView: View, private val mesoHotelAdViewModel: MesoHotelAdViewModel) : LaunchLoadingViewHolder(mesoAdView), ViewTreeObserver.OnPreDrawListener, Observer<MesoHotelAdResponse> {
    private val hotelBackgroundImage: ImageView by bindView(R.id.hotel_image_background)
    private val dealCardView: CardView by bindView(R.id.deal_card_view)
    private val percentageOff: TextView by bindView(R.id.price_off)
    private val hotelName: TextView by bindView(R.id.hotel_name)
    private val hotelSubText: TextView by bindView(R.id.hotel_sub_text)

    init {
        // Starts the loading animation in the parent class
        bind()
    }

    override fun onPreDraw(): Boolean {
        adjustDealPositionToBeOnImageBorder()
        adjustDealCornerRadiusToMaintainPillShape()
        adjustLineBreak()
        itemView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }

    fun fetchHotelMesoAd() {
        mesoHotelAdViewModel.fetchHotelMesoAd(this)
    }

    fun bindListData() {
        if (mesoHotelAdViewModel.mesoHotelAdResponse != null) {

            // A pre draw listener is added so that we can correct the pill shape of the price off cardView,
            // readjust the position of the pill cardView, and adjust the text format if the subText breaks multiple
            // lines.
            itemView.viewTreeObserver.addOnPreDrawListener(this)
            hotelBackgroundImage.setImageDrawable(mesoHotelAdViewModel.backgroundImage)

            // The gray change animation leaves the background color as a gray, so we need to set it back to null
            // to prevent overdraw
            backgroundImageView.background = null

            dealCardView.visibility = View.VISIBLE
            percentageOff.text = mesoHotelAdViewModel.percentageOff
            hotelName.text = mesoHotelAdViewModel.hotelName

            setSubText(mesoHotelAdViewModel.oneLineSubText)
        }
    }

    private fun adjustDealPositionToBeOnImageBorder() {
        val dealHeightAdjustment = dealCardView.height / 2
        val params = dealCardView.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, -dealHeightAdjustment, 0, 0)
        dealCardView.layoutParams = params
    }

    private fun adjustDealCornerRadiusToMaintainPillShape() {
        dealCardView.radius = dealCardView.height / 2f
    }

    private fun adjustLineBreak() {
        if (mesoHotelAdViewModel.shouldFormatSubText(hotelSubText.lineCount)) {
            setSubText(mesoHotelAdViewModel.twoLineSubText)
        }
    }

    // Sets the second line text on the meso card layout. If the text breaks more than 1 line, we will remove the
    // bullet character and use a new line character instead.
    private fun setSubText(htmlBody: String) {
        hotelSubText.text = HtmlCompat.fromHtml(htmlBody)
    }

    override fun onNext(mesoHotelAdResponse: MesoHotelAdResponse) {
        cancelAnimation()
        bindListData()
    }

    override fun onError(e: Throwable?) {
        mesoAdView.visibility = View.GONE
        val layoutParams = mesoAdView.layoutParams
        layoutParams.height = 0
        mesoAdView.layoutParams = layoutParams
    }

    override fun onCompleted() {
        // Not used
    }
}
