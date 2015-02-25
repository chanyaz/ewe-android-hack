package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Images;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class NearbyHotelsListAdapter extends RecyclerView.Adapter<NearbyHotelsListAdapter.ViewHolder> {
	private static final String PICASSO_TAG = "NEARBY_HOTELS_LIST";
	private static final int HEADER_VIEW = 0;
	private static final int HOTEL_CARD_VIEW = 1;
	private static final String HEADER_TAG = "HEADER_TAG";
	private List<Hotel> nearbyHotels = new ArrayList<>();

	private ViewGroup parentView;
	private View headerView;

	public NearbyHotelsListAdapter(View header) {
		headerView = header;
		if (header == null) {
			throw new IllegalArgumentException("Don't pass a null View into NearbyHotelsListAdapter");
		}
		ButterKnife.findById(headerView, R.id.see_all_hotels_button).setOnClickListener(SEE_ALL_LISTENER);
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
			.inflate(R.layout.section_nearby_hotel_summary, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(NearbyHotelsListAdapter.ViewHolder holder, int position) {
		boolean fullWidthTile;

		if (isHeader(position)) {
			headerView.setTag(HEADER_TAG);
			StaggeredGridLayoutManager.LayoutParams headerParams = new StaggeredGridLayoutManager.LayoutParams(headerView.getWidth(), headerView.getHeight());
			headerParams.setFullSpan(true);
			headerView.setLayoutParams(headerParams);
			return;
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

		String url = Images.getNearbyHotelImage(nearbyHotels.get(actualPosition));
		new HotelMedia(url).fillImageView(holder.hotelBackgroundImage, parentView.getWidth(), R.drawable.bg_tablet_hotel_results_placeholder, null, PICASSO_TAG);
		holder.bindNearbyHotelOffers(nearbyHotels.get(actualPosition), fullWidthTile);

	}

	@Override
	public int getItemViewType(int position) {
		return isHeader(position) ? HEADER_VIEW : HOTEL_CARD_VIEW;
	}

	@Override
	public int getItemCount() {
		return nearbyHotels.size() + 1;
	}

	public void setNearbyHotels(List<Hotel> nearbyHotels) {
		this.nearbyHotels = nearbyHotels;
	}

	private static final View.OnClickListener SEE_ALL_LISTENER = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Events.post(new Events.LaunchSeeAllButtonPressed(AnimUtils.createActivityScaleBundle(v)));
		}
	};

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private static final int FULL_TILE_TEXT_SIZE = 19;
		private static final int HALF_TILE_TEXT_SIZE = 17;

		@Optional
		@InjectView(R.id.hotel_name)
		public TextView hotelName;

		@Optional
		@InjectView(R.id.hotel_proximity)
		public TextView hotelProximity;

		@Optional
		@InjectView(R.id.hotel_price)
		public TextView hotelPrice;

		@Optional
		@InjectView(R.id.hotel_background_image)
		public ImageView hotelBackgroundImage;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindNearbyHotelOffers(Hotel offer, boolean fullWidthTile) {
			itemView.setTag(offer);
			hotelName.setText(offer.name);
			hotelProximity.setText(HotelUtils.formatDistanceForNearby(itemView.getContext(), offer, true));
			hotelPrice.setText(offer.lowRateInfo.currencySymbol + Math.round(offer.lowRateInfo.priceToShowUsers));

			if (fullWidthTile) {
				hotelName.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
				hotelPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, FULL_TILE_TEXT_SIZE);
			}
			else {
				hotelName.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
				hotelPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, HALF_TILE_TEXT_SIZE);
			}
		}

		@Override
		public void onClick(View view) {
			if (view.getTag().equals(HEADER_TAG)) {
				return;
			}
			Hotel selectedOffer = (Hotel) view.getTag();
			Events.post(new Events.LaunchListItemSelected(selectedOffer));
		}
	}
}
