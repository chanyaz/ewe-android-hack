package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Ui;
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
	// Design stuff
	int sideExpanded = 0;
	int topExpanded = 0;
	int sideCollapsed = 0;
	int topCollapsed = 0;
	int bottomCollapsed = 0;
	int paddingExpanded = 0;
	int toggleCollapsed = 0;
	int toggleExpanded = 0;
	int bottomExpanded = 0;
	int reserveExpanded = 0;


	private List<SearchCarOffer> offers = new ArrayList<>();
	private int mLastExpanded = 0;
	private static final int NONE_EXPANDED = -1;
	private static final float MAP_ZOOM_LEVEL = 12;

	public CarOffersAdapter(Context context) {
		sideExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());
		topExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());
		sideCollapsed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
		topCollapsed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, context.getResources().getDisplayMetrics());
		bottomCollapsed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
		paddingExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
		toggleCollapsed = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
		toggleExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
		bottomExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, context.getResources().getDisplayMetrics());
		reserveExpanded = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
	}

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

		@InjectView(R.id.passengers)
		public TextView passengers;

		@InjectView(R.id.bags)
		public TextView bags;

		@InjectView(R.id.doors)
		public TextView doors;

		@InjectView(R.id.transmission)
		public TextView transmission;

		@InjectView(R.id.address)
		public TextView address;

		@InjectView(R.id.category_price_text)
		public TextView ratePrice;

		@InjectView(R.id.map_click_container)
		public FrameLayout mapClickContainer;

		@InjectView(R.id.map_view)
		public MapView mapView;

		@InjectView(R.id.reserve_now)
		public ToggleButton reserveNow;

		@InjectView(R.id.total_price_text)
		public TextView totalPrice;

		@InjectView(R.id.root)
		public LinearLayout root;

		@InjectView(R.id.map_text)
		public TextView mapText;

		@InjectView(R.id.collapsed_container)
		public RelativeLayout collapsedContainer;

		@InjectView(R.id.main_container)
		public android.widget.FrameLayout mainContainer;

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
			carDetails.setText(CarDataUtils.getMakeName(mContext, offer.vehicleInfo.makes));
			CarInfo vehicleInfo = offer.vehicleInfo;
			passengers.setText(mContext.getString(R.string.car_details_TEMPLATE,
				String.valueOf(offer.vehicleInfo.adultCapacity),
				mContext.getString(R.string.passengers_label)));
			bags.setText(mContext.getString(R.string.car_details_TEMPLATE,
				String.valueOf(vehicleInfo.largeLuggageCapacity + vehicleInfo.smallLuggageCapacity),
				mContext.getString(R.string.car_bags_text)));
			doors.setText(
				mContext.getString(R.string.car_details_TEMPLATE,
					vehicleInfo.minDoors != vehicleInfo.maxDoors ? mContext
						.getString(R.string.car_door_range_TEMPLATE, vehicleInfo.minDoors, vehicleInfo.maxDoors)
						: String.valueOf(vehicleInfo.maxDoors),
					mContext.getString(R.string.car_doors_text)));
			transmission.setText(CarDataUtils.getStringForTransmission(mContext, vehicleInfo.transmission));

			ratePrice.setText(
				mContext.getString(R.string.car_details_TEMPLATE,
					CarDataUtils.getStringTemplateForRateTerm(mContext, offer.fare.rateTerm),
					offer.fare.rate.getFormattedMoney()));
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
						Events.post(new Events.CarsShowCheckout(offer.productKey, offer.fare.total, offer.isInsuranceIncluded, new LatLng(offer.pickUpLocation.latitude, offer.pickUpLocation.longitude)));
					}
					else {
						offer.isToggled = false;
						onItemExpanded(getPosition());
						OmnitureTracking.trackAppCarViewDetails(mContext);
					}
				}
			});
			updateState(offer);
		}

		private void updateState(SearchCarOffer offer) {
			boolean isChecked = offer.isToggled;
			mainContainer.setClickable(!isChecked);
			reserveNow.setChecked(isChecked);

			Ui.setViewBackground(root, isChecked ? mContext.getResources().getDrawable(R.drawable.card_background) : null);

			ratePrice.setTextColor(isChecked ? mContext.getResources().getColor(R.color.cars_primary_color)
				: mContext.getResources().getColor(R.color.cars_checkout_text_color));

			collapsedContainer.setPadding(isChecked ? sideExpanded : sideCollapsed,
				isChecked ? topExpanded : topCollapsed, isChecked ? sideExpanded : sideCollapsed,
				isChecked ? bottomExpanded : bottomCollapsed);
			address.setPadding(0, isChecked ? paddingExpanded : topCollapsed, 0,
				isChecked ? 0 : topCollapsed);
			reserveNow.setPadding(isChecked ? reserveExpanded : toggleCollapsed, 0,
				isChecked ? reserveExpanded : toggleCollapsed, 0);

			boolean passengerVisibility = isChecked && offer.vehicleInfo.adultCapacity > 0;
			boolean bagsVisibility = isChecked && offer.vehicleInfo.largeLuggageCapacity + offer.vehicleInfo.smallLuggageCapacity > 0;
			boolean doorsVisibility = isChecked && offer.vehicleInfo.maxDoors > 0;

			passengers.setVisibility(passengerVisibility ? View.VISIBLE : View.GONE);
			bags.setVisibility(bagsVisibility ? View.VISIBLE : View.GONE);
			doors.setVisibility(doorsVisibility ? View.VISIBLE : View.GONE);
			transmission.setVisibility(isChecked ? View.VISIBLE : View.GONE);

			mapView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			mapText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			totalPrice.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			reserveNow.setTextSize(TypedValue.COMPLEX_UNIT_SP, isChecked ? 17 : 15);

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
			final SearchCarOffer offer = (SearchCarOffer) itemView.getTag();
			addMarker(offer, googleMap);
			googleMap.getUiSettings().setMapToolbarEnabled(false);
			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(offer.pickUpLocation.latitude, offer.pickUpLocation.longitude),
				MAP_ZOOM_LEVEL));
			mapClickContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uri = String.format(Locale.ENGLISH, "geo:%f,%f", offer.pickUpLocation.latitude,
						offer.pickUpLocation.longitude);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					mContext.startActivity(intent);
					OmnitureTracking.trackAppCarMapClick(mContext);
				}
			});
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
		boolean shouldCollapseFirstItem = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppCarRatesCollapseTopListing);

		mLastExpanded = shouldCollapseFirstItem ? NONE_EXPANDED : 0;
		for (int i = 0; i < offers.size(); i++) {
			SearchCarOffer offer = offers.get(i);
			offer.isToggled = shouldCollapseFirstItem ? false : i == 0;
		}
	}

	public void onItemExpanded(int index) {
		if (mLastExpanded != NONE_EXPANDED && mLastExpanded != index) {
			offers.get(mLastExpanded).isToggled = false;
			notifyItemChanged(mLastExpanded);
		}
		offers.get(index).isToggled = true;
		notifyItemChanged(index);
		mLastExpanded = index;
	}
}
