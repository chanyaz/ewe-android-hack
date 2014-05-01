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
import com.expedia.bookings.fragment.CheckoutLoginButtonsFragment;
import com.expedia.bookings.widget.SlidingRadioGroup;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class TabletCheckoutDataFormFragment extends LobableFragment
	implements CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	private ViewGroup mRootC;
	private ViewGroup mFormContentC;
	private TextView mHeadingText;
	private TextView mHeadingButton;
	private SlidingRadioGroup mSaveRadioGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getLob() == null) {
			throw new RuntimeException("We should always have an LOB by the time onCreateView is being called.");
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(mRootC, R.id.content_container);
		mHeadingText = Ui.findView(mRootC, R.id.header_tv);
		mHeadingButton = Ui.findView(mRootC, R.id.header_text_button_tv);
		mSaveRadioGroup = Ui.findView(mRootC, R.id.save_button_group);

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

	public TextView getHeadingTextView() {
		return mHeadingText;
	}

	public TextView getHeadingButtonTextView() {
		return mHeadingButton;
	}

	public SlidingRadioGroup getSaveRadioGroup() {
		return mSaveRadioGroup;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mFormContentC != null) {
			setUpFormContent(mFormContentC);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// CheckoutLoginButtonsFragment.ILoginStateChangedListener
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onLoginStateChanged() {
		setUpFormContent(mFormContentC);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Abstract methods to be implemented by concrete children
	///////////////////////////////////////////////////////////////////////////////////////////////

	protected abstract void setUpFormContent(ViewGroup formContainer);

	protected abstract void onFormClosed();

	protected abstract void onFormOpened();
}
