package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
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

	@InjectView(R.id.offer_list)
	public RecyclerView offerList;

	private CarOffersAdapter adapter;
	private static final int LIST_DIVIDER_HEIGHT = 0;
	private float headerHeight;
	private float offset;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);

		int toolbarSize = Ui.getToolbarSize(getContext());
		offset = Ui.toolbarSizeWithStatusBar(getContext());
		headerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getContext().getResources().getDisplayMetrics());

		offerList.setLayoutManager(layoutManager);
		offerList.addItemDecoration(
			new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, (int) headerHeight, toolbarSize, true));
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
	public void onCarsIsFiltered(Events.CarsIsFiltered event) {
		if (event.categorizedCarOffers != null) {
			CategorizedCarOffers bucket = event.carSearch.getFromDisplayLabel(event.categorizedCarOffers.carCategoryDisplayLabel);
			adapter.setCarOffers(bucket.offers);
			adapter.notifyDataSetChanged();
		}
	}

	@Subscribe
	public void onCarsShowDetails(Events.CarsShowDetails event) {
		CategorizedCarOffers bucket = event.categorizedCarOffers;
		offerList.setVisibility(View.VISIBLE);
		backgroundHeader.setVisibility(View.VISIBLE);

		adapter.setCarOffers(bucket.offers);
		adapter.notifyDataSetChanged();

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type, getResources().getDimension(R.dimen.car_image_width));
		new PicassoHelper.Builder(headerImage)
			.setError(R.drawable.cars_fallback)
			.fade()
			.build()
			.load(url);
	}

	public float parallaxScrollHeader() {
		View view = offerList.getChildAt(0);
		int top = view.getTop();
		float y = headerHeight - top;
		backgroundHeader.setTranslationY(Math.min(-y * 0.5f, 0f));
		return y / (headerHeight - offset);
	}

	public void reset() {
		offerList.getLayoutManager().scrollToPosition(0);
		backgroundHeader.setTranslationY(0);
	}

}
