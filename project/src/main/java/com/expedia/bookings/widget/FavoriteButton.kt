package com.expedia.bookings.widget

import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.animation.HotelFavoriteBurstAnimation
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.utils.Strings
import com.mobiata.android.Log

class FavoriteButton(context: Context, attrs : AttributeSet) : ImageView(context, attrs), View.OnClickListener {

    var hotelId : String = ""
    var isInDetailView = false
    val HEART_BURST_RADIUS = 36
    val HEART_BURST_DURATION: Long = 600
    val HEART_IMAGE_WAIT_TIME: Long = 200

    // imgId is for testing propose only
    var imgId : Int? = null

    init {
        setOnClickListener(this)
        setImageResource(R.drawable.favoriting_unselected)
    }

    override fun onClick(v: View?) {
        if (Strings.isEmpty(hotelId)) {
            Log.w("hotelId for FavoriteButton is not set")
            return
        }
        toggleHotelState()
    }

    private fun toggleHotelState() {
        var toastWillShow = false;

        if (HotelFavoriteHelper.isFirstTimeFavoriting(context)) {
            makeFirstTimeDialog()
            toastWillShow = true
        }
        HotelFavoriteHelper.toggleHotelFavoriteState(context, hotelId)
        HotelFavoriteHelper.trackToggleHotelFavoriteState(context, hotelId, (parent as FrameLayout).id)
        updateImageState(true)

        if (!toastWillShow && !isInDetailView && HotelFavoriteHelper.isHotelFavorite(context, hotelId)) { makeToast() }
    }

    fun updateImageState(animated: Boolean) {
        if (HotelFavoriteHelper.isHotelFavorite(context, hotelId)) {
            if (animated) {
                val displayMetrics = resources.displayMetrics
                val px = Math.round(HEART_BURST_RADIUS * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
                val burstDrawable = HotelFavoriteBurstAnimation(ContextCompat.getColor(context, R.color.hotel_favorite_button), px, HEART_BURST_DURATION)
                background = burstDrawable
                burstDrawable.startAnimation()
            }
            val handler = Handler()
            handler.postDelayed({
                val imageRes = if (isInDetailView) R.drawable.favoriting_selected else R.drawable.favoriting_selected
                setImageResource(imageRes)
            }, if (animated) HEART_IMAGE_WAIT_TIME else 0)
        } else {
            val imageRes = if (isInDetailView) R.drawable.favoriting_unselected else R.drawable.favoriting_unselected
            setImageResource(imageRes)
        }
    }

    fun updateImageState() { updateImageState(false) }

    override fun setImageResource(resID: Int) {
        this.imgId = resID
        super.setImageResource(resID)
    }

    private fun makeToast() {
        val toast_message = resources.getString(R.string.favorite_toast)
        Toast.makeText(context, toast_message, Toast.LENGTH_LONG).show()
    }

    private fun makeFirstTimeDialog() {
        AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.favorite_first_time_dialog_title))
            .setMessage(resources.getString(R.string.favorite_first_time_dialog_content))
            .setPositiveButton(resources.getString(R.string.favorite_first_time_dialog_ok_button),
                    { dialogInterface, i ->
                        if (!isInDetailView){
                            makeToast()
                        }
                    })
            .show()
    }
}
