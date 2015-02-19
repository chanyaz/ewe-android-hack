package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.NearbyHotelOffer;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NearbyHotelsListAdapter extends RecyclerView.Adapter<NearbyHotelsListAdapter.ViewHolder> {
	private List<NearbyHotelOffer> nearbyHotels = new ArrayList<>();

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.section_nearby_hotel_summary, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(NearbyHotelsListAdapter.ViewHolder holder, int position) {
		StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
		if (position%3 == 0) {
			layoutParams.setFullSpan(true);
		}
		else {
			layoutParams.setFullSpan(false);
		}

		holder.bindNearbyHotelOffers(nearbyHotels.get(position));
	}

	@Override
	public int getItemCount() {
		return nearbyHotels.size();
	}

	public void setNearbyHotels(List<NearbyHotelOffer> nearbyHotels) {
		this.nearbyHotels = nearbyHotels;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		@InjectView(R.id.hotel_name)
		public TextView hotelName;

		@InjectView(R.id.hotel_proximity)
		public TextView hotelProximity;

		@InjectView(R.id.hotel_price)
		public TextView hotelPrice;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindNearbyHotelOffers(NearbyHotelOffer offer) {
			itemView.setTag(offer);
			hotelName.setText(offer.name);
			hotelProximity.setText(offer.proximityDistanceInMiles);
			hotelPrice.setText(offer.lowRateInfo.total);
		}

		@Override
		public void onClick(View view) {

		}
	}
}
