package com.expedia.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.ui.recyclerview.interfaces.VHClickable
import android.view.LayoutInflater
import android.view.View
import com.expedia.ui.recyclerview.interfaces.HolderAdapterBridge
import com.expedia.ui.recyclerview.interfaces.RecyclerViewContract

/**
 * Created by nbirla on 15/11/17.
 */
class GenericListAdapter(var itemList: FeedItemList, val contract: RecyclerViewContract) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), VHClickable, HolderAdapterBridge {

    var clickCallback: VHClickable? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        val viewHolder = contract.createHolder(LayoutInflater.from(parent!!.getContext()), parent, viewType)
        viewHolder.setVHClickCallback(this)
        return viewHolder

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        if (holder !is ItemVH<*>) return

        holder.setGLAdapterBridge(this)

        val feedItem = getItem(position)

        holder.bindFeedItem(feedItem)

    }

    private fun getItem(position: Int): FeedItem<*> {
        return itemList!!.get(position)
    }

    fun updateItems(list: FeedItemList) {
        itemList = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return contract.getViewType(getItem(position).getFeedItemViewType());
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setVHClickCallback(clickCallback: VHClickable) {
        this.clickCallback = clickCallback
    }


    override fun onViewHolderClicked(holder: ItemVH<*>, view: View) {
        clickCallback!!.onViewHolderClicked(holder, view);
    }

    override fun notifyVHChanged(position: Int) {
        notifyItemChanged(position);
    }
}