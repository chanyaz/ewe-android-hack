package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.RateTerm;
import com.expedia.bookings.data.cars.SearchCarFare;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Images;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoriesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int LOADING_VIEW = 0;
	private static final int DATA_VIEW = 1;
	private List<CategorizedCarOffers> categories = new ArrayList<>();
	private static final String ROW_PICASSO_TAG = "CAR_CATEGORY_LIST";
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
		}
		else {
			ValueAnimator animation = AnimUtils.setupLoadingAnimation(((LoadingViewHolder) holder).backgroundImageView, position % 2 == 0);
			((LoadingViewHolder) holder).setAnimator(animation);
		}
	}

	@Override
	public void onViewRecycled(RecyclerView.ViewHolder holder) {
		if (holder.getItemViewType() == LOADING_VIEW) {
			((LoadingViewHolder) holder).cancelAnimation();
		}
		super.onViewRecycled(holder);
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

		@InjectView(R.id.gradient_mask)
		public View gradientMask;


		public ViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, itemView);
			itemView.setOnClickListener(this);
		}

		public void bindCategorizedOffers(CategorizedCarOffers cco) {
			itemView.setTag(cco);

			SearchCarFare lowestFare = cco.getLowestTotalPriceOffer().fare;

			CarInfo vehicleInfo = cco.getLowestTotalPriceOffer().vehicleInfo;
			categoryTextView.setText(vehicleInfo.carCategoryDisplayLabel);

			setTextAndVisibility(doorCount, CarDataUtils.getDoorCount(cco));
			setTextAndVisibility(passengerCount, CarDataUtils.getPassengerCount(cco));
			setTextAndVisibility(bagCount, CarDataUtils.getBagCount(cco));

			cardView.setPreventCornerOverlap(false);

			if (lowestFare.rateTerm.equals(RateTerm.UNKNOWN)) {
				bestPriceTextView.setText("");
				bestPriceTextView.setVisibility(View.GONE);
			}
			else {
				bestPriceTextView.setText(totalTextView.getContext()
					.getString(R.string.car_details_TEMPLATE,
						CarDataUtils.getStringTemplateForRateTerm(bestPriceTextView.getContext(), lowestFare.rateTerm),
						Money.getFormattedMoneyFromAmountAndCurrencyCode(lowestFare.rate.amount,
							lowestFare.rate.getCurrency(), Money.F_NO_DECIMAL)));
				bestPriceTextView.setVisibility(View.VISIBLE);
			}
			totalTextView.setText(totalTextView.getContext()
				.getString(R.string.cars_total_template, Money.getFormattedMoneyFromAmountAndCurrencyCode(
					lowestFare.total.amount, lowestFare.total.getCurrency(), Money.F_NO_DECIMAL)));
			gradientMask.setVisibility(View.GONE);
			String url = Images.getCarRental(cco.category, cco.getLowestTotalPriceOffer().vehicleInfo.type,
				itemView.getContext().getResources().getDimension(R.dimen.car_image_width));
			new PicassoHelper.Builder(itemView.getContext())
				.setPlaceholder(R.drawable.results_list_placeholder)
				.setError(R.drawable.cars_fallback)
				.fade()
				.setTag(ROW_PICASSO_TAG)
				.setTarget(target)
				.build()
				.load(url);
		}

		@Override
		public void onClick(View view) {
			CategorizedCarOffers offers = (CategorizedCarOffers) view.getTag();
			Events.post(new Events.CarsShowDetails(offers));
		}

		private PicassoTarget target = new PicassoTarget() {
			@Override
			public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
				super.onBitmapLoaded(bitmap, from);
				backgroundImageView.setImageBitmap(bitmap);
				gradientMask.setVisibility(View.VISIBLE);
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {
				super.onBitmapFailed(errorDrawable);
				if (errorDrawable != null) {
					backgroundImageView.setImageDrawable(errorDrawable);
					gradientMask.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {
				super.onPrepareLoad(placeHolderDrawable);
				backgroundImageView.setImageDrawable(placeHolderDrawable);
				gradientMask.setVisibility(View.GONE);
			}
		};

		private static void setTextAndVisibility(TextView textView, String text) {
			if (text != null) {
				textView.setText(text);
				textView.setVisibility(View.VISIBLE);
			}
			else {
				textView.setVisibility(View.GONE);
			}
		}
	}

	public void setCategories(List<CategorizedCarOffers> categories) {
		this.categories = categories;
	}

}
