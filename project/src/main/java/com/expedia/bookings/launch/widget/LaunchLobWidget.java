package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
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

	@InjectView(R.id.activities_button)
	PhoneLaunchButton lxBtn;

	@InjectView(R.id.lob_btn_container)
	LinearLayout btnContainer;

	@InjectView(R.id.lob_bg)
	View bg;

	@InjectView(R.id.shadow)
	View shadow;

	float origHeight;
	boolean wasNetworkUnavailable = false;
	PhoneLaunchButton[] lobButtons = new PhoneLaunchButton[4];

	public LaunchLobWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.section_phone_launch_lob, this);
		ButterKnife.inject(this);
		initializeLobBtnsArray();
	}

	private void initializeLobBtnsArray() {
		lobButtons[0] = hotelsBtn;
		lobButtons[1] = carsBtn;
		lobButtons[2] = flightsBtn;
		lobButtons[3] = lxBtn;
	}

	public void transformButtons(float f) {
		for (int i = 0; i < lobButtons.length; i++) {
			lobButtons[i].scaleTo(f);
		}
		bg.setScaleY(f);
		shadow.setTranslationY((f * origHeight) - origHeight);
	}

	public void updateVisibilities() {
		lxBtn.setVisibility(PointOfSale.getPointOfSale().supports(LineOfBusiness.LX) ? VISIBLE : GONE);
		carsBtn.setVisibility(PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS) ? VISIBLE : GONE);
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

			for (int i = 0; i < lobButtons.length; i++) {
				lobButtons[i].transformToDefaultState();
			}
			bg.setScaleY(1.0f);
			shadow.setTranslationY(0.0f);
		}

		wasNetworkUnavailable = false;
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		wasNetworkUnavailable = true;

		for (int i = 0; i < lobButtons.length; i++) {
			lobButtons[i].transformToNoDataState();
		}
		bg.setScaleY(1.0f);
		shadow.setTranslationY(0.0f);
	}

	public void updateView() {
		updateMargins();
		updateVisibilities();
	}

	public void updateMargins() {
		int sideMargin = getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_side);
		int middleMargin = getResources().getDimensionPixelSize(R.dimen.launch_tile_margin_middle);
		if (PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)) {
			setRightMargin(flightsBtn, middleMargin);
			setRightMargin(lxBtn, sideMargin);
		}
		else if (PointOfSale.getPointOfSale().supports(LineOfBusiness.CARS)) {
			setRightMargin(flightsBtn, middleMargin);
			setRightMargin(carsBtn, sideMargin);
		}
		else {
			setRightMargin(flightsBtn, sideMargin);
		}
	}

	private void setRightMargin(PhoneLaunchButton lobBtn, int pixels) {
		MarginLayoutParams params = (MarginLayoutParams) lobBtn.getLayoutParams();
		params.setMargins(0, 0, pixels, 0);
		lobBtn.setLayoutParams(params);
	}

}
