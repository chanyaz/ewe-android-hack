package com.expedia.ui.recyclerview

/**
 * Created by nbirla on 15/11/17.
 */
class FeedItemList : ArrayList<FeedItem<*>>() {

    val INVALID_POSITION = -1

    fun addItem(feedItem: FeedItem<*>): Int {
        if (feedItem == null) return INVALID_POSITION
        add(feedItem)
        return indexOf(feedItem)
    }

}