package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;

public class CarCategoriesListAdapter extends RecyclerView.Adapter<CarCategoriesListAdapter.ViewHolder> {
	List<CategorizedCarOffers> categories = new ArrayList<>();

	private static final String ROW_PICASSO_TAG = "car_row";

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
		new PicassoHelper.Builder(holder.mBackroundImageView)
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

		private View mRoot;
		private TextView mCategoryTextView;
		private TextView mBestPriceTextView;
		private TextView mTotalTextView;
		private ImageView mBackroundImageView;

		private TextView mPassengerCount;
		private TextView mBagCount;
		private TextView mDoorCount;

		public ViewHolder(View view) {
			super(view);
			mRoot = view;
			mRoot.setOnClickListener(this);
			mCategoryTextView = Ui.findView(view, R.id.category_text);
			mBestPriceTextView = Ui.findView(view, R.id.category_price_text);
			mBackroundImageView = Ui.findView(view, R.id.background_image_view);
			mTotalTextView = Ui.findView(view, R.id.total_price_text);

			mPassengerCount = Ui.findView(view, R.id.passenger_count);
			mBagCount = Ui.findView(view, R.id.bag_count);
			mDoorCount = Ui.findView(view, R.id.door_count);
		}

		public void bindCategorizedOffers(CategorizedCarOffers cco) {
			mRoot.setTag(cco);
			SearchCarFare lowestFare = cco.getLowestTotalPriceOffer().fare;
			CarInfo vehicleInfo = cco.getLowestTotalPriceOffer().vehicleInfo;
			mCategoryTextView.setText(cco.category.toString());
			mPassengerCount.setText(String.valueOf(vehicleInfo.adultCapacity + vehicleInfo.childCapacity));
			mBagCount.setText(String.valueOf(vehicleInfo.largeLuggageCapacity + vehicleInfo.smallLuggageCapacity));
			mDoorCount.setText(String.valueOf(vehicleInfo.maxDoors));

			mBestPriceTextView.setText(lowestFare.rate.getFormattedMoney());
			mTotalTextView.setText(lowestFare.total.getFormattedMoney());
		}

		@Override
		public void onClick(View v) {
			CategorizedCarOffers offers = (CategorizedCarOffers) v.getTag();
			Events.post(new Events.CarsShowDetails(offers));
		}
	}

	public void setCategories(List<CategorizedCarOffers> categories) {
		this.categories = categories;
	}
}
