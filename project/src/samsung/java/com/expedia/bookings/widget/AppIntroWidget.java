package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.navigation.NavUtils;

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

	@InjectView(R.id.skip_intro)
	TextView skipIntroText;

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
		updatePageUI(0);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// ignore
	}

	@Override
	public void onPageSelected(int position) {
		updatePageUI(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// ignore
	}

	private void updatePageUI(int selectedPosition) {
		updateIndicators(selectedPosition);
		updateSkipVisibility(selectedPosition);
	}

	private void updateIndicators(int selectedPosition) {
		for (int i = 0; i < introPagerAdapter.getCount(); i++) {
			indicators[i].setImageResource(
				i == selectedPosition ? R.drawable.intro_selected_indicator
					: R.drawable.intro_unselected_indicator);
		}
	}

	/**
	 * Sets the visibility of SKIP depending upon the current position of the viewpager.
	 * It will be hidden for the last position.
	 *
	 * @param selectedPosition current position of the viewpager
	 */
	private void updateSkipVisibility(int selectedPosition) {
		int visibility = (selectedPosition == introPagerAdapter.getCount() - 1) ? GONE : VISIBLE;
		skipIntroText.setVisibility(visibility);
	}

	private void goToLaunchScreen() {
		NavUtils.goToLaunchScreen(getContext());
		((Activity) getContext()).finish();
	}
}
