package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LaunchFiveLobDoubleRowWidget extends RelativeLayout {

	@InjectView(R.id.hotels_button)
	PhoneLaunchDoubleRowButton hotelsBtn;

	@InjectView(R.id.flights_button)
	PhoneLaunchDoubleRowButton flightsBtn;

	@InjectView(R.id.cars_button)
	PhoneLaunchButton carsBtn;

	@InjectView(R.id.activities_button)
	PhoneLaunchButton lxBtn;

	@InjectView(R.id.transport_button)
	PhoneLaunchButton transportBtn;

	@InjectView(R.id.lob_bottom_row)
	FrameLayout bottomRow;

	@InjectView(R.id.lob_bottom_row_bg)
	View bottomRowBg;

	@InjectView(R.id.vertical_divider)
	View divider;

	@InjectView(R.id.shadow)
	View shadow;

	float origHeight;
	float bottomRowOrigHeight;

	public LaunchFiveLobDoubleRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.section_phone_launch_double_row_five_lob, this);
		ButterKnife.inject(this);
	}

	public void transformButtons(float f) {
		carsBtn.scaleTo(f);
		lxBtn.scaleTo(f);
		transportBtn.scaleTo(f);

		//Hotel and Flight button final height after scroll should be same as car, lx and transport button
		//So normalizing the value to get final 52.2 height of lob button
		f = 0.64f + (((f - 0.6f) / (1 - 0.6f)) * 0.36f);
		hotelsBtn.scaleTo(f);
		flightsBtn.scaleTo(f);
		bottomRowBg.setScaleY(f);
		divider.setTranslationY((f * origHeight) - origHeight);
		bottomRow.setTranslationY((f * origHeight) - origHeight);
		shadow.setTranslationY((f * bottomRowOrigHeight*2) - bottomRowOrigHeight*2);
	}

	public void toggleButtonState(boolean enabled) {
		hotelsBtn.setEnabled(enabled);
		flightsBtn.setEnabled(enabled);
		if (!enabled) {
			carsBtn.transformToNoDataState();
			lxBtn.transformToNoDataState();
			transportBtn.transformToNoDataState();
			bottomRowBg.setScaleY(1.0f);
			divider.setTranslationY(0.0f);
			bottomRow.setTranslationY(0.0f);
			shadow.setTranslationY(0.0f);
		}
		else {
			carsBtn.transformToDefaultState();
			lxBtn.transformToDefaultState();
			transportBtn.transformToDefaultState();
		}
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

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		bottomRowBg.setPivotY(-bottomRowBg.getTop());
		origHeight = getResources().getDimension(R.dimen.launch_lob_double_row_height);
		bottomRowOrigHeight = getResources().getDimension(R.dimen.launch_five_lob_double_row_height);
	}

	@Subscribe
	public void onNetworkAvailable(Events.LaunchOnlineState event) {
		toggleButtonState(true);
	}

	@Subscribe
	public void onNetworkUnavailable(Events.LaunchOfflineState event) {
		toggleButtonState(false);
	}

}
