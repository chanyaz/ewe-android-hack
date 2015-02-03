package com.expedia.bookings.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;

public class CarCategoryDetailsWidget extends LinearLayout {

	public CarCategoryDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	ProgressDialog mDoingCreateTripDialog;

	@InjectView(R.id.header_image)
	ImageView headerImage;

	@InjectView(R.id.offer_list)
	RecyclerView offerList;

	private CarOffersAdapter adapter;
	private static final int LIST_DIVIDER_HEIGHT = 8;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		layoutManager.scrollToPosition(0);

		offerList.setLayoutManager(layoutManager);
		offerList.addItemDecoration(new RecyclerDividerDecoration(getContext(), LIST_DIVIDER_HEIGHT, LIST_DIVIDER_HEIGHT));
		offerList.setHasFixedSize(true);
		//TODO add images
		//offerList.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));

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
		adapter.setCarOffers(bucket.offers);
		adapter.notifyDataSetChanged();

		String url = Images.getCarRental(bucket.category, bucket.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(headerImage)
			.setTag("Car Details")
			.fit()
			.centerCrop()
			.build()
			.load(url);
	}

	public void showProgressDialog() {
		if (mDoingCreateTripDialog == null) {
			mDoingCreateTripDialog = new ProgressDialog(getContext());
			mDoingCreateTripDialog.setMessage("Preparing checkout...");
			mDoingCreateTripDialog.setIndeterminate(true);
		}
		mDoingCreateTripDialog.show();
	}

	@Subscribe
	public void onItemSelected(Events.CarsKickOffCreateTrip event) {
		showProgressDialog();
		CarDb.getCarServices().createTrip(event.offer.productKey, event.offer.fare.total.amount.toString(), new Observer<CarCreateTripResponse>() {
			@Override
			public void onCompleted() {
				// ignore
			}

			@Override
			public void onError(Throwable e) {
				throw new RuntimeException(e);
			}

			@Override
			public void onNext(CarCreateTripResponse response) {
				mDoingCreateTripDialog.dismiss();
				Events.post(new Events.CarsShowCheckout(response));
			}
		});
	}

}
