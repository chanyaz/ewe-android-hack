package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Images;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CategoryDetailsWidget extends LinearLayout {

	@InjectView(R.id.header_image)
	ImageView headerImage;

	@InjectView(R.id.details_time_header)
	android.widget.TextView timeDetailsText;

	@InjectView(R.id.category_text)
	android.widget.TextView categoryText;

	@InjectView(R.id.offer_list)
	RecyclerView offerList;

	CarOffersAdapter adapter;

	private LinearLayoutManager mLayoutManager;
	private static final int LIST_VERTICAL_SPACER_DP = 8;

	public CategoryDetailsWidget(Context context) {
		super(context);
	}

	public CategoryDetailsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);
		mLayoutManager = new LinearLayoutManager(getContext());
		mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mLayoutManager.scrollToPosition(0);

		offerList.setLayoutManager(mLayoutManager);
		offerList.addItemDecoration(new RecyclerDividerDecoration(getContext(), LIST_VERTICAL_SPACER_DP));
		offerList.setHasFixedSize(true);
		//TODO add images
		//offerList.setOnScrollListener(new PicassoScrollListener(getContext(), PICASSO_TAG));

		adapter = new CarOffersAdapter();
		offerList.setAdapter(adapter);
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Events.unregister(this);
	}

	public void setCategorizedOffers(CategorizedCarOffers offers) {
		adapter.setCarOffers(offers.offers);
		adapter.notifyDataSetChanged();

		String url = Images.getCarRental(offers.category, offers.getLowestTotalPriceOffer().vehicleInfo.type);
		new PicassoHelper.Builder(headerImage)
			.setTag("Car Details")
			.fit()
			.centerCrop()
			.build()
			.load(url);
	}

}
