package com.expedia.bookings.widget.packages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso

public class CheckoutOverviewHeader(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val checkoutHeaderImage: ImageView by bindView(R.id.hotel_checkout_room_image)
    val destinationText: TextView by bindView(R.id.destination)
    val checkInOutDates: TextView by bindView(R.id.check_in_out_dates)
    val travelers: TextView by bindView(R.id.travelers)

    init {
        View.inflate(context, R.layout.checkout_overview_header, this)
    }

    public fun update(hotel: HotelCreateTripResponse.HotelProductResponse, size: Int) {
        destinationText.text = Phrase.from(context, R.string.hotel_city_country_checkout_header_TEMPLATE)
                .put("city", hotel.hotelCity)
                .put("country", Db.getPackageParams().destination.hierarchyInfo?.country?.name)
                .format()
        checkInOutDates.text = DateFormatUtils.formatPackageDateRange(context, hotel.checkInDate, hotel.checkOutDate)
        var numTravelers = Db.getPackageParams().guests()
        travelers.text = resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
        PicassoHelper.Builder(context)
                .setPlaceholder(R.drawable.room_fallback)
                .setError(R.drawable.room_fallback)
                .setTarget(picassoTarget)
                .build()
                .load(HotelMedia(Images.getMediaHost() + hotel.largeThumbnailUrl).getBestUrls(size / 2))
    }

    val picassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            val drawable = HeaderBitmapDrawable()
            drawable.setCornerRadius(resources.getDimensionPixelSize(R.dimen.hotel_checkout_image_corner_radius))
            drawable.setCornerMode(HeaderBitmapDrawable.CornerMode.TOP)
            drawable.setBitmap(bitmap)

            var textColor: Int
            if (!mIsFallbackImage) {
                // only apply gradient treatment to hotels with images #5647
                val fullColorBuilder = ColorBuilder(resources.getColor(R.color.packages_primary_color))
                val gradientColor = fullColorBuilder.setAlpha(154).build()
                val colorArrayBottom = intArrayOf(gradientColor, gradientColor)
                drawable.setGradient(colorArrayBottom, floatArrayOf(0f, 1f))
                textColor = ContextCompat.getColor(context, R.color.itin_white_text);
            } else {
                textColor = ContextCompat.getColor(context, R.color.text_black)
            }
            destinationText.setTextColor(textColor)
            checkInOutDates.setTextColor(textColor)
            checkoutHeaderImage.setImageDrawable(drawable)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)

            if (placeHolderDrawable != null) {
                checkoutHeaderImage.setImageDrawable(placeHolderDrawable)

                val textColor = ContextCompat.getColor(context, R.color.text_black)
                destinationText.setTextColor(textColor)
                checkInOutDates.setTextColor(textColor)

            }
        }
    }
}