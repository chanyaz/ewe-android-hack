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
import com.expedia.bookings.data.lx.LXCategoryMetadata
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.ColorBuilder
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LXDataUtils
import com.expedia.bookings.utils.bindView
import com.squareup.picasso.Picasso
import rx.subjects.PublishSubject


class LXCategoryListAdapter : LoadingRecyclerViewAdapter<LXCategoryMetadata>() {
    var categoryMetadataSubject: PublishSubject<LXCategoryMetadata> = PublishSubject.create<LXCategoryMetadata>()
    var imageCode: String? = null

    override fun loadingLayoutResourceId(): Int {
        return R.layout.lx_category_loading_animation_widget
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemViewHolder: RecyclerView.ViewHolder? = super.onCreateViewHolder(parent, viewType)
        if (itemViewHolder == null) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_lx_category, parent, false)
            itemViewHolder = ViewHolder(itemView)
        }
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.itemViewType == LoadingRecyclerViewAdapter.DATA_VIEW) {
            val categories = getItems().get(position)
            (holder as ViewHolder).bind(categories, categoryMetadataSubject, imageCode)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val categoryImage: ImageView by bindView(R.id.category_image)
        val categoryCount: TextView by bindView(R.id.category_count)
        val categoryTitle: TextView by bindView(R.id.category_title)
        val cardView: CardView by bindView(R.id.results_card_view)

        private var categoryMetadataSubject: PublishSubject<LXCategoryMetadata>? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val category = v.tag as LXCategoryMetadata
            categoryMetadataSubject!!.onNext(category)
        }

        fun bind(category: LXCategoryMetadata, subject: PublishSubject<LXCategoryMetadata>, imageCode: String?) {
            this.categoryMetadataSubject = subject
            itemView.tag = category
            cardView.preventCornerOverlap = false
            categoryTitle.text = category.displayValue
            categoryCount.text = category.activities.size.toString()

            if (category.activities.size >= 1 && category.activities.size <= 9) {
                categoryCount.background = ContextCompat.getDrawable(itemView.context, R.drawable.lx_category_count_background_single_digit)
            } else  {
                categoryCount.background = ContextCompat.getDrawable(itemView.context, R.drawable.lx_category_count_background_more_than_one_digit)
            }

            var imageURLs = Images.forLxCategory(itemView.context, category.categoryKeyEN, imageCode, itemView.getContext().getResources().getDimension(R.dimen.lx_category_image_width))

            var errorDrawable = LXDataUtils.getErrorDrawableForCategory(itemView.context, category.categoryKeyEN);

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
                val color = palette.getLightVibrantColor(R.color.transparent_dark)
                val overlayColorBuilder = ColorBuilder(color).darkenBy(.5f);
                val overlayColor = overlayColorBuilder.setAlpha(154).build()

                val overlayDrawable = ColorDrawable()
                overlayDrawable.color = overlayColor
                val drawable = HeaderBitmapDrawable()
                drawable.setBitmap(bitmap)
                drawable.setOverlayDrawable(overlayDrawable)
                categoryImage.setImageDrawable(drawable)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                super.onBitmapFailed(errorDrawable)
                categoryImage.setImageDrawable(errorDrawable)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                super.onPrepareLoad(placeHolderDrawable)
                categoryImage.setImageDrawable(placeHolderDrawable)
            }
        }
    }

    fun setCategories(categories: List<LXCategoryMetadata>, categoryPublishSubject: PublishSubject<LXCategoryMetadata>) {
        categoryMetadataSubject = categoryPublishSubject
        setItems(categories)
    }

    fun setDestinationImageCode(imageCode: String?) {
        this.imageCode = imageCode
    }

}
