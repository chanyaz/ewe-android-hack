package com.expedia.ui.javademo.interfaces;

import android.view.View;

import com.expedia.ui.javademo.ItemVH;

/**
 * Created by nbirla on 15/11/17.
 */

public interface VHClickable {
    void onViewHolderClicked(ItemVH holder, View view);
}
