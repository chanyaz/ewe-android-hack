package com.expedia.bookings.widget;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.R2;

import com.expedia.bookings.R2;


import butterknife.ButterKnife;
import butterknife.BindView;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
	private ValueAnimator animator;

	@BindView(R2.id.background_image_view)
	public View backgroundImageView;

	public LoadingViewHolder(View view) {
		super(view);
		ButterKnife.bind(this, itemView);
	}

	public void cancelAnimation() {
		if (animator != null) {
			animator.cancel();
		}
	}

	public void setAnimator(ValueAnimator animation) {
		animator = animation;
	}

}
