package com.expedia.bookings.meso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.net.Uri
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.meso.vm.MesoDestinationViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils.startActivity
import com.squareup.picasso.Picasso

class MesoDestinationViewHolder(private val adView: View, private val vm: MesoDestinationViewModel) : RecyclerView.ViewHolder(adView), View.OnClickListener {
    private val DEFAULT_GRADIENT_POSITIONS = floatArrayOf(0f, .3f, .6f, 1f)

    private val cityView: TextView by bindView(R.id.meso_destination_city)
    private val subtitle: TextView by bindView(R.id.meso_destination_subtitle)
    private val sponsored: TextView by bindView(R.id.meso_destination_sponsored)
    private val gradient: View by bindView(R.id.meso_destination_foreground)
    private val bgImageView: ImageView by bindView(R.id.meso_destination_card_background)

    init {
        itemView.setOnClickListener(this)
    }

    fun bindData() {
        if (vm.mesoDestinationAdResponse != null) {
            cityView.text = vm.title
            subtitle.text = vm.description
            sponsored.text = vm.sponsoredText
            bgImageView.background = null
            Picasso.with(adView.context).load(vm.backgroundUrl).error(vm.backgroundFallback).placeholder(vm.backgroundPlaceHolder).into(target)
        }
    }

    override fun onClick(view: View) {
        val url = vm.mesoDestinationAdResponse?.webviewUrl
        if (url != null) {
            goToMesoDestination(view.context, url)
            OmnitureTracking.trackMesoDestination(vm.title)
        }
    }

    private fun goToMesoDestination(context: Context, url: String) {
        val builder = WebViewActivity.IntentBuilder(context)
        builder.setUrl(appendParameters(url))
        builder.setTitle(vm.title)
        builder.setMesoDestinationPage(true)
        builder.setHandleBack(true)
        builder.setRetryOnFailure(true)
        startActivity(context, builder.intent, null)
    }

    private fun appendParameters(baseUrl: String): String {
        val linkBuilder = Uri.parse(baseUrl).buildUpon()
        linkBuilder.appendQueryParameter("rfrr", "App.LS.MeSo")
        linkBuilder.appendQueryParameter("mcicid", "App.LS.MeSo.Dest." + vm.title)
        return linkBuilder.build().toString()
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
            val startColor = fullColorBuilder.setAlpha(254).build()
            val endColor = fullColorBuilder.setAlpha(100).build()
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
