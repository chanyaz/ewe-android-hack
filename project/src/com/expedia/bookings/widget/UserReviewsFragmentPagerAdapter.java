package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.CompatFragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;

public class UserReviewsFragmentPagerAdapter extends FragmentPagerAdapter {

	// Instance variable names
	private static final String INSTANCE_RECENT_REVIEWS_FRAGMENT = "INSTANCE_RECENT_REVIEWS_FRAGMENT";
	private static final String INSTANCE_FAVORABLE_REVIEWS_FRAGMENT = "INSTANCE_FAVORABLE_REVIEWS_FRAGMENT";
	private static final String INSTANCE_CRITICAL_REVIEWS_FRAGMENT = "INSTANCE_CRITICAL_REVIEWS_FRAGMENT";

	private List<UserReviewsFragment> mFragments;

	public UserReviewsFragmentPagerAdapter(FragmentManager fm, Bundle savedInstanceState) {
		super(fm);

		mFragments = new ArrayList<UserReviewsFragment>();

		if (savedInstanceState != null) {
			mFragments.add((UserReviewsFragment) fm.getFragment(savedInstanceState, INSTANCE_RECENT_REVIEWS_FRAGMENT));
			mFragments.add((UserReviewsFragment) fm
					.getFragment(savedInstanceState, INSTANCE_FAVORABLE_REVIEWS_FRAGMENT));
			mFragments
					.add((UserReviewsFragment) fm.getFragment(savedInstanceState, INSTANCE_CRITICAL_REVIEWS_FRAGMENT));
		}
		else {
			Property property = Db.getSelectedProperty();
			mFragments.add(UserReviewsFragment.newInstance(property, ReviewSort.NEWEST_REVIEW_FIRST));
			mFragments.add(UserReviewsFragment.newInstance(property, ReviewSort.HIGHEST_RATING_FIRST));
			mFragments.add(UserReviewsFragment.newInstance(property, ReviewSort.LOWEST_RATING_FIRST));
		}

	}

	public void onSaveInstanceState(FragmentManager fm, Bundle outState) {
		fm.putFragment(outState, INSTANCE_RECENT_REVIEWS_FRAGMENT, getItem(0));
		fm.putFragment(outState, INSTANCE_FAVORABLE_REVIEWS_FRAGMENT, getItem(1));
		fm.putFragment(outState, INSTANCE_CRITICAL_REVIEWS_FRAGMENT, getItem(2));
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
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
		int indexStart = (mFragments.indexOf(fragmentDone) + 1) % mFragments.size();
		mFragments.get(indexStart).attemptReviewsDownload();
	}
}
