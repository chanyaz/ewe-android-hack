package com.expedia.ui.javademo.interfaces;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.expedia.ui.javademo.ItemVH;

/**
 * Created by nbirla on 15/11/17.
 */

public interface RecyclerViewContract {
    ItemVH createHolder(LayoutInflater inflater, ViewGroup parent, int type);
    int getViewType(String type);
}
