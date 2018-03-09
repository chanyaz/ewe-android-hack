package com.expedia.bookings.widget;

import java.util.HashMap;
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
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LXResultsListAdapter extends LoadingRecyclerViewAdapter {

	private static final String ROW_PICASSO_TAG = "lx_row";
	private static boolean userBucketedForRTRTest;
	private static HashMap<Integer, Integer> scrollDepthMap;
	private static int activitiesListSize;
	private static String promoDiscountType;

	public void setItems(List<LXActivity> items, String discountType) {
		setItems(items);
		promoDiscountType = discountType;
	}

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

	public void initializeScrollDepthMap(int activitiesListSize) {
		scrollDepthMap = new HashMap<>();
		this.activitiesListSize = activitiesListSize;
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(10, activitiesListSize), 10);
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(30, activitiesListSize), 30);
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(100, activitiesListSize), 100);
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
			Ui.getApplication(itemView.getContext()).lxComponent().inject(this);
			itemView.setOnClickListener(this);
			lxModTestEnabled = Constants.MOD_PROMO_TYPE.equals(lxState.getPromoDiscountType());
			lxMipTestEnabled = AbacusFeatureConfigManager.isBucketedForTest(itemView.getContext(), AbacusUtils.EBAndroidLXMIP);
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

		@InjectView(R.id.activity_vbp_lowest_price_text)
		TextView activityVbpLowestPriceText;

		@InjectView(R.id.mip_srp_tile_layout)
		LinearLayout mipSrpTileLayout;

		@InjectView(R.id.mip_srp_tile_image)
		ImageView mipSrpTileImage;

		@InjectView(R.id.mip_srp_tile_discount)
		TextView mipSrpTileDiscount;

		@Inject
		LXState lxState;

		private boolean lxModTestEnabled;

		private boolean lxMipTestEnabled;

		@Override
		public void onClick(View v) {
			LXActivity activity = (LXActivity) v.getTag();
			Events.post(new Events.LXActivitySelected(activity));
		}

		public void bind(LXActivity activity) {
			itemView.setTag(activity);
			urgencyMessage.setVisibility(View.GONE);
			if (activity.mipPricingEnabled(lxMipTestEnabled)) {
				LXDataUtils
					.bindPriceAndTicketType(itemView.getContext(), activity.fromPriceTicketCode, activity.mipPrice,
						activity.mipOriginalPrice, activityPrice, fromPriceTicketType);
				LXDataUtils.bindOriginalPrice(itemView.getContext(), activity.mipOriginalPrice, activityOriginalPrice);
			}
			else if (activity.modPricingEnabled(lxModTestEnabled)) {
				if (activity.mipDiscountPercentage >= Constants.LX_MIN_DISCOUNT_PERCENTAGE) {
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
			activityVbpLowestPriceText.setText(activity.vbpLowestPriceText);
			if (activity.vbpLowestPriceText == null) {
				activityVbpLowestPriceText.setVisibility(View.GONE);
			}
			else {
				activityVbpLowestPriceText.setVisibility(View.VISIBLE);
			}
			LXDataUtils.bindDuration(itemView.getContext(), activity.duration, activity.isMultiDuration, duration);

			LXDataUtils.bindRecommendation(itemView.getContext(), activity.recommendationScore, recommendationScoreView, recommendationTextView);
			LXDataUtils.bindDiscountPercentage(itemView.getContext(), activity, discountPercentageView, lxModTestEnabled);
			if (activity.mipPricingEnabled(lxMipTestEnabled) && promoDiscountType != null && activity.mipDiscountPercentage >= Constants.LX_MIN_DISCOUNT_PERCENTAGE ) {
				mipSrpTileLayout.setVisibility(View.VISIBLE);
				discountPercentageView.setVisibility(View.GONE);
				mipSrpTileDiscount.setText(Phrase.from(itemView.getContext(), R.string.mip_srp_tile_discount_TEMPLATE).put("discount", activity.mipDiscountPercentage).format().toString());
				if (promoDiscountType.equals(Constants.LX_AIR_HOTEL_MIP)) {
					mipSrpTileImage.setImageResource(R.drawable.mip_hotel_flight);
				}
				else if (promoDiscountType.equals(Constants.LX_AIR_MIP)) {
					mipSrpTileImage.setImageResource(R.drawable.mip_flight);
				}
				else if (promoDiscountType.equals(Constants.LX_HOTEL_MIP)) {
					mipSrpTileImage.setImageResource(R.drawable.mip_hotel);
				}
				else {
					mipSrpTileLayout.setVisibility(View.GONE);
				}
			}
			else {
				mipSrpTileLayout.setVisibility(View.GONE);
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

			if (scrollDepthMap != null && scrollDepthMap.get(getAdapterPosition() + 1) != null) {
				OmnitureTracking.trackLXSRPScrollDepth(scrollDepthMap.get(getAdapterPosition() + 1), activitiesListSize);
				scrollDepthMap.remove(getAdapterPosition() + 1);
			}
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
