package com.expedia.bookings.widget;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXResultsListAdapter extends LoadingRecyclerViewAdapter {

	private static final String ROW_PICASSO_TAG = "lx_row";
	private static boolean userBucketedForRTRTest;

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder itemViewHolder = super.onCreateViewHolder(parent, viewType);
		if (itemViewHolder == null) {
			View itemView;
			if (userBucketedForRTRTest) {
				itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.section_lx_search_row_recommended_ab_test, parent, false);
				itemViewHolder = new RecommendedViewHolder(itemView);
			}
			else {
				itemView = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.section_lx_search_row, parent, false);
				itemViewHolder = new ViewHolder(itemView);
			}
		}
		return itemViewHolder;
	}

	@Override
	protected int loadingLayoutResourceId() {
		return R.layout.car_lx_loading_animation_widget;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		if (holder.getItemViewType() == getDATA_VIEW()) {
			LXActivity activity = (LXActivity) getItems().get(position);
			if (userBucketedForRTRTest) {
				((RecommendedViewHolder) holder).bindRecommendationScore(activity.recommendationScore);
			}
			((ViewHolder) holder).bind(activity);
		}
	}

	public void setUserBucketedForRTRTest(boolean userBucketedForRTRTest) {
		LXResultsListAdapter.userBucketedForRTRTest = userBucketedForRTRTest;
	}

	public static class RecommendedViewHolder extends ViewHolder implements View.OnClickListener {

		@InjectView(R.id.recommended_percentage)
		TextView recommendedScore;

		@InjectView(R.id.recommended_score_text)
		TextView recommendedScoreText;

		public RecommendedViewHolder(View itemView) {
			super(itemView);
		}

		public void bindRecommendationScore(int recommendationScore) {
			if (recommendationScore > 0) {
				recommendedScore.setVisibility(View.VISIBLE);
				recommendedScoreText.setVisibility(View.VISIBLE);
				recommendedScore.setText(LXDataUtils
					.getUserRecommendPercentString(itemView.getContext(), recommendationScore));
				recommendedScoreText.setText(itemView.getResources().getString(R.string.lx_customers_recommend));
			}
			else {
				recommendedScore.setVisibility(View.GONE);
				recommendedScoreText.setVisibility(View.GONE);
			}
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		public ViewHolder(View itemView) {
			super(itemView);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
			lxModTestEnabled = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidLXMOD);
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

		@InjectView(R.id.lx_card_top_gradient)
		View cardGradientOnTop;

		@InjectView(R.id.urgency_message_layout_lx)
		LinearLayout urgencyMessage;

		@InjectView(R.id.activity_recommendation_rating)
		TextView recommendationScoreView;

		@InjectView(R.id.activity_recommended_text)
		TextView recommendationTextView;

		@InjectView(R.id.activity_discount_percentage)
		TextView discountPercentageView;

		private boolean lxModTestEnabled;

		@Override
		public void onClick(View v) {
			LXActivity activity = (LXActivity) v.getTag();
			Events.post(new Events.LXActivitySelected(activity));
		}

		public void bind(LXActivity activity) {
			itemView.setTag(activity);
			urgencyMessage.setVisibility(View.GONE);
			if (activity.modPricingEnabled(lxModTestEnabled)) {
				if (activity.mipDiscountPercentage >= 5) {
					urgencyMessage.setVisibility(View.VISIBLE);
				}
				LXDataUtils.bindPriceAndTicketType(itemView.getContext(), activity.fromPriceTicketCode, activity.mipPrice,
						activity.mipOriginalPrice, activityPrice, fromPriceTicketType);
				LXDataUtils.bindOriginalPrice(itemView.getContext(), activity.mipOriginalPrice, activityOriginalPrice);
			}
			else {
				LXDataUtils.bindPriceAndTicketType(itemView.getContext(), activity.fromPriceTicketCode, activity.price,
						activity.originalPrice, activityPrice, fromPriceTicketType);
				LXDataUtils.bindOriginalPrice(itemView.getContext(), activity.originalPrice, activityOriginalPrice);
			}
			// Remove the extra margin that card view adds for pre-L devices.
			cardView.setPreventCornerOverlap(false);
			activityTitle.setText(activity.title);
			LXDataUtils.bindDuration(itemView.getContext(), activity.duration, activity.isMultiDuration, duration);

			if (FeatureToggleUtil.isFeatureEnabled(itemView.getContext(), R.string.preference_enable_lx_srp_redesign)) {
				LXDataUtils.bindRecommendation(itemView.getContext(), activity.recommendationScore, recommendationScoreView, recommendationTextView);
				LXDataUtils.bindDiscountPercentage(activity, discountPercentageView);
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
				cardGradientOnTop.setVisibility(userBucketedForRTRTest ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
				if (errorDrawable != null) {
					activityImage.setImageDrawable(errorDrawable);
					gradientMask.setVisibility(View.VISIBLE);
					cardGradientOnTop.setVisibility(View.GONE);
				}
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
				activityImage.setImageDrawable(placeHolderDrawable);
				gradientMask.setVisibility(View.GONE);
				cardGradientOnTop.setVisibility(View.GONE);
			}
		};
	}
}
