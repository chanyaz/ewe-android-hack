package com.expedia.bookings.fragment.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class TabletCheckoutDataFormFragment extends LobableFragment {

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private TextView mTopLeftHeadingText;
	private TextView mTopRightButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//We do this to do the lob state restoration (but our super classes probably have no view...)
		super.onCreateView(inflater, container, savedInstanceState);

		if (getLob() == null) {
			throw new RuntimeException("We should always have an LOB by the time onCreateView is being called.");
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);
		mTopLeftHeadingText = Ui.findView(mRootC, R.id.header_tv);
		mTopRightButton = Ui.findView(mRootC, R.id.top_right_text_button);

		setUpFormContent(mFormContentC);

		return mRootC;
	}

	public void setTopLeftText(CharSequence seq) {
		if (mTopLeftHeadingText != null) {
			mTopLeftHeadingText.setText(seq);
		}
	}

	public void setTopRightText(CharSequence seq) {
		if (mTopRightButton != null) {
			mTopRightButton.setText(seq);
		}
	}

	public void setTopRightTextOnClick(OnClickListener listener) {
		if (mTopRightButton != null) {
			mTopRightButton.setOnClickListener(listener);
		}
	}

	public TextView getTopLeftTextView() {
		return mTopLeftHeadingText;
	}

	public TextView getTopRightButton() {
		return mTopRightButton;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mFormContentC != null) {
			setUpFormContent(mFormContentC);
		}
	}

	protected abstract void setUpFormContent(ViewGroup formContainer);
}
