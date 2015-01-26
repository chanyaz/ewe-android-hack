package com.expedia.bookings.widget;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.utils.Ui;

public class CarOffersAdapter extends RecyclerView.Adapter<CarOffersAdapter.ViewHolder> {

	List<CarOffer> carOffers;

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_car_offer_row, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		CarOffer offer = carOffers.get(position);
		holder.vendor.setText(offer.vendor.name);
		holder.carDetails.setText(offer.vehicleInfo.makes.get(0));
		holder.address.setText(offer.dropOffLocation.locationDescription);
		holder.ratePrice.setText(offer.fare.rate.getFormattedMoney());
		holder.totalPrice.setText(offer.fare.total.getFormattedMoney());
	}

	@Override
	public int getItemCount() {
		return carOffers.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private TextView vendor;
		private TextView carDetails;
		private TextView address;
		private TextView ratePrice;
		private TextView totalPrice;

		public ViewHolder(View view) {
			super(view);
			vendor = Ui.findView(view, R.id.vendor);
			carDetails = Ui.findView(view, R.id.car_details);
			address = Ui.findView(view, R.id.address);
			ratePrice = Ui.findView(view, R.id.category_price_text);
			totalPrice = Ui.findView(view, R.id.total_price_text);
		}
	}

	public void setCarOffers(List<CarOffer> carOffers) {
		this.carOffers = carOffers;
	}
}
