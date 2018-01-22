package com.expedia.bookings.launch.widget

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.meso.vm.MesoHotelAdViewModel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.mobiata.android.Log
import com.mobiata.android.text.StrikethroughTagHandler

class MesoHotelAdViewHolder(private var mesoAdView: View, private val mesoHotelAdViewModel: MesoHotelAdViewModel) : RecyclerView.ViewHolder(mesoAdView), ViewTreeObserver.OnPreDrawListener, View.OnClickListener {
    private val hotelBackgroundImage: ImageView by bindView(R.id.hotel_image_background)
    private val dealCardView: CardView by bindView(R.id.deal_card_view)
    private val percentageOff: TextView by bindView(R.id.price_off)
    private val hotelName: TextView by bindView(R.id.hotel_name)
    private val hotelSubText: TextView by bindView(R.id.hotel_sub_text)

    override fun onPreDraw(): Boolean {
        adjustDealPositionToBeOnImageBorder()
        adjustDealCornerRadiusToMaintainPillShape()
        adjustLineBreak()
        itemView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }

    fun bindListData() {
        if (mesoHotelAdViewModel.mesoHotelAdResponse != null) {
            if (mesoHotelAdViewModel.dataIsValid()) {
                // Let's not even add a click listener unless the data is valid
                itemView.setOnClickListener(this)

                // A pre draw listener is added so that we can correct the pill shape of the price off cardView,
                // readjust the position of the pill cardView, and adjust the text format if the subText breaks multiple
                // lines.
                itemView.viewTreeObserver.addOnPreDrawListener(this)
                hotelBackgroundImage.setImageDrawable(mesoHotelAdViewModel.backgroundImage)

                dealCardView.visibility = View.VISIBLE
                percentageOff.text = mesoHotelAdViewModel.percentageOff
                hotelName.text = mesoHotelAdViewModel.hotelName

                setSubText(mesoHotelAdViewModel.oneLineSubText)
            } else {
                Log.d("Some of the meso data is missing")
            }
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
        hotelSubText.text = HtmlCompat.fromHtml(htmlBody, null, StrikethroughTagHandler())
    }

    override fun onClick(v: View?) {
        if (mesoHotelAdViewModel.dataIsValid()) {
            val flags = NavUtils.FLAG_PINNED_SEARCH_RESULTS
            HotelNavUtils.goToHotelsV2Params(mesoAdView.context, mesoHotelAdViewModel.hotelParamsForSearch, null, flags)
        }
    }
}
