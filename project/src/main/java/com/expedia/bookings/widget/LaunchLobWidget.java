package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;

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

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		bg.setPivotY(-bg.getTop());
		origHeight = getResources().getDimension(R.dimen.launch_lob_tiles);
	}
}
