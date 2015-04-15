package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LaunchLobWidget extends RelativeLayout {

	@InjectView(R.id.hotels_button)
	PhoneLaunchButton hotelsBtn;

	@InjectView(R.id.flights_button)
	PhoneLaunchButton flightsBtn;

	@InjectView(R.id.cars_button)
	PhoneLaunchButton carsBtn;

	@InjectView(R.id.lob_btn_container)
	LinearLayout btnContainer;

	@InjectView(R.id.lob_bg)
	View bg;

	@InjectView(R.id.shadow)
	View shadow;

	float origHeight;
	boolean wasNetworkUnavailable = false;

	public LaunchLobWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.section_phone_launch_lob, this);
		ButterKnife.inject(this);
	}

	public void transformButtons(float f) {
		hotelsBtn.scaleTo(f);
		flightsBtn.scaleTo(f);
		carsBtn.scaleTo(f);
		bg.setScaleY(f);
		shadow.setTranslationY((f * origHeight) - origHeight);
	}

	public void updateVisibilities() {
		if (PointOfSale.getPointOfSale().supportsCars()) {
			carsBtn.setVisibility(View.VISIBLE);
		}
		else {
			carsBtn.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
		// Otto events aren't being received so call updateVisibilities() here when ever the app crashes and restarts.
		updateVisibilities();
	}

	@Override
	public void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		bg.setPivotY(-bg.getTop());
		origHeight = getResources().getDimension(R.dimen.launch_lob_height);
	}

	@Subscribe
	public void onNetworkAvailable(Events.LaunchOnlineState event) {
		if (wasNetworkUnavailable) {
			hotelsBtn.transformToDefaultState();
			flightsBtn.transformToDefaultState();
			carsBtn.transformToDefaultState();
			bg.setScaleY(1.0f);
			shadow.setTranslationY(0.0f);
		}
		wasNetworkUnavailable = false;
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		wasNetworkUnavailable = true;
		hotelsBtn.transformToNoDataState();
		flightsBtn.transformToNoDataState();
		carsBtn.transformToNoDataState();
		bg.setScaleY(1.0f);
		shadow.setTranslationY(0.0f);
	}
}
