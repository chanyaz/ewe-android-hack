package com.expedia.bookings.hotel.widget.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.util.HotelFavoriteCache
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeOnClick
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject
import java.math.BigDecimal

class HotelCompareAdapter(private val context: Context, private val listener: CompareCheckListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface CompareCheckListener {
        fun compareCheckChanged(hotelId: String, checked: Boolean)
    }

    val fetchPricesSubject = PublishSubject.create<Unit>()

    enum class Type {
        HEADER,
        LOADING_VIEW,
        HOTEL_INFO
    }

    private var listData: List<HotelOffersResponse> = emptyList()

    fun updateHotels(list: List<HotelOffersResponse>) {
        this.listData = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listData.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return Type.HEADER.ordinal
        }
        else {
            return Type.HOTEL_INFO.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HotelCompareAdapter.Type.HEADER.ordinal) {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.hotel_compare_list_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.hotel_compare_list_info_cell, parent, false)
            val holder = HotelInfoHolder(view)
            holder.comparedCheckedSubject.subscribe { checked ->
                val position = holder.adapterPosition
                if (position != NO_POSITION) {
                    listener.compareCheckChanged(listData[position - 1].hotelId, checked)
                }
            }
            holder.fetchPriceSubject.subscribe(fetchPricesSubject)
            return  holder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is HotelInfoHolder) {
            val hotelInfo = listData[position - 1]
            if (hotelInfo != null) {
                holder.bind(hotelInfo)
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

    class HotelInfoHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val comparedCheckedSubject = PublishSubject.create<Boolean>()
        val fetchPriceSubject = PublishSubject.create<Unit>()

        val compareCheckBox: CheckBox by bindView(R.id.hotel_info_compare_check_box)
        val hotelImage: ImageView by bindView(R.id.hotel_info_background_image)
        val hotelName: TextView by bindView(R.id.hotel_info_name)
        val guestRating: TextView by bindView(R.id.hotel_info_guest_rating)
        val guestReviewCount: TextView by bindView(R.id.hotel_info_total_guest_count)
        val hotelCity: TextView by bindView(R.id.hotel_info_city)

        val fetchPriceButton : TextView by bindView(R.id.hotel_info_fetch_price_button)
        val priceTextView: TextView by bindView(R.id.hotel_info_display_price)

        init {
            compareCheckBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    comparedCheckedSubject.onNext(isChecked)
                }
            })

            fetchPriceButton.subscribeOnClick(fetchPriceSubject)
        }

        fun bind(offersResponse: HotelOffersResponse) {
            hotelName.text = offersResponse.hotelName
            guestRating.text = "${offersResponse.hotelGuestRating.toString()}/5"
            guestReviewCount.text = "(${offersResponse.totalReviews} reviews)"
            hotelCity.text = offersResponse.hotelCity

            val background = ContextCompat.getDrawable(view.context, R.drawable.confirmation_background)

            Picasso.with(view.context)
                    .load(getHotelImage(offersResponse.photos[0]).getUrl(HotelMedia.Size.Y))
                    .placeholder(background)
                    .error(background)
                    .into(hotelImage)

            if (offersResponse.hotelRoomResponse != null
                    && offersResponse.hotelRoomResponse.isNotEmpty()) {
                fetchPriceButton.visibility = View.GONE
                priceTextView.visibility = View.VISIBLE
                val rate = offersResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo
                val displayText = Money(BigDecimal(rate.averageRate.toDouble()),
                        rate.currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
                priceTextView.text = displayText
            }
        }

        private fun getHotelImage(photo: HotelOffersResponse.Photos) : HotelMedia {
            return HotelMedia(Images.getMediaHost() + photo.url, photo.displayText)
        }
    }
}