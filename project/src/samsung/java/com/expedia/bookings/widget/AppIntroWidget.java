package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.SettingUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AppIntroWidget extends FrameLayout implements ViewPager.OnPageChangeListener {

	public AppIntroWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.app_intro_pager)
	ViewPager introPager;

	@InjectView(R.id.pager_indicator_container)
	LinearLayout pagerIndicatorContainer;

	private AppIntroPagerAdapter introPagerAdapter;
	private ImageView[] indicators;

	@OnClick(R.id.pager_next)
	public void onNext() {
		if (introPager.getCurrentItem() < introPagerAdapter.getCount() - 1) {
			introPager.setCurrentItem(introPager.getCurrentItem() + 1);
		}
		else {
			goToLaunchScreen();
		}
	}

	@OnClick(R.id.skip_intro)
	public void onSkip() {
		goToLaunchScreen();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		introPagerAdapter = new AppIntroPagerAdapter(getContext());
		introPager.setAdapter(introPagerAdapter);
		introPager.setOnPageChangeListener(this);

		indicators = new ImageView[introPagerAdapter.getCount()];
		for (int i = 0; i < introPagerAdapter.getCount(); i++) {
			indicators[i] = new ImageView(getContext());
			indicators[i].setMinimumHeight(20);
			indicators[i].setMinimumWidth(20);
			pagerIndicatorContainer.addView(indicators[i]);
		}
		updateIndicators(0);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// ignore
	}

	@Override
	public void onPageSelected(int position) {
		updateIndicators(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// ignore
	}

	private void updateIndicators(int selectedPosition) {
		for (int i = 0; i < introPagerAdapter.getCount(); i++) {
			// TODO Replace with actual assets once available.
			indicators[i].setBackgroundColor(
				getResources().getColor(i == selectedPosition ? R.color.cars_primary_color : R.color.lx_primary_color));
		}
	}

	private void goToLaunchScreen() {
		NavUtils.goToLaunchScreen(getContext());
		SettingUtils
			.save(getContext(), R.string.preference_app_intro_shown_once, true);
	}
}
