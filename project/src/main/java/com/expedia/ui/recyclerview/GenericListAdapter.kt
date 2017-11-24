package com.expedia.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.expedia.ui.recyclerview.interfaces.VHClickable
import android.view.LayoutInflater
import android.view.View
import com.expedia.ui.recyclerview.interfaces.HolderAdapterBridge
import com.expedia.ui.recyclerview.interfaces.RecyclerViewContract
import com.expedia.ui.recyclerview.viewholders.VHWrapper
import rx.subjects.PublishSubject

/**
 * Created by nbirla on 15/11/17.
 */
class GenericListAdapter(var itemList: FeedItemList, val contract: RecyclerViewContract) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), HolderAdapterBridge {

    var clickSubject: PublishSubject<VHWrapper>? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        val viewHolder = contract.createHolder(LayoutInflater.from(parent!!.getContext()), parent, viewType)
        viewHolder.setVHClickCallback(clickSubject)
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

    fun setVHClickSubject(clickSubject: PublishSubject<VHWrapper>){
        this.clickSubject = clickSubject
    }

    override fun notifyVHChanged(position: Int) {
        notifyItemChanged(position);
    }
}