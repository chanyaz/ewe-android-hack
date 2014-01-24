package com.expedia.bookings.fragment.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

/**
 * TripBucketItemFragment: Tablet 2014
 */
public abstract class TripBucketItemFragment extends Fragment {

	//Views
	private ViewGroup mRootC;
	private ViewGroup mTopC;
	private ViewGroup mExpandedC;
	private Button mBookBtn;

	//Colors
	private int mExpandedBgColor = Color.WHITE;
	private int mCollapsedBgColor = Color.TRANSPARENT;

	//Misc
	private boolean mShowButton = true;
	private boolean mExpanded = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_item, null);
		mTopC = Ui.findView(mRootC, R.id.trip_bucket_item_top_container);
		mExpandedC = Ui.findView(mRootC, R.id.trip_bucket_item_expanded_container);
		mBookBtn = Ui.findView(mRootC, R.id.checkout_button);
		FontCache.setTypeface(mBookBtn, Font.ROBOTO_MEDIUM);

		addTopView(inflater, mTopC);

		bind();

		return mRootC;
	}

	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
		updateVisibilities();
	}

	public boolean getExpanded() {
		return mExpanded;
	}

	public void setShowButton(boolean showButton) {
		mShowButton = showButton;
		updateVisibilities();
	}

	public boolean getShowButton() {
		return mShowButton;
	}

	public void bind() {
		if (mRootC != null) {
			updateVisibilities();

			mBookBtn.setText(getBookButtonText());
			mBookBtn.setOnClickListener(getOnBookClickListener());

			doBind();
		}
	}

	private void updateVisibilities() {
		if (mRootC != null) {
			if (mExpanded) {
				int padding = (int) getResources().getDimension(R.dimen.trip_bucket_expanded_card_padding);
				mRootC.setBackgroundColor(mExpandedBgColor);
				mRootC.setPadding(padding, padding, padding, padding);
				mExpandedC.removeAllViews();
				addExpandedView(getLayoutInflater(null), mExpandedC);
				mBookBtn.setVisibility(View.GONE);
				mExpandedC.setVisibility(View.VISIBLE);

			}
			else {
				int padding = 0;
				mRootC.setBackgroundColor(mCollapsedBgColor);
				mRootC.setPadding(padding, padding, padding, padding);
				mExpandedC.removeAllViews();
				mBookBtn.setVisibility(mShowButton ? View.VISIBLE : View.GONE);
				mExpandedC.setVisibility(View.GONE);
			}
		}
	}

	protected abstract void doBind();

	public abstract CharSequence getBookButtonText();

	public abstract void addTopView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract OnClickListener getOnBookClickListener();
}
