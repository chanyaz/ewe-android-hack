package com.expedia.ui.javademo;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.ui.javademo.interfaces.HolderAdapterBridge;
import com.expedia.ui.javademo.interfaces.VHClickable;

/**
 * Created by nbirla on 15/11/17.
 */

public abstract class ItemVH<V> extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View parent;
    protected HolderAdapterBridge adapterBridge;
    private FeedItem feedItem;
    private VHClickable clickCallback;

    public ItemVH(View itemView) {
        super(itemView);
        parent = itemView;
    }

    public View getParent() {
        return parent;
    }

    public void bindFeedItem(FeedItem <V> feedItem) {
        this.feedItem = feedItem;
        bindData(feedItem.getBindingData());
    }

    protected abstract void bindData(V v);

    protected FeedItem<V> getFeedItem(){
        return feedItem;
    }

    @Override
    public void onClick(View v) {
        if(clickCallback != null){
            clickCallback.onViewHolderClicked(this, v);
        }
    }

    public void setVHClickCallback(VHClickable callback) {
        this.clickCallback = callback;
    }

    public VHClickable getVHClickable() {
        return clickCallback;
    }

    public void setGLAdapterBridge(HolderAdapterBridge adapterBridge) {
        this.adapterBridge = adapterBridge;
    }

    protected void notifyVHChanged() {
        if(adapterBridge != null) {
            adapterBridge.notifyVHChanged(getAdapterPosition());
        }
    }
}
