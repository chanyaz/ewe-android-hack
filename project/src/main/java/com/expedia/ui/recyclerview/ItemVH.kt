package com.expedia.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.ButterKnife
import com.expedia.ui.recyclerview.interfaces.VHClickable
import com.expedia.ui.recyclerview.interfaces.HolderAdapterBridge

/**
 * Created by nbirla on 15/11/17.
 */
abstract class ItemVH<V>(val root: View) : RecyclerView.ViewHolder(root), View.OnClickListener {

    protected var adapterBridge: HolderAdapterBridge? = null
    private var feedItem: FeedItem<V>? = null
    private var clickCallback: VHClickable? = null

    init {
        ButterKnife.inject(this, root);
    }

    fun getParent(): View {
        return root
    }

    fun bindFeedItem(feedItem: FeedItem<*>) {
        val feedItem = FeedItem<V>(feedItem as FeedItem<V>)
        this.feedItem = feedItem
        bindData(feedItem.getBindingData())
    }

    protected abstract fun bindData(v: V)

    protected fun getFeedItem(): FeedItem<V>? {
        return feedItem
    }

    override fun onClick(v: View) {
        clickCallback!!.onViewHolderClicked(this, v)
    }

    fun setVHClickCallback(callback: VHClickable) {
        this.clickCallback = callback
    }

    fun getVHClickable(): VHClickable? {
        return clickCallback
    }

    fun setGLAdapterBridge(adapterBridge: HolderAdapterBridge) {
        this.adapterBridge = adapterBridge
    }

    protected fun notifyVHChanged() {
        adapterBridge!!.notifyVHChanged(adapterPosition)
    }
}