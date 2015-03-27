package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Images;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoriesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int LOADING_VIEW = 0;
	private static final int DATA_VIEW = 1;
	private List<CategorizedCarOffers> categories = new ArrayList<>();
	private static final String ROW_PICASSO_TAG = "CAR_CATEGORY_LIST";
	private ArrayList<ValueAnimator> mAnimations = new ArrayList<ValueAnimator>();
	public static boolean loadingState = false;

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == LOADING_VIEW) {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.car_lx_loading_animation_widget, parent, false);
			return new LoadingViewHolder(view);
		}
		else {
			View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.section_car_category_summary, parent, false);
			return new ViewHolder(view);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return loadingState ? LOADING_VIEW : DATA_VIEW;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder.getItemViewType() != LOADING_VIEW) {
			CategorizedCarOffers cco = categories.get(position);
			((ViewHolder) holder).bindCategorizedOffers(cco);
			String url = Images.getCarRental(cco.category, cco.getLowestTotalPriceOffer().vehicleInfo.type);
			new PicassoHelper.Builder(((ViewHolder) holder).backgroundImageView)
				.setPlaceholder(R.drawable.cars_placeholder)
				.fade()
				.setTag(ROW_PICASSO_TAG)
				.build()
				.load(url);
		}
		else {
			ValueAnimator animation = AnimUtils.setupLoadingAnimation(((LoadingViewHolder) holder).backgroundImageView, LoadingViewHolder.index);
			mAnimations.add(animation);
			LoadingViewHolder.index++;
		}
	}

	public void cleanup() {
		for (ValueAnimator animation : mAnimations) {
			animation.cancel();
		}
		mAnimations.clear();
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
		public ImageView backgroundImageView;

		@InjectView(R.id.passenger_count)
		public TextView passengerCount;

		@InjectView(R.id.bag_count)
		public TextView bagCount;

		@InjectView(R.id.door_count)
		public TextView doorCount;

		@InjectView(R.id.card_view)
		public CardView cardView;

		public Context mContext;

		public ViewHolder(View view) {
			super(view);
			mContext = view.getContext();
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindCategorizedOffers(CategorizedCarOffers cco) {
			itemView.setTag(cco);

			SearchCarFare lowestFare = cco.getLowestTotalPriceOffer().fare;
			CarInfo vehicleInfo = cco.getLowestTotalPriceOffer().vehicleInfo;
			categoryTextView.setText(vehicleInfo.carCategoryDisplayLabel);
			passengerCount.setText(String.valueOf(vehicleInfo.adultCapacity + vehicleInfo.childCapacity));
			bagCount.setText(String.valueOf(vehicleInfo.largeLuggageCapacity + vehicleInfo.smallLuggageCapacity));
			if (vehicleInfo.minDoors != vehicleInfo.maxDoors) {
				doorCount.setText(doorCount.getContext()
					.getString(R.string.car_door_range_TEMPLATE, vehicleInfo.minDoors, vehicleInfo.maxDoors));
			}
			else {
				doorCount.setText(String.valueOf(vehicleInfo.maxDoors));
			}
			cardView.setPreventCornerOverlap(false);
			bestPriceTextView.setText(totalTextView.getContext()
				.getString(R.string.car_details_TEMPLATE,
					CarDataUtils.getStringTemplateForRateTerm(bestPriceTextView.getContext(), lowestFare.rateTerm),
					lowestFare.rate.getFormattedMoney()));
			totalTextView.setText(totalTextView.getContext()
				.getString(R.string.cars_total_template, lowestFare.total.getFormattedMoney()));
		}

		@Override
		public void onClick(View view) {
			CategorizedCarOffers offers = (CategorizedCarOffers) view.getTag();
			Events.post(new Events.CarsShowDetails(offers));
			OmnitureTracking.trackAppCarRateDetails(mContext, offers);
		}
	}

	public void setCategories(List<CategorizedCarOffers> categories) {
		this.categories = categories;
	}

}
