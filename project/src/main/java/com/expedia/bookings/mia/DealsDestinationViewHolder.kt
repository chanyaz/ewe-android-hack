package com.expedia.bookings.mia

import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.DeprecatedHotelSearchParams
import com.expedia.bookings.enums.DiscountColors
import com.expedia.bookings.mia.activity.LastMinuteDealActivity
import com.expedia.bookings.mia.activity.MemberDealsActivity
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBrandColorEnabled
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso

class DealsDestinationViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    val titleView: TextView by bindView(R.id.deals_title)
    private val dateView: TextView by bindView(R.id.deals_date)
    private val discountView: TextView by bindView(R.id.deals_discount_percentage)
    private val strikePriceView: TextView by bindView(R.id.deals_strike_through_price)
    private val priceView: TextView by bindView(R.id.deals_price_per_night)
    private val bgImageView: ImageView by bindView(R.id.deals_background)
    private val gradient: View by bindView(R.id.deals_foreground)
    private val cardView: CardView by bindView(R.id.deals_cardview)
    val dealsSubtitle: TextView by bindView(R.id.deals_subtitle)
    lateinit var searchParams: DeprecatedHotelSearchParams
    private lateinit var discountPercent: String

    fun bind(vm: DealsDestinationViewModel) {
        discountView.visibility = View.VISIBLE
        titleView.text = getTitle(vm)
        dateView.text = vm.dateRangeText
        discountView.text = vm.percentSavingsText
        strikePriceView.text = vm.strikeOutPriceText
        priceView.text = vm.priceText
        dealsSubtitle.text = getSubtitle(vm)
        discountPercent = vm.getDiscountPercentForContentDesc(vm.leadingHotel.hotelPricingInfo?.percentSavings)

        val backgroundUrl = getBackgroundUrl(vm)

        PicassoHelper.Builder(view.context)
                .setPlaceholder(vm.backgroundPlaceHolder)
                .setError(vm.backgroundFallback)
                .setTarget(target)
                .build()
                .load(listOf(backgroundUrl, vm.memberDealBackgroundUrl))

        searchParams = setSearchParams(vm)
        cardView.contentDescription = getDealsContentDesc()

        setDiscountColors()
        hideDiscountViewWithNoDiscount()
    }

    fun setSearchParams(vm: DealsDestinationViewModel): DeprecatedHotelSearchParams {
        val params = DeprecatedHotelSearchParams()
        params.regionId = vm.regionId
        params.checkInDate = vm.startDate
        params.checkOutDate = vm.endDate
        params.searchType = DeprecatedHotelSearchParams.SearchType.CITY
        params.hotelId = getHotelId(vm)
        params.query = vm.cityName
        params.numAdults = getNumAdults(vm)
        return params
    }

    private fun getNumAdults(vm: DealsDestinationViewModel): Int {
        if (view.context is LastMinuteDealActivity) {
            return vm.numberOfLastMinuteDealTravelers
        }
        return vm.numberOfMemberOnlyDealTravelers
    }

    private fun getSubtitle(vm: DealsDestinationViewModel): String? {
        if (view.context is LastMinuteDealActivity) {
            return vm.cityName
        }
        return view.context.getString(R.string.deals_hotel_only)
    }

    private fun getBackgroundUrl(vm: DealsDestinationViewModel): String? {
        return if (view.context is MemberDealsActivity) {
            vm.memberDealBackgroundUrl
        } else {
            vm.lastMinuteDealsBackgroundUrl
        }
    }

    private fun getTitle(vm: DealsDestinationViewModel): String? {
        return if (view.context is MemberDealsActivity) {
            vm.cityName
        } else {
            vm.hotelName
        }
    }

    private fun getHotelId(vm: DealsDestinationViewModel): String? {
        return if (view.context is LastMinuteDealActivity) {
            vm.hotelId
        } else null
    }

    fun getDealsContentDesc(): CharSequence {
        val result = SpannableBuilder()

        result.append(titleView.text.toString() + ".")
        result.append(DateFormatUtils.formatPackageDateRangeContDesc(view.context, searchParams.checkInDate.toString(), searchParams.checkOutDate.toString()))

        if (discountView.text != null) {
            result.append(Phrase.from(view.context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE).put("percentage", discountPercent).format().toString())
        }

        if (strikePriceView.text != null) {
            result.append(Phrase.from(view.context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", strikePriceView.text)
                    .put("price", priceView.text)
                    .format()
                    .toString())
        } else {
            result.append(Phrase.from(view.context, R.string.hotel_card_view_price_cont_desc_TEMPLATE)
                    .put("price", priceView.text)
                    .format()
                    .toString())
        }

        result.append(Phrase.from(view.context, R.string.deals_hotel_only).format().toString())

        return result.build()
    }

    private val target = object : PicassoTarget() {

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)
            bgImageView.scaleType = ImageView.ScaleType.MATRIX
            bgImageView.setImageBitmap(bitmap)
            bgImageView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    bgImageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val matrix = bgImageView.imageMatrix
                    val imageViewWidth = bgImageView.width.toFloat()
                    val bitmapWidth = bitmap.width.toFloat()
                    val bitmapHeight = bitmap.height.toFloat()
                    val scaleRatio = imageViewWidth / bitmapWidth
                    matrix.setScale(scaleRatio, scaleRatio)
                    val shift = bitmapHeight * scaleRatio * Constants.SOS_IMAGE_SHIFT
                    matrix.postTranslate(0.5f, shift + 0.5f)
                    bgImageView.imageMatrix = matrix
                    return true
                }
            })

            Thread(Runnable {
                val palette = Palette.Builder(bitmap).generate()
                bgImageView.post {
                    mixColor(palette)
                }
            }).start()
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            super.onBitmapFailed(errorDrawable)
            if (errorDrawable != null) {
                bgImageView.setImageDrawable(errorDrawable)
            }
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            super.onPrepareLoad(placeHolderDrawable)
            bgImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            bgImageView.setImageDrawable(placeHolderDrawable)
            gradient.background = null
        }

        private fun mixColor(palette: Palette) {
            val color = palette.getDarkVibrantColor(R.color.transparent_dark)
            val fullColorBuilder = ColorBuilder(color).darkenBy(.6f).setSaturation(if (!mIsFallbackImage) .8f else 0f)
            val startColor = fullColorBuilder.setAlpha(154).build()
            val endColor = fullColorBuilder.setAlpha(0).build()
            val colorArrayBottom = intArrayOf(0, 0, endColor, startColor)
            val drawable = PaintDrawable()
            drawable.shape = RectShape()
            drawable.shaderFactory = getShader(colorArrayBottom)
            gradient.background = drawable
        }
    }

    private fun getShader(array: IntArray): ShapeDrawable.ShaderFactory {
        return object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                val lg = LinearGradient(0f, 0f, 0f, height.toFloat(),
                        array, DEFAULT_GRADIENT_POSITIONS, Shader.TileMode.REPEAT)
                return lg
            }
        }
    }

    private fun hideDiscountViewWithNoDiscount() {
        if (discountView.text.isEmpty()) {
            discountView.visibility = View.GONE
        }
    }

    private fun setDiscountColors() =
            if (isBrandColorEnabled(view.context) && view.context is MemberDealsActivity) {
                discountView.setBackgroundResource(DiscountColors.MEMBER_DEALS.backgroundColor)
                discountView.setTextColor(ContextCompat.getColor(view.context, DiscountColors.MEMBER_DEALS.textColor))
            } else {
                discountView.setBackgroundResource(DiscountColors.LAST_MINUTE_DEALS.backgroundColor)
                discountView.setTextColor(ContextCompat.getColor(view.context, DiscountColors.LAST_MINUTE_DEALS.textColor))
            }
}
