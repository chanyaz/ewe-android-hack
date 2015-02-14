package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;

public class CarCategoriesListAdapter extends RecyclerView.Adapter<CarCategoriesListAdapter.ViewHolder> {
	private List<CategorizedCarOffers> categories = new ArrayList<>();

	private static final String ROW_PICASSO_TAG = "CAR_CATEGORY_LIST";

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.section_car_category_summary, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		CategorizedCarOffers cco = categories.get(position);
		holder.bindCategorizedOffers(cco);

		String url = Images.getCarRental(cco.category, cco.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(holder.backroundImageView)
			.fade()
			.setTag(ROW_PICASSO_TAG)
			.fit()
			.centerCrop()
			.build()
			.load(url);
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		@InjectView(R.id.category_text)
		public TextView categoryTextView;

		@InjectView(R.id.category_price_text)
		public TextView bestPriceTextView;

		@InjectView(R.id.total_price_text)
		public TextView totalTextView;

		@InjectView(R.id.background_image_view)
		public ImageView backroundImageView;

		@InjectView(R.id.passenger_count)
		public TextView passengerCount;

		@InjectView(R.id.bag_count)
		public TextView bagCount;

		@InjectView(R.id.door_count)
		public TextView doorCount;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindCategorizedOffers(CategorizedCarOffers cco) {
			itemView.setTag(cco);

			SearchCarFare lowestFare = cco.getLowestTotalPriceOffer().fare;
			CarInfo vehicleInfo = cco.getLowestTotalPriceOffer().vehicleInfo;
			categoryTextView.setText(cco.category.toString());
			passengerCount.setText(String.valueOf(vehicleInfo.adultCapacity + vehicleInfo.childCapacity));
			bagCount.setText(String.valueOf(vehicleInfo.largeLuggageCapacity + vehicleInfo.smallLuggageCapacity));
			doorCount.setText(String.valueOf(vehicleInfo.maxDoors));

			bestPriceTextView.setText(totalTextView.getContext().getString(R.string.cars_total_template, lowestFare.rate.getFormattedMoney()));
			totalTextView.setText(totalTextView.getContext().getString(R.string.cars_total_template, lowestFare.total.getFormattedMoney()));
		}

		@Override
		public void onClick(View view) {
			CategorizedCarOffers offers = (CategorizedCarOffers) view.getTag();
			Events.post(new Events.CarsShowDetails(offers));
		}
	}

	public void setCategories(List<CategorizedCarOffers> categories) {
		this.categories = categories;
	}
}
