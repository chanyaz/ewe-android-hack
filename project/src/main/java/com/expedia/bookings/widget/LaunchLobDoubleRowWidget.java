package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LaunchLobDoubleRowWidget extends RelativeLayout {

	@InjectView(R.id.hotels_button)
	PhoneLaunchDoubleRowButton hotelsBtn;

	@InjectView(R.id.flights_button)
	PhoneLaunchDoubleRowButton flightsBtn;

	@InjectView(R.id.cars_button)
	PhoneLaunchDoubleRowButton carsBtn;

	@InjectView(R.id.activities_button)
	PhoneLaunchDoubleRowButton lxBtn;

	@InjectView(R.id.lob_bottom_row)
	FrameLayout bottomRow;

	@InjectView(R.id.lob_top_row_bg)
	View topRowBg;

	@InjectView(R.id.lob_bottom_row_bg)
	View bottomRowBg;

	@InjectView(R.id.vertical_divider)
	View divider;

	@InjectView(R.id.shadow)
	View shadow;

	float origHeight;
	boolean wasNetworkUnavailable = false;

	public LaunchLobDoubleRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.section_phone_launch_double_row_lob, this);
		ButterKnife.inject(this);
	}

	public void transformButtons(float f) {
		hotelsBtn.scaleTo(f);
		flightsBtn.scaleTo(f);
		carsBtn.scaleTo(f);
		lxBtn.scaleTo(f);
		topRowBg.setScaleY(f);
		bottomRowBg.setScaleY(f);
		divider.setTranslationY((f * origHeight) - origHeight);
		bottomRow.setTranslationY((f * origHeight) - origHeight);
		shadow.setTranslationY((f * origHeight * 2) - origHeight * 2);
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
		topRowBg.setPivotY(-topRowBg.getTop());
		bottomRowBg.setPivotY(-bottomRowBg.getTop());
		origHeight = getResources().getDimension(R.dimen.launch_lob_double_row_height);
	}

	@Subscribe
	public void onNetworkAvailable(Events.LaunchOnlineState event) {
		if (wasNetworkUnavailable) {
			hotelsBtn.transformToDefaultState();
			flightsBtn.transformToDefaultState();
			carsBtn.transformToDefaultState();
			lxBtn.transformToDefaultState();
			topRowBg.setScaleY(1.0f);
			bottomRowBg.setScaleY(1.0f);
			divider.setTranslationY(0.0f);
			bottomRow.setTranslationY(0.0f);
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
		lxBtn.transformToNoDataState();
		topRowBg.setScaleY(1.0f);
		bottomRowBg.setScaleY(1.0f);
		divider.setTranslationY(0.0f);
		bottomRow.setTranslationY(0.0f);
		shadow.setTranslationY(0.0f);
	}
}

