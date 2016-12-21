package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.widget.SlidingTabLayout;

/* TODO remove old phone launch toolbar */
public class NewPhoneLaunchToolbar extends Toolbar {

	@InjectView(R.id.tab_layout)
	public SlidingTabLayout slidingTabLayout;

	public NewPhoneLaunchToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.toolbar_phone_new_launch, this);
		ButterKnife.inject(this);
		updateActionBarLogo();
	}

	public void updateActionBarLogo() {
		int actionBarLogoResId = ProductFlavorFeatureConfiguration.getInstance().getLaunchScreenActionLogo();
		if (actionBarLogoResId != 0) {
			setNavigationIcon(actionBarLogoResId);
		}
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		slidingTabLayout.setCustomTabView(R.layout.phone_actionbar_tab_bg, R.id.tab_text);
		slidingTabLayout.setSelectedIndicatorColors(
			ContextCompat.getColor(getContext(), R.color.launch_tab_indicator));
		slidingTabLayout.setDistributeEvenly(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setElevation(getResources().getDimensionPixelSize(R.dimen.launch_toolbar_elevation));
		}
	}

}
