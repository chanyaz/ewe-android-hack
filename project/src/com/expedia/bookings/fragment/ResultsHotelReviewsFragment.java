package com.expedia.bookings.fragment;

import java.util.Set;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.UserReviewsFragmentPagerAdapter;
import com.mobiata.android.widget.SegmentedControlGroup;

public class ResultsHotelReviewsFragment extends Fragment implements UserReviewsFragmentListener,
		OnPageChangeListener, OnCheckedChangeListener {

	public static ResultsHotelReviewsFragment newInstance() {
		ResultsHotelReviewsFragment frag = new ResultsHotelReviewsFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private ViewPager mViewPager;
	private SegmentedControlGroup mSortGroup;
	private TextView mReviewSectionTitle;

	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_reviews, null);
		mViewPager = Ui.findView(mRootC, R.id.pager);
		mReviewSectionTitle = Ui.findView(mRootC, R.id.reviews_title);
		mReviewSectionTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		mPagerAdapter = new UserReviewsFragmentPagerAdapter(getChildFragmentManager(), savedInstanceState);

		// Pager
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		// Tabs
		mSortGroup = Ui.findView(mRootC, R.id.user_review_sort_group);
		mSortGroup.setOnCheckedChangeListener(this);
		int position = mViewPager.getCurrentItem();
		((RadioButton) mSortGroup.getChildAt(position)).setChecked(true);

		mRootC.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				int paddingx = (int) (mRootC.getWidth() * 0.14f);
				mRootC.setPadding(paddingx, mRootC.getPaddingTop(), paddingx, mRootC.getPaddingBottom());
			}
		});

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null) {
			Property property = Db.getHotelSearch().getSelectedProperty();
			bind(property);
		}
	}

	private void bind(Property property) {
		mReviewSectionTitle.setText(getString(R.string.reviews_for_TEMPLATE, property.getName()));
		mPagerAdapter.bind();
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnPageChangeListener

	@Override
	public void onPageScrollStateChanged(int state) {
		// nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing to do here
	}

	@Override
	public void onPageSelected(int position) {
		((RadioButton) mSortGroup.getChildAt(position)).setChecked(true);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnCheckedChangeListener

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		String referrerId = null;
		int position = 0;
		switch (checkedId) {
			case R.id.user_review_button_recent:
				referrerId = "App.Hotels.Reviews.Sort.Recent";
				position = 0;
				break;
			case R.id.user_review_button_favorable:
				referrerId = "App.Hotels.Reviews.Sort.Favorable";
				position = 1;
				break;
			case R.id.user_review_button_critical:
				referrerId = "App.Hotels.Reviews.Sort.Critical";
				position = 2;
				break;
		}

		mViewPager.setCurrentItem(position);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FragmentListener

	@Override
	public void onUserReviewsFragmentReady(UserReviewsFragment frag) {
		frag.bind();
	}

	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		// TODO: Omniture tracking
	}
}
