package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
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

import com.expedia.bookings.R;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Images;
import com.mobiata.android.text.StrikethroughTagHandler;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class LaunchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";
	private static final int HEADER_VIEW = 0;
	public static final int CARD_VIEW = 1;
	public static final int LOADING_VIEW = 2;
	private static final String HEADER_TAG = "HEADER_TAG";
	private List<?> listData = new ArrayList<>();
	private ArrayList<ValueAnimator> animations = new ArrayList<ValueAnimator>();

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
			return new ViewHolder(headerView);
		}
		parentView = parent;

		if (loadingState) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.launch_tile_loading_widget, parent, false);
			return new LoadingViewHolder(view);
		}

		else {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_launch_list_card, parent, false);
			return new ViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		boolean fullWidthTile;

		if (isHeader(position)) {
			headerView.setTag(HEADER_TAG);
			StaggeredGridLayoutManager.LayoutParams headerParams = new StaggeredGridLayoutManager.LayoutParams(headerView.getWidth(), headerView.getHeight());
			headerParams.setFullSpan(true);
			headerView.setLayoutParams(headerParams);
			return;
		}

		if (listData.get(0).getClass() == Hotel.class) {
			headerView.setOnClickListener(SEE_ALL_LISTENER);
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
			setupLoadingAnimation(((LoadingViewHolder) holder).backgroundImageView);
			LoadingViewHolder.index++;
		}

		else if (listData.get(actualPosition).getClass() == Hotel.class) {
			Hotel hotel = (Hotel) listData.get(actualPosition);

			final String url = Images.getNearbyHotelImage(hotel);
			HeaderBitmapDrawable drawable = Images.makeHotelBitmapDrawable(parentView.getContext(), width, url,
				PICASSO_TAG);
			((ViewHolder) holder).backgroundImage.setImageDrawable(drawable);

			((ViewHolder) holder).bindListData(hotel, fullWidthTile);
		}

		else if (listData.get(actualPosition).getClass() == CollectionLocation.class) {
			CollectionLocation location = (CollectionLocation) listData.get(actualPosition);

			final String url = Images.getCollectionImageUrl(location, width);
			HeaderBitmapDrawable drawable = Images.makeCollectionBitmapDrawable(parentView.getContext(), url, PICASSO_TAG);
			((ViewHolder) holder).backgroundImage.setImageDrawable(drawable);

			((ViewHolder) holder).bindListData(location, fullWidthTile);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return isHeader(position) ? HEADER_VIEW : loadingState ? LOADING_VIEW : CARD_VIEW;
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

	public void setupLoadingAnimation(View v) {
		int loadingColorLight = Color.parseColor("#D3D4D4");
		int loadingColorDark = Color.parseColor("#848F94");
		switch (LoadingViewHolder.index % 10) {
		case 0:
		case 3:
		case 4:
		case 6:
		case 7:
			animateBackground(v, loadingColorDark, loadingColorLight);
			break;
		default:
			animateBackground(v, loadingColorLight, loadingColorDark);
			break;
		}
	}

	private void animateBackground(final View view, int startColor, int endColor) {
		ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				view.setBackgroundColor((Integer) animator.getAnimatedValue());
			}
		});
		animation.setRepeatMode(ValueAnimator.REVERSE);
		animation.setRepeatCount(ValueAnimator.INFINITE);
		animation.setDuration(600);
		animation.start();
		animations.add(animation);
	}

	public void cleanup() {
		for (ValueAnimator animation : animations) {
			animation.cancel();
		}
		animations.clear();
	}

	private static final View.OnClickListener SEE_ALL_LISTENER = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Events.post(new Events.LaunchSeeAllButtonPressed(AnimUtils.createActivityScaleBundle(v)));
			OmnitureTracking.trackNewLaunchScreenSeeAllClick(v.getContext());
		}
	};

	/**
	 * A Viewholder for the case where our data are hotels.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private static final int FULL_TILE_TEXT_SIZE = 18;
		private static final int HALF_TILE_TEXT_SIZE = 15;
		private final int green;
		private final int orange;
		private final int purple;
		private final int blue;
		private Drawable mobileOnly;
		private Drawable tonightOnly;

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

		public ViewHolder(View view) {
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
				fullTilePrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
			}
			else {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
				fullTilePriceContainer.setVisibility(View.GONE);
				halfTilePriceContainer.setVisibility(View.VISIBLE);
				halfTilePrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
			}

			// Bind nearby hotel data
			if (data.getClass() == Hotel.class) {
				Hotel hotel = (Hotel) data;
				bindHotelData(hotel, context, fullWidthTile);
			}

			// Bind collection location data
			else if (data.getClass() == CollectionLocation.class) {
				CollectionLocation location = (CollectionLocation) data;
				bindLocationData(location);
			}
		}

		public void bindHotelData(Hotel hotel, Context context, boolean fullWidth) {
			title.setText(hotel.name);
			subtitle.setVisibility(View.GONE);
			ratingInfo.setVisibility(View.VISIBLE);

			rating.setText(Float.toString(hotel.hotelGuestRating));
			if (fullWidth) {
				if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
					fullTileStrikethroughPrice.setVisibility(View.VISIBLE);
					fullTileStrikethroughPrice.setText(Html.fromHtml(context.getString(R.string.strike_template,
							hotel.lowRateInfo.currencySymbol + Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers)),
						null,
						new StrikethroughTagHandler()));
				}
				else {
					fullTileStrikethroughPrice.setVisibility(View.GONE);
				}
				fullTilePrice.setText(hotel.lowRateInfo.currencySymbol + Math.round(hotel.lowRateInfo.priceToShowUsers));
				ratingText.setVisibility(View.VISIBLE);
			}
			else {
				if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
					halfTileStrikethroughPrice.setVisibility(View.VISIBLE);
					halfTileStrikethroughPrice.setText(Html.fromHtml(context.getString(R.string.strike_template,
							hotel.lowRateInfo.currencySymbol + Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers)),
						null,
						new StrikethroughTagHandler()));
				}
				else {
					halfTileStrikethroughPrice.setVisibility(View.GONE);
				}
				halfTilePrice.setText(hotel.lowRateInfo.currencySymbol + Math.round(hotel.lowRateInfo.priceToShowUsers));
				ratingText.setVisibility(View.GONE);
			}
			setHotelDiscountBanner(hotel, context, fullWidth);
		}

		// Set appropriate discount and / or DRR message
		public void setHotelDiscountBanner(Hotel hotel, Context context, boolean fullWidth) {
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

		public void bindLocationData(CollectionLocation location) {
			title.setText(location.title);
			FontCache.setTypeface(title, FontCache.Font.ROBOTO_MEDIUM);
			subtitle.setText(location.subtitle);
			ratingInfo.setVisibility(View.GONE);
			fullTilePriceContainer.setVisibility(View.GONE);
			halfTilePriceContainer.setVisibility(View.GONE);
			saleTextView.setVisibility(View.GONE);
		}

		@Override
		public void onClick(View view) {
			if (view.getTag().equals(HEADER_TAG)) {
				return;
			}

			if (view.getTag().getClass() == Hotel.class) {
				Hotel selectedHotel = (Hotel) view.getTag();
				Events.post(new Events.LaunchListItemSelected(selectedHotel));
				OmnitureTracking.trackNewLaunchScreenTileClick(view.getContext(), false);
			}
			else if (view.getTag().getClass() == CollectionLocation.class) {
				Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
				CollectionLocation location = (CollectionLocation) view.getTag();
				Events.post(new Events.LaunchCollectionItemSelected(location, animOptions));
				OmnitureTracking.trackNewLaunchScreenTileClick(view.getContext(), true);
			}
		}
	}

	public static class LoadingViewHolder extends RecyclerView.ViewHolder {
		private static int index = 0;

		@InjectView(R.id.background_image_view)
		public ImageView backgroundImageView;

		public LoadingViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
		}

	}
}
