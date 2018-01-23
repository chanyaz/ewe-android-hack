package com.expedia.bookings.widget

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.bitmaps.PicassoTarget
import com.expedia.bookings.data.lx.LXTheme
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject

class LXThemeListAdapter : LoadingRecyclerViewAdapter<LXTheme>() {
    var themeClickSubject = PublishSubject.create<LXTheme>()
    var imageCode: String? = null

    override fun loadingLayoutResourceId(): Int {
        return R.layout.lx_theme_loading_animation_widget
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemViewHolder: RecyclerView.ViewHolder? = super.onCreateViewHolder(parent, viewType)
        if (itemViewHolder == null) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_lx_theme, parent, false)
            itemViewHolder = ViewHolder(itemView)
        }
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.itemViewType == LoadingRecyclerViewAdapter.DATA_VIEW) {
            val theme = getItems()[position]
            (holder as ViewHolder).bind(theme, themeClickSubject, imageCode)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val themeImage: ImageView by bindView(R.id.theme_image)
        val themeCount: TextView by bindView(R.id.theme_count)
        val themeTitle: TextView by bindView(R.id.theme_title)
        val themeDescription: TextView by bindView(R.id.theme_description)
        val cardView: CardView by bindView(R.id.results_card_view)

        private var themeClickSubject: PublishSubject<LXTheme>? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val theme = v.tag as LXTheme
            themeClickSubject!!.onNext(theme)
        }

        fun bind(theme: LXTheme, subject: PublishSubject<LXTheme>, imageCode: String?) {
            this.themeClickSubject = subject
            itemView.tag = theme
            cardView.preventCornerOverlap = false
            themeTitle.text = theme.title
            val activitySize = theme.unfilteredActivities.size
            themeCount.text = activitySize.toString()
            themeDescription.text = theme.description

            if (activitySize in 1..9) {
                themeCount.background = ContextCompat.getDrawable(itemView.context, R.drawable.lx_category_count_background_more_than_one_digit)
            } else {
                themeCount.background = ContextCompat.getDrawable(itemView.context, R.drawable.lx_category_count_background_more_than_one_digit)
            }

            val imageURLs = Images.forLxCategory(itemView.context, theme.titleEN, imageCode, itemView.context.resources.getDimension(R.dimen.lx_category_image_width))

            val errorDrawable = LXDataUtils.getErrorDrawableForCategory(itemView.context, theme.titleEN)

            PicassoHelper
                    .Builder(itemView.context)
                    .setPlaceholder(R.drawable.results_list_placeholder)
                    .setError(errorDrawable)
                    .fade()
                    .setTag("CATEGORY_ROW")
                    .setTarget(target)
                    .build()
                    .load(imageURLs)
        }

        private val target = object : PicassoTarget() {
            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                super.onBitmapLoaded(bitmap, from)
                val palette = Palette.Builder(bitmap).generate()
                val color = palette.getDarkVibrantColor(R.color.transparent_dark)
                val overlayColorBuilder = ColorBuilder(color).darkenBy(.5f)
                val overlayColor = overlayColorBuilder.setAlpha(154).build()

                val overlayDrawable = ColorDrawable()
                overlayDrawable.color = overlayColor
                val drawable = HeaderBitmapDrawable()
                drawable.setBitmap(bitmap)
                drawable.setOverlayDrawable(overlayDrawable)
                themeImage.setImageDrawable(drawable)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                super.onBitmapFailed(errorDrawable)
                themeImage.setImageDrawable(errorDrawable)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                super.onPrepareLoad(placeHolderDrawable)
                themeImage.setImageDrawable(placeHolderDrawable)
            }
        }
    }

    fun setThemes(themes: List<LXTheme>, themePublishSubject: PublishSubject<LXTheme>) {
        themeClickSubject = themePublishSubject
        setItems(themes)
    }

    fun setDestinationImageCode(imageCode: String?) {
        this.imageCode = imageCode
    }
}
