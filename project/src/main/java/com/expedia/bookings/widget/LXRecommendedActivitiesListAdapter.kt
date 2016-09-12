package com.expedia.bookings.widget

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LXDataUtils
import com.mobiata.android.util.AndroidUtils
import com.squareup.picasso.Picasso
import java.util.ArrayList

class LXRecommendedActivitiesListAdapter : BaseAdapter() {

    private var activities: List<LXActivity> = ArrayList()

    fun setActivities(activities: List<LXActivity>) {
        this.activities = activities
    }

    override fun getCount(): Int {
        return activities.size
    }

    override fun getItem(position: Int): LXActivity {
        return activities[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewLocal = convertView
        val offer = getItem(position)
        val viewHolder: ViewHolder
        if (convertViewLocal == null) {
            convertViewLocal = initializeViewHolder(parent)
        }

        viewHolder = convertViewLocal.tag as ViewHolder
        viewHolder.bind(offer)
        return convertViewLocal
    }

    protected fun initializeViewHolder(parent: ViewGroup): View {
        val convertView = LayoutInflater.from(parent.context).inflate(R.layout.section_lx_you_might_also_like, parent, false)
        val viewHolder = ViewHolder(convertView)
        convertView.tag = viewHolder

        return convertView
    }

    inner class ViewHolder(private val itemView: View) : View.OnClickListener {

        private var activity: LXActivity? = null

        val activityTitle = itemView.findViewById(R.id.activity_title) as TextView
        val activityImage = itemView.findViewById(R.id.activity_image) as ImageView
        val fromPriceTicketType = itemView.findViewById(R.id.activity_from_price_ticket_type) as TextView
        val activityPrice = itemView.findViewById(R.id.activity_price) as TextView
        val cardView = itemView.findViewById(R.id.results_card_view) as CardView
        val duration = itemView.findViewById(R.id.activity_duration) as TextView
        val gradientMask = itemView.findViewById(R.id.gradient_mask)
        val activityOriginalPrice = itemView.findViewById(R.id.activity_original_price) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            OmnitureTracking.trackLinkLXRecommendedActivity();
            Events.post(Events.LXActivitySelected(activity))
        }

        fun bind(activity: LXActivity) {

            this.activity = activity
            activityTitle.text = activity.title

            LXDataUtils.bindPriceAndTicketType(itemView.context, activity.fromPriceTicketCode, activity.price,
                    activity.originalPrice, activityPrice, fromPriceTicketType)
            LXDataUtils.bindOriginalPrice(itemView.context, activity.originalPrice, activityOriginalPrice)
            LXDataUtils.bindDuration(itemView.context, activity.duration, activity.isMultiDuration, duration)

            val imageURLs = Images.getLXImageURLBasedOnWidth(activity.images,
                    AndroidUtils.getDisplaySize(itemView.context).x)
            PicassoHelper.Builder(itemView.context)
                    .setPlaceholder(R.drawable.results_list_placeholder)
                    .setError(R.drawable.itin_header_placeholder_activities)
                    .fade()
                    .setTag("ROW_PICASSO_TAG")
                    .setTarget(target)
                    .build()
                    .load(imageURLs)
        }

        private val target = object : PicassoTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                super.onBitmapLoaded(bitmap, from)
                activityImage.setImageBitmap(bitmap)
                gradientMask.visibility = View.VISIBLE
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                super.onBitmapFailed(errorDrawable)
                if (errorDrawable != null) {
                    activityImage.setImageDrawable(errorDrawable)
                    gradientMask.visibility = View.VISIBLE
                }
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                super.onPrepareLoad(placeHolderDrawable)
                activityImage.setImageDrawable(placeHolderDrawable)
                gradientMask.visibility = View.GONE
            }
        }
    }
}
