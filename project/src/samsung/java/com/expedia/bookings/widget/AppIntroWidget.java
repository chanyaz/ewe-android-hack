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

		int indicatorPadding = getResources().getDimensionPixelSize(R.dimen.intro_indicator_image_padding);
		indicators = new ImageView[introPagerAdapter.getCount()];
		for (int i = 0; i < introPagerAdapter.getCount(); i++) {
			indicators[i] = new ImageView(getContext());
			indicators[i].setPadding(indicatorPadding, 0, indicatorPadding, 0);
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
			indicators[i].setImageResource(
				i == selectedPosition ? R.drawable.intro_selected_indicator
					: R.drawable.intro_unselected_indicator);
		}
	}

	private void goToLaunchScreen() {
		NavUtils.goToLaunchScreen(getContext());
		SettingUtils
			.save(getContext(), R.string.preference_app_intro_shown_once, true);
	}
}
