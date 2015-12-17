package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";
	private static final int HEADER_VIEW = 0;
	public static final int HOTEL_VIEW = 1;
	public static final int COLLECTION_VIEW = 2;
	public static final int LOADING_VIEW = 3;
	private List<?> listData = new ArrayList<>();

	private ViewGroup parentView;
	private View headerView;
	private TextView seeAllButton;
	private TextView launchListTitle;

	public static boolean loadingState = false;

	public LaunchListAdapter(View header) {
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into LaunchListAdapter");
		}
		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
	}

	private static boolean isHeader(int position) {
		return position == 0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == HEADER_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.launch_header_root, parent, false);
			FrameLayout layout = (FrameLayout) view.findViewById(R.id.parent_layout);
			layout.addView(headerView);
			return new HeaderViewHolder(view);
		}
		parentView = parent;

		if (viewType == LOADING_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LoadingViewHolder(view);
		}
		else if (viewType == HOTEL_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new HotelViewHolder(view);
		}
		else {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_collection_list_card, parent, false);
			return new CollectionViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		boolean fullWidthTile;

		if (holder.getItemViewType() == HEADER_VIEW) {
			StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
			layoutParams.setFullSpan(true);
			return;
		}

		if (listData.get(0).getClass() == Hotel.class) {
			headerView.setOnClickListener(SEE_ALL_LISTENER);
		}
		else if (BuildConfig.DEBUG && Db.getMemoryTestActive()) {
			headerView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Events.post(new Events.MemoryTestImpetus());
				}
			});
		}

		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
		int actualPosition = position - 1;
		if (actualPosition % 5 == 0) {
			layoutParams.setFullSpan(true);
			fullWidthTile = true;
		}
		else {
			fullWidthTile = false;
			layoutParams.setFullSpan(false);
		}

		int width = fullWidthTile ? parentView.getWidth() : parentView.getWidth()/2;

		if (holder.getItemViewType() == LOADING_VIEW) {
			((LoadingViewHolder) holder).bind();
		}
		else if (holder.getItemViewType() == HOTEL_VIEW) {
			Hotel hotel = (Hotel) listData.get(actualPosition);

			final String url = Images.getNearbyHotelImage(hotel);
			HeaderBitmapDrawable drawable = Images.makeHotelBitmapDrawable(parentView.getContext(), (HotelViewHolder) holder, width, url,
				PICASSO_TAG);
			((HotelViewHolder) holder).backgroundImage.setImageDrawable(drawable);

			((HotelViewHolder) holder).bindListData(hotel, fullWidthTile);
		}
		else if (holder.getItemViewType() == COLLECTION_VIEW) {
			CollectionLocation location = (CollectionLocation) listData.get(actualPosition);

			final String url = Images.getCollectionImageUrl(location, width);
			HeaderBitmapDrawable drawable = Images.makeCollectionBitmapDrawable(parentView.getContext(), (CollectionViewHolder) holder, url, PICASSO_TAG);
			((CollectionViewHolder) holder).backgroundImage.setImageDrawable(drawable);

			((CollectionViewHolder) holder).bindListData(location, fullWidthTile);
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
	public int getItemViewType(int position) {
		if (isHeader(position)) {
			return HEADER_VIEW;
		}
		else if (loadingState) {
			return LOADING_VIEW;
		}
		else if (listData.get(position - 1).getClass() == CollectionLocation.class) {
			return COLLECTION_VIEW;
		}
		else {
			return HOTEL_VIEW;
		}
	}

	@Override
	public int getItemCount() {
		return listData.size() + 1;
	}

	public void setListData(List<?> listData, String headerTitle) {

		Class clz = listData.get(0).getClass();
		launchListTitle.setText(headerTitle);
		if (clz == Integer.class) {
			seeAllButton.setVisibility(View.GONE);
			loadingState = true;
		}
		else if (clz == Hotel.class) {
			seeAllButton.setVisibility(View.VISIBLE);
			loadingState = false;
		}
		else if (clz == CollectionLocation.class) {
			seeAllButton.setVisibility(View.GONE);
			loadingState = false;
		}

		this.listData = listData;
	}

	private static final View.OnClickListener SEE_ALL_LISTENER = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Events.post(new Events.LaunchSeeAllButtonPressed(AnimUtils.createActivityScaleBundle(v)));
			OmnitureTracking.trackNewLaunchScreenSeeAllClick();
		}
	};

	/**
	 * A Viewholder for the case where our data are hotels.
	 */
	public static class HotelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, HeaderBitmapDrawable.CallbackListener {
		private static final int FULL_TILE_TEXT_SIZE = 18;
		private static final int HALF_TILE_TEXT_SIZE = 15;
		private final int green;
		private final int orange;
		private final int purple;
		private final int blue;
		private Drawable mobileOnly;
		private Drawable tonightOnly;

		@Optional
		@InjectView(R.id.gradient)
		public View gradient;

		@Optional
		@InjectView(R.id.card_view)
		public CardView cardView;

		@Optional
		@InjectView(R.id.title)
		public TextView title;

		@Optional
		@InjectView(R.id.subtitle)
		public TextView subtitle;

		@Optional
		@InjectView(R.id.rating_info)
		public View ratingInfo;

		@Optional
		@InjectView(R.id.rating)
		public TextView rating;

		@Optional
		@InjectView(R.id.rating_text)
		public TextView ratingText;

		@Optional
		@InjectView(R.id.full_tile_price_container)
		public View fullTilePriceContainer;

		@Optional
		@InjectView(R.id.full_tile_strikethrough_price)
		public TextView fullTileStrikethroughPrice;

		@Optional
		@InjectView(R.id.full_tile_price)
		public TextView fullTilePrice;

		@Optional
		@InjectView(R.id.half_tile_price_container)
		public View halfTilePriceContainer;

		@Optional
		@InjectView(R.id.half_tile_strikethrough_price)
		public TextView halfTileStrikethroughPrice;

		@Optional
		@InjectView(R.id.half_tile_price)
		public TextView halfTilePrice;

		@Optional
		@InjectView(R.id.background_image)
		public ImageView backgroundImage;

		@Optional
		@InjectView(R.id.launch_tile_upsell_text)
		public TextView saleTextView;

		@Optional
		@InjectView(R.id.no_rating_text)
		public TextView noRatingText;

		public HotelViewHolder(View view) {
			super(view);
			green = view.getResources().getColor(R.color.launch_discount);
			orange = view.getResources().getColor(R.color.launch_air_attach);
			purple = view.getResources().getColor(R.color.launch_mobile_exclusive);
			blue = view.getResources().getColor(R.color.launch_tonight_only);
			mobileOnly = view.getResources().getDrawable(R.drawable.ic_mobile_only);
			tonightOnly = view.getResources().getDrawable(R.drawable.ic_tonight_only);

			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindListData(Object data, boolean fullWidthTile) {
			Context context = itemView.getContext();
			itemView.setTag(data);
			cardView.setPreventCornerOverlap(false);

			if (fullWidthTile) {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
				fullTilePriceContainer.setVisibility(View.VISIBLE);
				halfTilePriceContainer.setVisibility(View.GONE);
			}
			else {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
				fullTilePriceContainer.setVisibility(View.GONE);
				halfTilePriceContainer.setVisibility(View.VISIBLE);
			}

			Hotel hotel = (Hotel) data;
			bindHotelData(hotel, context, fullWidthTile);

		}

		private void bindHotelData(Hotel hotel, Context context, boolean fullWidth) {
			title.setText(hotel.localizedName);
			subtitle.setVisibility(View.GONE);
			ratingInfo.setVisibility(View.VISIBLE);
			noRatingText.setVisibility(View.GONE);

			if (fullWidth) {
				if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
					fullTileStrikethroughPrice.setVisibility(View.VISIBLE);
					fullTileStrikethroughPrice.setText(Html.fromHtml(context.getString(R.string.strike_template,
								StrUtils.formatHotelPrice(new Money(String.valueOf(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers)), hotel.lowRateInfo.currencyCode))),
						null,
						new StrikethroughTagHandler()));
				}
				else {
					fullTileStrikethroughPrice.setVisibility(View.GONE);
				}
				fullTilePrice.setText(StrUtils.formatHotelPrice(new Money(String.valueOf(Math.round(hotel.lowRateInfo.priceToShowUsers)), hotel.lowRateInfo.currencyCode)));
				if (hotel.hotelGuestRating == 0) {
					ratingInfo.setVisibility(View.GONE);
					noRatingText.setVisibility(View.VISIBLE);
				}
				else {
					rating.setText(Float.toString(hotel.hotelGuestRating));
					ratingText.setVisibility(View.VISIBLE);
				}
			}
			else {
				if (PointOfSale.getPointOfSale().supportsStrikethroughPrice() && HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
					halfTileStrikethroughPrice.setVisibility(View.VISIBLE);
					halfTileStrikethroughPrice.setText(Html.fromHtml(context.getString(R.string.strike_template,
						StrUtils.formatHotelPrice(new Money(String.valueOf(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers)), hotel.rateCurrencyCode))),
						null,
						new StrikethroughTagHandler()));
				}
				else {
					halfTileStrikethroughPrice.setVisibility(View.GONE);
				}
				halfTilePrice.setText(StrUtils.formatHotelPrice(new Money(String.valueOf(Math.round(hotel.lowRateInfo.priceToShowUsers)), hotel.lowRateInfo.currencyCode)));
				if (hotel.hotelGuestRating == 0) {
					ratingInfo.setVisibility(View.INVISIBLE);
				}
				else {
					rating.setText(Float.toString(hotel.hotelGuestRating));
				}
				ratingText.setVisibility(View.GONE);
			}
			setHotelDiscountBanner(hotel, context, fullWidth);
		}

		// Set appropriate discount and / or DRR message
		private void setHotelDiscountBanner(Hotel hotel, Context context, boolean fullWidth) {
			if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
				saleTextView.setVisibility(View.VISIBLE);
				// Mobile exclusive case
				if (hotel.isDiscountRestrictedToCurrentSourceType) {
					saleTextView.setBackgroundColor(purple);
					saleTextView.setCompoundDrawablesWithIntrinsicBounds(mobileOnly, null, null, null);
					if (fullWidth) {
						saleTextView.setText(R.string.launch_mobile_exclusive);
					}
					else {
						saleTextView.setText(context.getString(R.string.percent_off_TEMPLATE,
							HotelUtils.getDiscountPercent(hotel.lowRateInfo)));
					}
				}
				// Tonight only case
				else if (hotel.isSameDayDRR) {
					saleTextView.setBackgroundColor(blue);
					saleTextView.setCompoundDrawablesWithIntrinsicBounds(tonightOnly, null, null, null);
					if (fullWidth) {
						saleTextView.setText(R.string.launch_tonight_only);
					}
					else {
						saleTextView.setText(context.getString(R.string.percent_off_TEMPLATE,
							HotelUtils.getDiscountPercent(hotel.lowRateInfo)));
					}
				}
				// Default discount case
				else {
					saleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
					saleTextView.setText(context.getString(R.string.percent_off_TEMPLATE,
						HotelUtils.getDiscountPercent(hotel.lowRateInfo)));
					if (hotel.lowRateInfo.airAttached) {
						saleTextView.setBackgroundColor(orange);
					}
					else {
						saleTextView.setBackgroundColor(green);
					}
				}
			}
			else {
				saleTextView.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClick(View view) {
			Hotel selectedHotel = (Hotel) view.getTag();
			Events.post(new Events.LaunchListItemSelected(selectedHotel));
			OmnitureTracking.trackNewLaunchScreenTileClick(false);
		}

		@Override
		public void onBitmapLoaded() {
			gradient.setVisibility(View.VISIBLE);
		}

		@Override
		public void onBitmapFailed() {
			gradient.setVisibility(View.GONE);
		}

		@Override
		public void onPrepareLoad() {
			gradient.setVisibility(View.GONE);
		}
	}

	/**
	 * A Viewholder for the case where our data are launch collections.
	 */
	public static class CollectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, HeaderBitmapDrawable.CallbackListener {
		private static final int FULL_TILE_TEXT_SIZE = 18;
		private static final int HALF_TILE_TEXT_SIZE = 15;

		@Optional
		@InjectView(R.id.card_view)
		public CardView cardView;

		@Optional
		@InjectView(R.id.title)
		public TextView title;

		@Optional
		@InjectView(R.id.subtitle)
		public TextView subtitle;

		@Optional
		@InjectView(R.id.background_image)
		public ImageView backgroundImage;

		@Optional
		@InjectView(R.id.gradient)
		public View gradient;

		public CollectionViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindListData(Object data, boolean fullWidthTile) {
			itemView.setTag(data);
			cardView.setPreventCornerOverlap(false);

			if (fullWidthTile) {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
			}
			else {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
			}

			CollectionLocation location = (CollectionLocation) data;
			bindLocationData(location);
		}

		private void bindLocationData(CollectionLocation location) {
			title.setText(location.title);
			FontCache.setTypeface(title, FontCache.Font.ROBOTO_MEDIUM);
			subtitle.setText(location.subtitle);
			subtitle.setVisibility(View.VISIBLE);
		}

		@Override
		public void onClick(View view) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
			CollectionLocation location = (CollectionLocation) view.getTag();
			Events.post(new Events.LaunchCollectionItemSelected(location, animOptions));
			OmnitureTracking.trackNewLaunchScreenTileClick(true);
		}

		@Override
		public void onBitmapLoaded() {
			gradient.setVisibility(View.VISIBLE);
		}

		@Override
		public void onBitmapFailed() {
			gradient.setVisibility(View.GONE);
		}

		@Override
		public void onPrepareLoad() {
			gradient.setVisibility(View.GONE);
		}
	}

	/**
	 * A Viewholder for the list header
	 */
	public static class HeaderViewHolder extends RecyclerView.ViewHolder {
		public HeaderViewHolder(View view) {
			super(view);
		}
	}

	public static class LoadingViewHolder extends RecyclerView.ViewHolder {
		@InjectView(R.id.background_image_view)
		public View backgroundImageView;

		private ValueAnimator animation;

		public LoadingViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
		}

		/**
		 Loading animation that alternates between rows

		 | |____*____| |
		 | |_ _| |_ _| |
		 | |_*_| |_*_| |
		 | |____ ____| |
		 | |_*_| |_*_| |
		 | |_ _| |_ _| |
		 | etc etc etc |

		 **/
		public void bind() {
			switch (getAdapterPosition() % 10) {
			case 0:
			case 3:
			case 4:
			case 6:
			case 7:
				animation = AnimUtils.setupLoadingAnimation(backgroundImageView, true);
				break;
			default:
				animation = AnimUtils.setupLoadingAnimation(backgroundImageView, false);
				break;
			}
		}

		public void cancelAnimation() {
			if (animation != null) {
				animation.removeAllUpdateListeners();
				animation.cancel();
			}
		}
	}
}
