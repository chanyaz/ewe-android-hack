package com.expedia.bookings.widget;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
	public static int index = 0;

	@InjectView(R.id.background_image_view)
	public ImageView backgroundImageView;

	@InjectView(R.id.loading_card_view)
	public CardView cardView;

	public LoadingViewHolder(View view) {
		super(view);
		ButterKnife.inject(this, itemView);
	}

}
