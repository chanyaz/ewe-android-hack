package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CarInfo;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarCategoryDetailsWidget extends LinearLayout {

	public CarCategoryDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.header_image)
	ImageView headerImage;

	@InjectView(R.id.offer_list)
	RecyclerView offerList;

	@InjectView(R.id.passenger_count)
	public TextView passengerCount;

	@InjectView(R.id.bag_count)
	public TextView bagCount;

	@InjectView(R.id.door_count)
	public TextView doorCount;

	private CarOffersAdapter adapter;
	private static final int LIST_DIVIDER_HEIGHT = 12;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);

		offerList.setLayoutManager(layoutManager);
		offerList.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, LIST_DIVIDER_HEIGHT, 0, true));
		offerList.setHasFixedSize(true);

		adapter = new CarOffersAdapter();
		offerList.setAdapter(adapter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Subscribe
	public void onCarsShowDetails(Events.CarsShowDetails event) {
		CategorizedCarOffers bucket = event.categorizedCarOffers;

		CarInfo vehicleInfo = bucket.getLowestTotalPriceOffer().vehicleInfo;
		passengerCount.setText(String.valueOf(vehicleInfo.adultCapacity + vehicleInfo.childCapacity));
		bagCount.setText(String.valueOf(vehicleInfo.largeLuggageCapacity + vehicleInfo.smallLuggageCapacity));
		doorCount.setText(String.valueOf(vehicleInfo.maxDoors));

		adapter.setCarOffers(bucket.offers);
		adapter.notifyDataSetChanged();

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(headerImage)
			.fade()
			.setTag("Car Details")
			.fit()
			.centerCrop()
			.build()
			.load(url);
	}
}
