package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;

public class UserReviewsFragmentPagerAdapter extends FragmentPagerAdapter {

	// Instance variable names
	private static final String FRAGMENT_RECENT = "FRAGMENT_RECENT";
	private static final String FRAGMENT_FAVORABLE = "FRAGMENT_FAVORABLE";
	private static final String FRAGMENT_CRITICAL = "FRAGMENT_CRITICAL";

	private static final String[] TAGS = { FRAGMENT_RECENT, FRAGMENT_FAVORABLE, FRAGMENT_CRITICAL };
	private static final ReviewSort[] TABS = { ReviewSort.NEWEST_REVIEW_FIRST, ReviewSort.HIGHEST_RATING_FIRST,
			ReviewSort.LOWEST_RATING_FIRST };

	private List<UserReviewsFragment> mFragments;

	public UserReviewsFragmentPagerAdapter(FragmentManager fm, Bundle savedInstanceState) {
		super(fm);

		mFragments = new ArrayList<UserReviewsFragment>(TABS.length);
		mFragments.add(null);
		mFragments.add(null);
		mFragments.add(null);

		if (savedInstanceState != null) {
			for (int i = 0; i <= 2; i++) {
				Fragment fragment = fm.getFragment(savedInstanceState, TAGS[i]);
				if (fragment != null) {
					mFragments.set(i, (UserReviewsFragment) fragment);
				}
			}
		}
	}

	public void onSaveInstanceState(FragmentManager fm, Bundle outState) {
		for (int i = 0; i <= 2; i++) {
			if (mFragments.get(i) != null) {
				fm.putFragment(outState, TAGS[i], mFragments.get(i));
			}
		}
	}

	@Override
	public Fragment getItem(int position) {
		if (mFragments.get(position) == null) {
			Property property = Db.getSelectedProperty();
			UserReviewsFragment fragment = UserReviewsFragment.newInstance(property, TABS[position]);
			mFragments.set(position, fragment);
		}
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return TABS.length;
	}

	// populate the list header for all three fragments
	public void populateReviewsStats() {
		for (UserReviewsFragment f : mFragments) {
			f.populateListHeader();
		}
		mFragments.get(0).attemptReviewsDownload();
	}

	public void cancelDownloads() {
		for (UserReviewsFragment f : mFragments) {
			f.cancelReviewsDownload();
		}
	}

	public void attemptNextDownload(UserReviewsFragment fragmentDone) {
		int indexStart = (mFragments.indexOf(fragmentDone) + 1) % TABS.length;
		mFragments.get(indexStart).attemptReviewsDownload();
	}
}
