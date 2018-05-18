package com.expedia.bookings.widget;

import java.util.List;

import javax.inject.Inject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
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
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.expedia.bookings.utils.FeatureUtilKt.isActivityCountHeaderViewEnabled;

public class LXResultsListAdapter extends LoadingRecyclerViewAdapter {
	private static final String ROW_PICASSO_TAG = "lx_row";
	private static SparseIntArray scrollDepthMap;
	private static int activitiesListSize;
	private static String promoDiscountType;
	private static String activityDestination;

	public void setItems(List<LXActivity> items, String discountType, String destination) {
		setItems(items);
		promoDiscountType = discountType;
		activityDestination = destination;
	}

	@Override
	public int getItemCount() {
		return getItems().size() + adjustPosition();
	}

	private int adjustPosition() {
		if (isLoading()) {
			return 0;
		}
		else {
			return isActivityCountHeaderViewEnabled() ? 1 : 0;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder itemViewHolder = super.onCreateViewHolder(parent, viewType);
		if (itemViewHolder == null) {
			if (viewType == getACTIVITY_COUNT_HEADER_VIEW()) {
				itemViewHolder = new HeaderViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.lx_activity_count_header_cell, parent, false));
			}
			else if (viewType == getDATA_VIEW()) {
				itemViewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
					.inflate(R.layout.section_lx_search_row, parent, false));
			}
			else {
				throw new UnsupportedOperationException("Did not recognise the viewType");
			}
		}
		return itemViewHolder;
	}

	@Override
	public int getItemViewType(int position) {
		if (isLoading()) {
			return getLOADING_VIEW();
		}
		else if (isActivityCountHeaderViewEnabled() && position == 0) {
			return getACTIVITY_COUNT_HEADER_VIEW();
		}
		else {
			return getDATA_VIEW();
		}
	}

	@Override
	protected int loadingLayoutResourceId() {
		return R.layout.car_lx_loading_animation_widget;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		if (holder.getItemViewType() == getDATA_VIEW()) {
			LXActivity activity = (LXActivity) getItems().get(getDataViewPosition(position));
			((ViewHolder) holder).bind(activity);
		}
		else if (holder.getItemViewType() == getACTIVITY_COUNT_HEADER_VIEW()) {
			((HeaderViewHolder) holder).bindActivityCount(activityDestination);
		}
	}

	private int getDataViewPosition(int position) {
		return isActivityCountHeaderViewEnabled() ? position - 1 : position;
	}

	public void initializeScrollDepthMap(int activitiesListSize) {
		scrollDepthMap = new SparseIntArray();
		LXResultsListAdapter.activitiesListSize = activitiesListSize;
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(10, activitiesListSize), 10);
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(30, activitiesListSize), 30);
		scrollDepthMap.put(LXDataUtils.findScrolledPosition(100, activitiesListSize), 100);
	}

	public class HeaderViewHolder extends RecyclerView.ViewHolder {

		@InjectView(R.id.lx_activity_count_header)
		TextView activityCountText;

		private HeaderViewHolder(View itemView) {
			super(itemView);
			ButterKnife.inject(this, itemView);
		}

		private void bindActivityCount(String destination) {
			String displayCountText = Strings.isEmpty(destination) ?
				LXDataUtils.getActivityCountHeaderCurrentLocationString(itemView.getContext(), getItems().size()) :
				LXDataUtils.getActivityCountHeaderString(itemView.getContext(), getItems().size(), destination);
			activityCountText.setText(displayCountText);
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

			if (scrollDepthMap != null && scrollDepthMap.indexOfKey(getAdapterPosition() + 1) >= 0) {
				OmnitureTracking.trackLXSRPScrollDepth(scrollDepthMap.get(getAdapterPosition() + 1), activitiesListSize);
				scrollDepthMap.removeAt(getAdapterPosition() + 1);
			}
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
