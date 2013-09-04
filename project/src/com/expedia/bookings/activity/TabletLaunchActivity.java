package com.expedia.bookings.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.TabletSearchFragment;
import com.expedia.bookings.fragment.TabletSearchFragment.SearchFragmentListener;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.fragment.debug.ColorFragment;
import com.expedia.bookings.utils.Ui;

public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
		SearchFragmentListener {

	// On top when search params covers up everything
	private static final String BACKSTACK_SEARCH_PARAMS = "BACKSTACK_SEARCH_PARAMS";

	private MeasurableFragment mTopFragment;
	private Fragment mBottomFragment;
	private TabletSearchFragment mSearchFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		getWindow().setBackgroundDrawable(null);

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			mTopFragment = ColorFragment.newInstance(Color.BLUE);
			mBottomFragment = ColorFragment.newInstance(Color.GREEN);
			mSearchFragment = new TabletSearchFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.top_container, mTopFragment);
			ft.add(R.id.bottom_container, mBottomFragment);
			ft.add(R.id.search_container, mSearchFragment);
			ft.commit();
		}
		else {
			mTopFragment = Ui.findSupportFragment(this, R.id.top_container);
			mBottomFragment = Ui.findSupportFragment(this, R.id.bottom_container);
			mSearchFragment = Ui.findSupportFragment(this, R.id.search_container);

			if (BACKSTACK_SEARCH_PARAMS.equals(getTopBackStackName())) {
				mSearchFragment.expand();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (!mSearchFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Back stack utils

	public String getTopBackStackName() {
		FragmentManager fm = getSupportFragmentManager();
		int backStackEntryCount = fm.getBackStackEntryCount();
		if (backStackEntryCount > 0) {
			return fm.getBackStackEntryAt(backStackEntryCount - 1).getName();
		}
		return "";
	}

	//////////////////////////////////////////////////////////////////////////
	// MeasureableFragmentListener

	@Override
	public void canMeasure(Fragment fragment) {
		if ((fragment == mTopFragment || fragment == mSearchFragment) && mTopFragment.isMeasurable()
				&& mSearchFragment.isMeasurable()) {
			mSearchFragment.setInitialTranslationY(mTopFragment.getView().getHeight());
			mSearchFragment.collapse();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchFragmentListener

	@Override
	public void onFinishExpand() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.detach(mTopFragment);
		ft.detach(mBottomFragment);
		ft.addToBackStack(BACKSTACK_SEARCH_PARAMS);
		ft.commit();
	}

}
