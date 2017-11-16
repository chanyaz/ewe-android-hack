package com.expedia.ui.javademo;

import java.util.ArrayList;

/**
 * Created by nbirla on 15/11/17.
 */

public class FeedItemList extends ArrayList<FeedItem> {

    private int INVALID_POSITION = -1;

    public int addItem(FeedItem feedItem){
        if (feedItem == null) return INVALID_POSITION;
        add(feedItem);
        return indexOf(feedItem);
    }
}
