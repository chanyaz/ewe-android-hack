package com.expedia.bookings.widget.packages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BaseCheckoutOverviewViewModel
import com.squareup.picasso.Picasso

class CheckoutOverviewHeader(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    var checkoutHeaderImage: ImageView? = null
    val destinationText: TextView by bindView(R.id.destination)
    val checkInOutDates: TextView by bindView(R.id.check_in_out_dates)
    val travelers: TextView by bindView(R.id.travelers)

    init {
        View.inflate(context, R.layout.checkout_overview_header, this)
        orientation = VERTICAL
    }

    var viewmodel: BaseCheckoutOverviewViewModel by notNullAndObservable { vm ->
        vm.cityTitle.subscribeText(destinationText)
        vm.datesTitle.subscribeText(checkInOutDates)
        vm.travelersTitle.subscribeText(travelers)
        vm.url.subscribe { urls ->
            PicassoHelper.Builder(context)
                    .setPlaceholder(R.drawable.room_fallback)
                    .setError(R.drawable.room_fallback)
                    .setTarget(picassoTarget)
                    .build()
                    .load(urls)
        }
    }

    val picassoTarget = object : PicassoTarget() {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)

            val drawable = HeaderBitmapDrawable()
            drawable.setBitmap(bitmap)

            var textColor: Int
            if (!mIsFallbackImage) {
                // only apply gradient treatment to hotels with images #5647
                val fullColorBuilder = ColorBuilder(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
                val gradientColor = fullColorBuilder.setAlpha(230).build()
                val colorArrayBottom = intArrayOf(gradientColor, gradientColor)
                drawable.setGradient(colorArrayBottom, floatArrayOf(0f, 1f))
                textColor = ContextCompat.getColor(context, R.color.itin_white_text);
            } else {
                textColor = ContextCompat.getColor(context, R.color.text_black)
            }
            destinationText.setTextColor(textColor)
            checkInOutDates.setTextColor(textColor)
            checkoutHeaderImage?.setImageDrawable(drawable)
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)

            if (placeHolderDrawable != null) {
                checkoutHeaderImage?.setImageDrawable(placeHolderDrawable)

                val textColor = ContextCompat.getColor(context, R.color.text_black)
                destinationText.setTextColor(textColor)
                checkInOutDates.setTextColor(textColor)

            }
        }
    }
}