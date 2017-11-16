package com.expedia.ui.javademo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.ui.javademo.interfaces.HolderAdapterBridge;
import com.expedia.ui.javademo.interfaces.RecyclerViewContract;
import com.expedia.ui.javademo.interfaces.VHClickable;

/**
 * Created by nbirla on 15/11/17.
 */

public class GenericListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements VHClickable, HolderAdapterBridge {

    private FeedItemList mItemList;
    private RecyclerViewContract contract;
    private VHClickable clickCallback;

    public GenericListAdapter(FeedItemList itemList, RecyclerViewContract contract){
        mItemList = itemList;
        this.contract = contract;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemVH viewHolder = contract.createHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        viewHolder.setVHClickCallback(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(!(holder instanceof ItemVH)) return;

        ItemVH itemVH = (ItemVH) holder;
        itemVH.setGLAdapterBridge(this);

        FeedItem feedItem = getItem(position);

        itemVH.bindFeedItem(feedItem);
    }

    private FeedItem getItem(int position){
        return mItemList.get(position);
    }

    public void updateItems(FeedItemList list) {
        mItemList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return contract.getViewType(getItem(position).getViewType());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void setVHClickCallback(VHClickable clickCallback) {
        this.clickCallback = clickCallback;
    }

    @Override
    public void onViewHolderClicked(ItemVH holder, View view) {
        if(clickCallback != null){
            clickCallback.onViewHolderClicked(holder, view);
        }
    }

    @Override
    public void notifyVHChanged(int position) {
        notifyItemChanged(position);
    }
}
