package com.expedia.bookings.launch.widget;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.expedia.bookings.utils.FeatureUtilKt.isBrandColorEnabled;

/* TODO remove old phone launch toolbar */
public class PhoneLaunchToolbar extends Toolbar {

	@InjectView(R.id.tab_layout)
	public TabLayout tabLayout;

	public PhoneLaunchToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.toolbar_phone_new_launch, this);
		ButterKnife.inject(this);
		updateActionBarLogo();
		if (isBrandColorEnabled(context)) {
			tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.brand_secondary));
		}
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
		setElevation(getResources().getDimensionPixelSize(R.dimen.launch_toolbar_elevation));
	}

}
