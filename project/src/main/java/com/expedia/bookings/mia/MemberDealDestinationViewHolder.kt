package com.expedia.bookings.mia

import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.mia.vm.MemberDealDestinationViewModel
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso

class MemberDealDestinationViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    val cityView: TextView by bindView(R.id.member_deal_city)
    val dateView: TextView by bindView(R.id.member_deal_date)
    val discountView: TextView by bindView(R.id.member_deal_discount_percentage)
    val strikePriceView: TextView by bindView(R.id.member_deal_strike_through_price)
    val priceView: TextView by bindView(R.id.member_deal_price_per_night)
    val bgImageView: ImageView by bindView(R.id.member_deal_background)
    val gradient: View by bindView(R.id.member_deal_foreground)

    val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    fun bind(vm: MemberDealDestinationViewModel) {
        cityView.text = vm.cityName
        dateView.text = vm.dateRangeText
        discountView.text = vm.percentSavingsText
        strikePriceView.text = vm.strikeOutPriceText
        priceView.text = vm.priceText
        Picasso.with(view.context).load(vm.backgroundUrl).error(vm.backgroundFallback).placeholder(vm.backgroundPlaceHolder).into(target)
    }

    private val target = object : PicassoTarget() {

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            super.onBitmapLoaded(bitmap, from)
            bgImageView.setImageBitmap(bitmap)
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
}

