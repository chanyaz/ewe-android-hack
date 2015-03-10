package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
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

public class CarCategoryDetailsWidget extends FrameLayout {

	public CarCategoryDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.background_header)
	View backgroundHeader;

	@InjectView(R.id.header_image)
	ImageView headerImage;

	@InjectView(R.id.price_container)
	LinearLayout priceContainer;

	@InjectView(R.id.offer_list)
	public RecyclerView offerList;

	@InjectView(R.id.passenger_count)
	public TextView passengerCount;

	@InjectView(R.id.bag_count)
	public TextView bagCount;

	@InjectView(R.id.door_count)
	public TextView doorCount;

	private CarOffersAdapter adapter;
	private static final int LIST_DIVIDER_HEIGHT = 0;
	private float headerHeight;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);

		TypedValue typedValue = new TypedValue();
		int[] textSizeAttr = new int[] { android.R.attr.actionBarSize };
		TypedArray a = getContext().obtainStyledAttributes(typedValue.data, textSizeAttr);
		float toolbarSize = a.getDimension(0, 44f);

		headerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getContext().getResources().getDisplayMetrics());

		offerList.setLayoutManager(layoutManager);
		offerList.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, (int) headerHeight, (int) toolbarSize, true));
		offerList.setHasFixedSize(true);

		adapter = new CarOffersAdapter(getContext());
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
		if (vehicleInfo.minDoors != vehicleInfo.maxDoors) {
			doorCount.setText(doorCount.getContext()
				.getString(R.string.car_door_range_TEMPLATE, vehicleInfo.minDoors, vehicleInfo.maxDoors));
		}
		else {
			doorCount.setText(String.valueOf(vehicleInfo.maxDoors));
		}

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

 	public float parallaxScrollHeader() {
		View view = offerList.getChildAt(0);
		int top = view.getTop();
		float y = headerHeight - top;
		backgroundHeader.setTranslationY(Math.min(-y * 0.5f, 0f));
		priceContainer.setTranslationY(-y * 0.5f);
		return y / headerHeight;
	}

	public void reset() {
		offerList.getLayoutManager().scrollToPosition(0);
		backgroundHeader.setTranslationY(0);
		priceContainer.setTranslationY(0);
	}

}
