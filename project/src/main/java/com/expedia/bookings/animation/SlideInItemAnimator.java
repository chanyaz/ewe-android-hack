package com.expedia.bookings.animation;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.animation.OvershootInterpolator;

import com.expedia.bookings.launch.widget.LaunchDataItem;


public class SlideInItemAnimator extends DefaultItemAnimator {
	private RecyclerView recyclerView;

	public SlideInItemAnimator(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	@Override
	public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
		return true;
	}

	@Override
	public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX,
		int fromY, int toX, int toY) {

		return false;
	}

	@Override
	public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
		return false;
	}

	@Override
	public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
		boolean animated;
		boolean shouldRunEnterAnimation = (viewHolder.getItemViewType() == LaunchDataItem.ITIN_VIEW || viewHolder.getItemViewType() == LaunchDataItem.POPULAR_HOTELS);

		if (shouldRunEnterAnimation) {
			runEnterAnimation(viewHolder);
			return false;
		}
		else {
			animated = super.animateAdd(viewHolder);
		}
		dispatchAddFinished(viewHolder);

		return animated;
	}

	private void runEnterAnimation(final RecyclerView.ViewHolder holder) {
		holder.itemView.setTranslationY(-holder.itemView.getHeight());
		holder.itemView.setTranslationZ(getTranslationYOffset(holder.getItemViewType()));

		ViewCompat.animate(holder.itemView)
			.setInterpolator(new OvershootInterpolator(1.5f))
			.setDuration(500L)
			.translationY(0)
			.start();
	}

	private int getTranslationYOffset(int type) {
		int height = 0;
		for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
			RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
			if (holder.getItemViewType() == LaunchDataItem.LOB_VIEW) {
				continue;
			}
			recyclerView.getLayoutManager().measureChild(holder.itemView, 0, 0);
			height += holder.itemView.getMeasuredHeight();
			if (holder.getItemViewType() == type) {
				return -height;
			}
		}

		return -height;
	}
}
