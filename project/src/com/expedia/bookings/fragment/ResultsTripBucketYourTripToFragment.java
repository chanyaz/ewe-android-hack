package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketYourTripToFragment extends Fragment {

	public static ResultsTripBucketYourTripToFragment newInstance() {
		ResultsTripBucketYourTripToFragment frag = new ResultsTripBucketYourTripToFragment();
		return frag;
	}

	private boolean mRunBind = false;

	private ViewGroup mRootC;
	private TextView mTripToHeaderTv;
	private TextView mPrimaryDestTv;
	private TextView mSecondaryDestTv;
	private TextView mEmptyTripTv;
	private View mLeftDivider;
	private View mRightDivider;
	private LinearLayout mTripToLayout;
	private LinearLayout mEmptyTripLayout;
	private boolean isResized;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		isResized = false;
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_your_trip_to, null);
		mTripToHeaderTv = Ui.findView(mRootC, R.id.top_header_text);
		mLeftDivider = Ui.findView(mRootC, R.id.left_divider);
		mRightDivider = Ui.findView(mRootC, R.id.right_divider);
		mPrimaryDestTv = Ui.findView(mRootC, R.id.primary_destination_text);
		mSecondaryDestTv = Ui.findView(mRootC, R.id.secondary_destination_text);
		mEmptyTripTv = Ui.findView(mRootC, R.id.trip_empty_text);
		mTripToLayout = Ui.findView(mRootC, R.id.trip_to_layout);
		mEmptyTripLayout = Ui.findView(mRootC, R.id.empty_trip_container);

		FontCache.setTypeface(mTripToHeaderTv, Font.ROBOTOSLAB_LIGHT);
		FontCache.setTypeface(mPrimaryDestTv, Font.ROBOTOSLAB_BOLD);
		FontCache.setTypeface(mSecondaryDestTv, Font.ROBOTOSLAB_BOLD);
		FontCache.setTypeface(mEmptyTripTv, Font.ROBOTO_LIGHT);

		if (mRunBind) {
			bindToDb();
		}
		return mRootC;
	}

	public void bindToDb() {
		if (mPrimaryDestTv != null) {

			String city = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity();
			String country = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCountryCode();

			mPrimaryDestTv.setText(city);
			mSecondaryDestTv.setText(country);
			if (!isResized) {
				resizeDividers();
			}

			mRunBind = false;
		}
		else {
			mRunBind = true;
		}
	}

	/**
	* This method resizes the divider lines on either sides of "Trip To" text
	* to be as wide as the longest of the two strings. i.e. either cityName or countryName
	*/
	private void resizeDividers() {
		int priDestWidth = mPrimaryDestTv.getMeasuredWidth();
		int secDestWidth = mSecondaryDestTv.getMeasuredWidth();
		int requiredWidth = priDestWidth > secDestWidth ? priDestWidth : secDestWidth;

		int tripToWidth = mTripToLayout.getMeasuredWidth();

		int delta = requiredWidth - tripToWidth;
		if (delta <= 1 && (priDestWidth != 0 || secDestWidth != 0)) {
			isResized = true;
		}
		else {
			int eachDividerWidth = delta / 2;
			float dp = eachDividerWidth / getResources().getDisplayMetrics().density;

			LayoutParams leftDividerParams = mLeftDivider.getLayoutParams();
			leftDividerParams.width = (int) dp;
			mLeftDivider.setLayoutParams(leftDividerParams);

			LayoutParams rightDividerParams = mRightDivider.getLayoutParams();
			rightDividerParams.width = (int) dp;
			mRightDivider.setLayoutParams(rightDividerParams);
		}
	}

	public void hideEmptyTripContainer(boolean hide) {
		if (hide) {
			mEmptyTripLayout.setVisibility(View.GONE);
			mSecondaryDestTv.setVisibility(View.GONE);
		}
		else {
			mEmptyTripLayout.setVisibility(View.VISIBLE);
			mSecondaryDestTv.setVisibility(View.VISIBLE);
		}
	}
}
