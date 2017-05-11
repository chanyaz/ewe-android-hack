package com.expedia.bookings.widget

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView

/**
 * A Viewholder for the case where our data are launch collections.
 */
class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener, HeaderBitmapDrawable.PicassoTargetListener {

    val cardView: CardView by bindView (R.id.card_view)
    val title: TextView by bindView(R.id.title)
    val subtitle: TextView by bindView(R.id.subtitle)
    val backgroundImage: ImageView by bindView(R.id.background_image)
    val browseHotelsLabel: TextView by bindView(R.id.browse_hotels_label)
    val gradient: View by bindView(R.id.gradient)
    var collectionUrl = ""

    init {
        itemView.setOnClickListener(this)
    }

    fun bindListData(data: Any, fullWidthTile: Boolean, showBrowseHotelsLabel: Boolean) {
        itemView.tag = data
        cardView.preventCornerOverlap = false

        if (fullWidthTile) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE.toFloat())
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE.toFloat())
        }
        browseHotelsLabel.visibility = if (showBrowseHotelsLabel) View.VISIBLE else View.GONE

        val location = data as CollectionLocation
        bindLocationData(location)
    }

    private fun bindLocationData(location: CollectionLocation) {
        title.text = location.title
        FontCache.setTypeface(title, FontCache.Font.ROBOTO_MEDIUM)
        subtitle.text = location.subtitle
        subtitle.visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        val animOptions = AnimUtils.createActivityScaleBundle(view)
        val collection = view.tag as CollectionLocation
        Events.post(Events.LaunchCollectionItemSelected(collection, animOptions, collectionUrl))
        OmnitureTracking.trackNewLaunchScreenTileClick(true)
    }

    override fun onBitmapLoaded() {
        gradient.visibility = View.VISIBLE
    }

    override fun onBitmapFailed() {
        gradient.visibility = View.GONE
    }

    override fun onPrepareLoad() {
        gradient.visibility = View.GONE
    }

    companion object {
        private val FULL_TILE_TEXT_SIZE = 18
        private val HALF_TILE_TEXT_SIZE = 15
    }
}
