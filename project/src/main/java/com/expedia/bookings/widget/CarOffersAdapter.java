package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarOffersAdapter extends RecyclerView.Adapter<CarOffersAdapter.ViewHolder> {

	private List<SearchCarOffer> offers = new ArrayList<>();
	private int mLastExpanded = 0;

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

	public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnMapReadyCallback {

		@InjectView(R.id.vendor)
		public TextView vendor;

		@InjectView(R.id.car_details)
		public TextView carDetails;

		@InjectView(R.id.address)
		public TextView address;

		@InjectView(R.id.category_price_text)
		public TextView ratePrice;

		@InjectView(R.id.map_view)
		public MapView mapView;

		@InjectView(R.id.reserve_now)
		public ToggleButton reserveNow;

		@InjectView(R.id.total_price_text)
		public TextView totalPrice;

		@InjectView(R.id.card_view)
		public CardView cardView;

		@InjectView(R.id.map_text)
		public TextView mapText;

		public Context mContext;

		public ViewHolder(View view) {
			super(view);
			mContext = view.getContext();
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindOffer(final SearchCarOffer offer) {
			itemView.setTag(offer);

			vendor.setText(offer.vendor.name);
			carDetails.setText(offer.vehicleInfo.makes.get(0));
			ratePrice.setText(
				ratePrice.getContext().getString(R.string.cars_daily_template, offer.fare.rate.getFormattedMoney()));
			totalPrice.setText(
				totalPrice.getContext().getString(R.string.cars_total_template, offer.fare.total.getFormattedMoney()));
			address.setText(offer.pickUpLocation.locationDescription);
			mapText.setText(offer.pickUpLocation.airportInstructions);

			reserveNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean isChecked = reserveNow.isChecked();
					if (!isChecked) {
						offer.isToggled = true;
						reserveNow.setChecked(true);
						Events.post(new Events.CarsKickOffCreateTrip(offer));
					} else {
						offer.isToggled = false;
						onItemExpanded(getPosition());
					}
				}
			});
			updateState(offer.isToggled);
		}

		private void updateState(boolean isChecked) {
			reserveNow.setChecked(isChecked);
			cardView.setCardBackgroundColor(isChecked ? Color.WHITE : Color.TRANSPARENT);
			mapView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			mapText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			cardView.setCardElevation(isChecked ? 5 : 0);
			if (isChecked) {
				mapView.onCreate(null);
				mapView.getMapAsync(this);
			}
		}

		@Override
		public void onClick(View view) {
			onItemExpanded(getPosition());
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			MapsInitializer.initialize(mContext);
			SearchCarOffer offer = (SearchCarOffer) itemView.getTag();
			addMarker(offer, googleMap);
			googleMap.getUiSettings().setMapToolbarEnabled(false);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(offer.pickUpLocation.latitude, offer.pickUpLocation.longitude),
				18));
		}

		public void addMarker(SearchCarOffer offer, GoogleMap googleMap) {
			MarkerOptions marker = new MarkerOptions();
			marker.position(new LatLng(offer.pickUpLocation.latitude, offer.pickUpLocation.longitude));
			marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.cars_pin));
			googleMap.addMarker(marker);
		}
	}

	public void setCarOffers(List<SearchCarOffer> offers) {
		this.offers = offers;
		if (offers.size() >= 1) {
			SearchCarOffer offer = offers.get(0);
			offer.isToggled = true;
		}
	}

	public void onItemExpanded(int index) {
		if (mLastExpanded != index) {
			offers.get(mLastExpanded).isToggled = false;
			offers.get(index).isToggled = true;
			mLastExpanded = index;
			notifyDataSetChanged();
		}
	}
}
