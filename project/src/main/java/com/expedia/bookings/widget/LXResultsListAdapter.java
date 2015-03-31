package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXResultsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int LOADING_VIEW = 0;
	private static final int DATA_VIEW = 1;
	private static final String ROW_PICASSO_TAG = "lx_row";
	private ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();
	private List<LXActivity> activities = new ArrayList<>();
	public static boolean loadingState = false;

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == LOADING_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.car_lx_loading_animation_widget, parent, false);
			return new LoadingViewHolder(view);
		}
		else {
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_lx_search_row, parent, false);
			return new ViewHolder(itemView);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return loadingState ? LOADING_VIEW : DATA_VIEW;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() != LOADING_VIEW) {
			LXActivity activity = activities.get(position);
			((ViewHolder) holder).bind(activity);

			String url = Images.getLXImageURL(activity.imageUrl);

			new PicassoHelper.Builder(((ViewHolder) holder).activityImage)
				.setPlaceholder(R.drawable.lx_placeholder)
				.fade()
				.setTag(ROW_PICASSO_TAG)
				.build()
				.load(url);
		}
		else {
			ValueAnimator animation = AnimUtils
				.setupLoadingAnimation(((LoadingViewHolder) holder).backgroundImageView, LoadingViewHolder.index);
			mAnimations.add(animation);
			LoadingViewHolder.index++;
		}
	}

	public void cleanup() {
		for (ValueAnimator animation : mAnimations) {
			animation.cancel();
		}
		mAnimations.clear();
	}

	@Override
	public int getItemCount() {
		return activities.size();
	}

	public void setActivities(List<LXActivity> activities) {
		this.activities = activities;
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		@InjectView(R.id.activity_title)
		TextView activityTitle;

		@InjectView(R.id.activity_image)
		ImageView activityImage;

		@InjectView(R.id.activity_from_price_ticket_type)
		TextView fromPriceTicketType;

		@InjectView(R.id.activity_category)
		TextView category;

		@InjectView(R.id.activity_price)
		TextView activityPrice;

		@InjectView(R.id.results_card_view)
		CardView cardView;

		@InjectView(R.id.activity_duration)
		TextView duration;

		@Override
		public void onClick(View v) {
			LXActivity activity = (LXActivity) v.getTag();
			Events.post(new Events.LXActivitySelected(activity));
		}

		public void bind(LXActivity activity) {
			itemView.setTag(activity);
			// Remove the extra margin that card view adds for pre-L devices.
			cardView.setPreventCornerOverlap(false);
			activityTitle.setText(activity.title);
			activityPrice.setText(activity.fromPrice);
			category.setText(activity.bestApplicableCategoryLocalized);
			fromPriceTicketType.setText(
				itemView.getContext().getString(LXDataUtils.LX_PER_TICKET_TYPE_MAP.get(activity.fromPriceTicketCode)));

			String activityDuration = activity.duration;
			if (Strings.isNotEmpty(activityDuration)) {
				if (activity.isMultiDuration) {
					duration.setText(itemView.getResources()
						.getString(R.string.search_result_multiple_duration_TEMPLATE, activityDuration));
				}
				else {
					duration.setText(activityDuration);
				}
				duration.setVisibility(View.VISIBLE);
			}
		}
	}
}
