package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;

public class CarOffersAdapter extends RecyclerView.Adapter<CarOffersAdapter.ViewHolder> {

	private List<SearchCarOffer> offers = new ArrayList<>();

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_car_offer_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		SearchCarOffer offer = offers.get(position);
		holder.bindOffer(offer);
	}

	@Override
	public int getItemCount() {
		return offers.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		@InjectView(R.id.vendor)
		public TextView vendor;

		@InjectView(R.id.car_details)
		public TextView carDetails;

		@InjectView(R.id.address)
		public TextView address;

		@InjectView(R.id.category_price_text)
		public TextView ratePrice;

		@InjectView(R.id.total_price_text)
		public TextView totalPrice;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindOffer(SearchCarOffer offer) {
			itemView.setTag(offer);

			vendor.setText(offer.vendor.name);
			carDetails.setText(offer.vehicleInfo.makes.get(0));
			address.setText(offer.dropOffLocation.locationDescription);
			ratePrice.setText(offer.fare.rate.getFormattedMoney());
			totalPrice.setText(offer.fare.total.getFormattedMoney());
		}

		@Override
		public void onClick(View view) {
			SearchCarOffer offer = (SearchCarOffer) view.getTag();
			Events.post(new Events.CarsKickOffCreateTrip(offer));
		}
	}

	public void setCarOffers(List<SearchCarOffer> offers) {
		this.offers = offers;
	}
}
