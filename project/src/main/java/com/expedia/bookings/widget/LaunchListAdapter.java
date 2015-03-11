package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Images;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class LaunchListAdapter extends RecyclerView.Adapter<LaunchListAdapter.ViewHolder> {
	private static final String PICASSO_TAG = "LAUNCH_LIST";
	private static final int HEADER_VIEW = 0;
	public static final int CARD_VIEW = 1;
	private static final String HEADER_TAG = "HEADER_TAG";
	private List<?> listData = new ArrayList<>();

	private ViewGroup parentView;
	private View headerView;
	private TextView seeAllButton;
	private TextView launchListTitle;

	public LaunchListAdapter(View header) {
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into NearbyHotelsListAdapter");
		}
		seeAllButton = ButterKnife.findById(headerView, R.id.see_all_hotels_button);
		launchListTitle = ButterKnife.findById(headerView, R.id.launch_list_header_title);
		FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM);
	}

	private static boolean isHeader(int position) {
		return position == 0;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == HEADER_VIEW) {
			return new ViewHolder(headerView);
		}
		parentView = parent;
		View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.section_launch_list_card, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
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

		if (listData.get(actualPosition).getClass() == Hotel.class) {
			Hotel hotel = (Hotel) listData.get(actualPosition);
			String url = Images.getNearbyHotelImage(hotel);
			new HotelMedia(url).fillImageView(holder.backgroundImage, parentView.getWidth(),
				R.drawable.bg_tablet_hotel_results_placeholder, null, PICASSO_TAG);
			holder.bindListData(hotel, fullWidthTile);
		}

		else if (listData.get(actualPosition).getClass() == CollectionLocation.class) {
			CollectionLocation location = (CollectionLocation) listData.get(actualPosition);
			final String url = Images.getResizedImageUrl(parentView.getContext(), location, layoutParams.width);

			new PicassoHelper.Builder(holder.backgroundImage)
				.fade()
				.setTag(PICASSO_TAG)
				.fit()
				.centerCrop()
				.build()
				.load(url);
			holder.bindListData(location, fullWidthTile);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return isHeader(position) ? HEADER_VIEW : CARD_VIEW;
	}

	@Override
	public int getItemCount() {
		return listData.size() + 1;
	}

	public void setListData(List<?> listData, String headerTitle) {
		this.listData = listData;
		Class clz = listData.get(0).getClass();
		launchListTitle.setText(headerTitle);
		if (clz == Hotel.class) {
			seeAllButton.setVisibility(View.VISIBLE);

		}
		else if (clz == CollectionLocation.class) {
			seeAllButton.setVisibility(View.GONE);
		}
	}

	private static final View.OnClickListener SEE_ALL_LISTENER = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Events.post(new Events.LaunchSeeAllButtonPressed(AnimUtils.createActivityScaleBundle(v)));
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
		@InjectView(R.id.price)
		public TextView price;

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
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindListData(Object data, boolean fullWidthTile) {
			Context context = itemView.getContext();
			itemView.setTag(data);
			cardView.setPreventCornerOverlap(false);

			if (fullWidthTile) {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
				price.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
			}
			else {
				title.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
				price.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
			}

			// Bind nearby hotel data
			if (data.getClass() == Hotel.class) {
				Hotel hotel = (Hotel) data;
				bindHotelData(hotel, context);
			}

			// Bind collection location data
			else if (data.getClass() == CollectionLocation.class) {
				CollectionLocation location = (CollectionLocation) data;
				bindLocationData(location);
			}
		}

		public void bindHotelData(Hotel hotel, Context context) {
			title.setText(hotel.name);
			subtitle.setText(HotelUtils.formatDistanceForNearby(context, hotel, true));
			price.setText(hotel.lowRateInfo.currencySymbol + Math.round(hotel.lowRateInfo.priceToShowUsers));

			if (HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)) {
				saleTextView.setText(context.getString(R.string.percent_off_TEMPLATE,
					HotelUtils.getDiscountPercent(hotel.lowRateInfo)));
				if (hotel.lowRateInfo.airAttached) {
					saleTextView.setBackgroundColor(orange);
				}
				else {
					saleTextView.setBackgroundColor(green);
				}
				saleTextView.setVisibility(View.VISIBLE);
			}
			else {
				saleTextView.setVisibility(View.GONE);
			}
			//TODO: resolve mobile exclusive string length localization issue
//			else if (HotelUtils.getDiscountPercent(offer.lowRateInfo) > 0 && offer.isDiscountRestrictedToCurrentSourceType) {
//				saleTextView.setText(R.string.mobile_exclusive);
//				saleTextView.setBackgroundColor(purple);
//				saleTextView.setVisibility(View.VISIBLE);
//			}
		}

		public void bindLocationData(CollectionLocation location) {
			title.setText(location.title);
			subtitle.setText(location.subtitle);
			price.setVisibility(View.GONE);
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
			}
			else if (view.getTag().getClass() == CollectionLocation.class) {
				Bundle animOptions = AnimUtils.createActivityScaleBundle(view);
				CollectionLocation location = (CollectionLocation) view.getTag();
				Events.post(new Events.LaunchCollectionItemSelected(location, animOptions));
			}
		}
	}
}
