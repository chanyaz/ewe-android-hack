package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.utils.Ui;

public class CarsListAdapter extends RecyclerView.Adapter<CarsListAdapter.ViewHolder> {
	List<CarCategory> categoriesTestList = new ArrayList<>();

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.include_car_category, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.mCategoryTextView.setText(categoriesTestList.get(position).toString());
		holder.mBestPriceTextView.setText(CarDb.carSearch.carCategoryOfferMap.get(categoriesTestList.get(position)).getSelectedOffer().fare.total.getFormattedMoney());
	}

	@Override
	public int getItemCount() {
		return categoriesTestList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		private TextView mCategoryTextView;
		private TextView mBestPriceTextView;
		public ViewHolder(View view) {
			super(view);
			mCategoryTextView = Ui.findView(view, R.id.category_text);
			mBestPriceTextView = Ui.findView(view, R.id.category_price_text);
		}
	}

	public void setCategoriesTestList(List<CarCategory> categoriesTestList) {
		this.categoriesTestList = categoriesTestList;
	}
}
