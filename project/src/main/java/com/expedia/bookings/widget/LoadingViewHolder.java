package com.expedia.bookings.widget;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoadingViewHolder extends RecyclerView.ViewHolder {
	private ValueAnimator animator;

	//@InjectView(R.id.background_image_view)
	public View backgroundImageView;

	public LoadingViewHolder(View view) {
		super(view);
		ButterKnife.inject(this, itemView);
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
