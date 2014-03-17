package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsListLoadingFragment extends Fragment {

	private final static String STATE_LOADING_TEXT = "STATE_LOADING_TEXT";

	private View mRootC;
	private TextView mLoadingTv;

	//loading anim vars
	private int mLoadingUpdateInterval = 250;
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;
	private Runnable mLoadingAnimRunner;
	private ViewGroup mLoadingC;

	private int mLoadingNumber = 0;
	private boolean mLoadingMovingUp = false;

	private String mLoadingText;

	public static ResultsListLoadingFragment newInstance(String loadingText) {
		ResultsListLoadingFragment frag = new ResultsListLoadingFragment();
		frag.setLoadingText(loadingText);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = inflater.inflate(R.layout.fragment_results_list_loading, null);
		mLoadingTv = Ui.findView(mRootC, R.id.loading_tv);
		mLoadingC = Ui.findView(mRootC, R.id.loading_bars_container);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOADING_TEXT)) {
				setLoadingText(savedInstanceState.getString(STATE_LOADING_TEXT));
			}
		}

		if(mLoadingText != null){
			setLoadingText(mLoadingText);
		}

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		setLoadingAnimationEnabled(true);
	}

	@Override
	public void onPause() {
		setLoadingAnimationEnabled(false);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mLoadingText != null) {
			outState.putString(STATE_LOADING_TEXT, mLoadingText);
		}
	}

	public void setLoadingText(String text) {
		mLoadingText = text;
		if (mLoadingTv != null) {
			mLoadingTv.setText(mLoadingText);
		}
	}

	private void setLoadingAnimationEnabled(boolean loading) {
		if (!loading) {
			mLoadingAnimRunner = null;
		}
		else if (mRootC != null) {
			mLoadingAnimRunner = new Runnable() {
				@Override
				public void run() {
					if (this == mLoadingAnimRunner && mRootC != null && getActivity() != null) {
						loadingAnimUpdate();
						mRootC.postDelayed(this, mLoadingUpdateInterval);
					}
				}
			};
			mRootC.post(mLoadingAnimRunner);
		}
	}

	private void loadingAnimUpdate() {
		if (mLoadingC != null) {
			if (mLoadingMovingUp) {
				if (mLoadingNumber <= 0) {
					mLoadingMovingUp = false;
					loadingAnimUpdate();
					return;
				}
				else {
					mLoadingNumber--;
				}
			}
			else {
				if (mLoadingC.getChildCount() > 0 && mLoadingNumber >= (mLoadingC.getChildCount() - 1)) {
					mLoadingMovingUp = true;
					loadingAnimUpdate();
					return;
				}
				else {
					mLoadingNumber++;
				}
			}

			for (int i = 0; i < mLoadingC.getChildCount(); i++) {
				mLoadingC.getChildAt(i)
					.setBackgroundColor(i == mLoadingNumber ? mLoadingColorDark : mLoadingColorLight);
			}
		}
	}
}
