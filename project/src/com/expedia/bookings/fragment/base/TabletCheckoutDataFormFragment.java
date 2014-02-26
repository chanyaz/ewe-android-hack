package com.expedia.bookings.fragment.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class TabletCheckoutDataFormFragment extends LobableFragment {

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private TextView mHeadingText;
	private TextView mHeadingButton;
	private Spinner mHeadingSpinner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getLob() == null) {
			throw new RuntimeException("We should always have an LOB by the time onCreateView is being called.");
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);
		mHeadingText = Ui.findView(mRootC, R.id.header_tv);
		mHeadingButton = Ui.findView(mRootC, R.id.header_text_button_tv);
		mHeadingSpinner = Ui.findView(mRootC, R.id.header_spinner);

		setUpFormContent(mFormContentC);

		return mRootC;
	}

	public void setHeadingText(CharSequence seq) {
		if (mHeadingText != null) {
			mHeadingText.setText(seq);
		}
	}

	public void setHeadingButtonText(CharSequence seq) {
		if (mHeadingButton != null) {
			mHeadingButton.setText(seq);
		}
	}

	public void setHeadingButtonOnClick(OnClickListener listener) {
		if (mHeadingButton != null) {
			mHeadingButton.setOnClickListener(listener);
		}
	}

	public Spinner getHeadingSpinner() {
		return mHeadingSpinner;
	}

	public TextView getHeadingTextView() {
		return mHeadingText;
	}

	public TextView getHeadingButtonTextView() {
		return mHeadingButton;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mFormContentC != null) {
			setUpFormContent(mFormContentC);
		}
	}

	protected abstract void setUpFormContent(ViewGroup formContainer);

	protected abstract void onFormClosed();
}
