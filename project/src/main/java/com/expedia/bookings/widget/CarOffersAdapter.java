package com.expedia.bookings.widget;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.otto.Events;
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
		holder.bindOffer(offer);
	}

	@Override
	public int getItemCount() {
		return carOffers.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private View root;
		private TextView vendor;
		private TextView carDetails;
		private TextView address;
		private TextView ratePrice;
		private TextView totalPrice;

		public ViewHolder(View view) {
			super(view);
			root = view;
			root.setOnClickListener(this);
			vendor = Ui.findView(view, R.id.vendor);
			carDetails = Ui.findView(view, R.id.car_details);
			address = Ui.findView(view, R.id.address);
			ratePrice = Ui.findView(view, R.id.category_price_text);
			totalPrice = Ui.findView(view, R.id.total_price_text);
		}

		public void bindOffer(CarOffer co) {
			root.setTag(co);
			vendor.setText(co.vendor.name);
			carDetails.setText(co.vehicleInfo.makes.get(0));
			address.setText(co.dropOffLocation.locationDescription);
			ratePrice.setText(co.fare.rate.getFormattedMoney());
			totalPrice.setText(co.fare.total.getFormattedMoney());
		}

		@Override
		public void onClick(View v) {
			CarOffer offer = (CarOffer) v.getTag();
			Events.post(new Events.CarsShowCheckout(offer));
		}
	}

	public void setCarOffers(List<CarOffer> carOffers) {
		this.carOffers = carOffers;
	}
}
