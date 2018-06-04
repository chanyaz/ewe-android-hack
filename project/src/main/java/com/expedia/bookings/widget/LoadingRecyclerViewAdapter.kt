package com.expedia.bookings.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.utils.AnimUtils
import java.util.ArrayList

/**
 * This adapter handle list loading animation with RecyclerView - This is to be used when we have to do a dummy
 * loading animation of the items till the time we do not have the real data.
 *
 * It has two item types - LOADING_VIEW & DATA_VIEW, LOADING_VIEW and animation is abstracted in this adapter and we need to provide
 * implementation for the DATA_VIEW in the inheriting class.
 *
 * Use the abstract method loadingLayoutResourceId() to provide your custom loading layout.
 * override onCreateViewHolder to create your custom data view.
 *
 * Use setDummyItems(items: List<T>) to show Loading state and supply data to setItems(items: List<T>)
 * and use flag to control whether you want to display dummy items (with your custom loading layout) or real items.
 */

abstract class LoadingRecyclerViewAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected abstract fun loadingLayoutResourceId(): Int
    protected var isLoading = false
    private var items: List<T> = ArrayList()

    private fun setItems(items: List<T>, areDummyItems: Boolean = false) {
        this.isLoading = areDummyItems
        this.items = items
        notifyDataSetChanged()
    }

    fun setDummyItems(items: List<T>) {
        setItems(items, true)
    }

    fun setItems(items: List<T>) {
        setItems(items, false)
    }

    fun getItems(): List<T> {
        return items
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == LOADING_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(loadingLayoutResourceId(), parent, false)
            return LoadingViewHolder(view)
        }
        return super.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == LOADING_VIEW) {
            val animation = AnimUtils.setupLoadingAnimation((holder as LoadingViewHolder).backgroundImageView, position % 2 == 0)
            holder.setAnimator(animation)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) LOADING_VIEW else DATA_VIEW
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == LOADING_VIEW) {
            (holder as LoadingViewHolder?)?.cancelAnimation()
        }
        super.onViewRecycled(holder)
    }

    companion object {
        @JvmStatic val LOADING_VIEW: Int = 0
        @JvmStatic val DATA_VIEW: Int = 1
        @JvmStatic val ACTIVITY_COUNT_HEADER_VIEW = 2
    }
}
