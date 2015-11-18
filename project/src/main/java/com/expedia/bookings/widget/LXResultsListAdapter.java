package com.expedia.bookings.widget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXResultsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int LOADING_VIEW = 0;
	private static final int DATA_VIEW = 1;
	private static final String ROW_PICASSO_TAG = "lx_row";
	private List<LXActivity> activities = new ArrayList<>();
	private boolean isLoading = false;

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
		return isLoading ? LOADING_VIEW : DATA_VIEW;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() != LOADING_VIEW) {
			LXActivity activity = activities.get(position);
			((ViewHolder) holder).bind(activity);
		}
		else {
			ValueAnimator animation = AnimUtils.setupLoadingAnimation(((LoadingViewHolder) holder).backgroundImageView, position % 2 == 0);
			((LoadingViewHolder) holder).setAnimator(animation);
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder.getItemViewType() == LOADING_VIEW) {
			((LoadingViewHolder) holder).cancelAnimation();
		}
		super.onViewRecycled(holder);
	}


	@Override
	public int getItemCount() {
		return activities.size();
	}

	public void setDummyActivities(List<LXActivity> activities) {
		setActivities(activities, true);
	}

	public void setActivities(List<LXActivity> activities) {
		setActivities(activities, false);
	}

	private void setActivities(List<LXActivity> activities, boolean areDummyActivities) {
		this.isLoading = areDummyActivities;
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

		@InjectView(R.id.activity_price)
		TextView activityPrice;

		@InjectView(R.id.activity_original_price)
		TextView activityOriginalPrice;

		@InjectView(R.id.results_card_view)
		CardView cardView;

		@InjectView(R.id.activity_duration)
		TextView duration;

		@InjectView(R.id.gradient_mask)
		public View gradientMask;

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
			if (activity.fromPriceTicketCode != null) {
				fromPriceTicketType.setText(
					LXDataUtils.perTicketTypeDisplayLabel(itemView.getContext(), activity.fromPriceTicketCode));
				activityPrice.setText(activity.price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
			}
			else {
				fromPriceTicketType.setText("");
				activityPrice.setText("");
			}

			if (activity.originalPrice.getAmount().equals(BigDecimal.ZERO)) {
				activityOriginalPrice.setVisibility(View.GONE);
			}
			else {
				activityOriginalPrice.setVisibility(View.VISIBLE);
				String formattedOriginalPrice = activity.originalPrice.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP);
				activityOriginalPrice.setText(Html.fromHtml(
						itemView.getContext().getString(R.string.strike_template, formattedOriginalPrice),
						null,
						new StrikethroughTagHandler()));
			}

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
			else {
				duration.setText("");
				duration.setVisibility(View.GONE);
			}

			List<String> imageURLs = Images
				.getLXImageURLBasedOnWidth(activity.getImages(), AndroidUtils.getDisplaySize(itemView.getContext()).x);
			new PicassoHelper.Builder(itemView.getContext())
				.setPlaceholder(R.drawable.results_list_placeholder)
				.setError(R.drawable.itin_header_placeholder_activities)
				.fade()
				.setTag(ROW_PICASSO_TAG)
				.setTarget(target)
				.build()
				.load(imageURLs);

		}

		private PicassoTarget target = new PicassoTarget() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				super.onBitmapLoaded(bitmap, from);
				activityImage.setImageBitmap(bitmap);
				gradientMask.setVisibility(View.VISIBLE);
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
				if (errorDrawable != null) {
					activityImage.setImageDrawable(errorDrawable);
					gradientMask.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
				activityImage.setImageDrawable(placeHolderDrawable);
				gradientMask.setVisibility(View.GONE);
			}
		};

	}
}
