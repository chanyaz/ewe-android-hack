package com.expedia.bookings.meso

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
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.meso.vm.MesoDestinationViewModel
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso

class MesoDestinationViewHolder(private val adView: View, private val vm: MesoDestinationViewModel) : RecyclerView.ViewHolder(adView) {
    private val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    private val cityView: TextView by bindView(R.id.meso_destination_city)
    private val subtitle: TextView by bindView(R.id.meso_destination_subtitle)
    private val sponsored: TextView by bindView(R.id.meso_destination_sponsored)
    private val gradient: View by bindView(R.id.meso_destination_foreground)
    private val bgImageView: ImageView by bindView(R.id.meso_destination_card_background)

    fun bindData() {
        if (vm.mesoDestinationAdResponse != null) {
            cityView.text = vm.title
            subtitle.text = vm.description
            sponsored.text = vm.sponsoredText
            bgImageView.background = null
            Picasso.with(adView.context).load(vm.backgroundUrl).error(vm.backgroundFallback).placeholder(vm.backgroundPlaceHolder).into(target)
        }
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
            val color = palette.getDarkVibrantColor(adView.context.resources.getColor(R.color.transparent_dark))
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