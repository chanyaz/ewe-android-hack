package com.expedia.bookings.fragment.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class TabletCheckoutDataFormFragment extends Fragment {

	private static final String STATE_LOB = "STATE_LOB";

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private TextView mTopLeftHeadingText;
	private TextView mTopRightButton;

	private LineOfBusiness mLob;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOB)) {
				LineOfBusiness lob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_LOB));
				if (lob != null) {
					setLob(lob);
				}
			}
		}

		if (mLob == null) {
			throw new RuntimeException("We should always have an LOB by the time onCreateView is being called.");
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);
		mTopLeftHeadingText = Ui.findView(mRootC, R.id.header_tv);
		mTopRightButton = Ui.findView(mRootC, R.id.top_right_text_button);

		setUpFormContent(mFormContentC);

		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LOB, mLob.name());
	}

	public void setLob(LineOfBusiness lob) {
		if (lob != mLob) {
			mLob = lob;
			if (mFormContentC != null) {
				setUpFormContent(mFormContentC);
			}
		}
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	public void setTopLeftText(CharSequence seq) {
		mTopLeftHeadingText.setText(seq);
	}

	public void setTopRightText(CharSequence seq) {
		mTopRightButton.setText(seq);
	}

	public void setTopRightTextOnClick(OnClickListener listener) {
		mTopRightButton.setOnClickListener(listener);
	}

	public TextView getTopLeftTextView() {
		return mTopLeftHeadingText;
	}

	public TextView getTopRightButton() {
		return mTopRightButton;
	}

	protected abstract void setUpFormContent(ViewGroup formContainer);
}
