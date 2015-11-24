package com.expedia.bookings.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.CheckoutLoginButtonsFragment;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.mobiata.android.util.Ui;

public abstract class TabletCheckoutDataFormFragment extends LobableFragment
	implements CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	public interface ICheckoutDataFormListener {
		void onFormRequestingClosure(TabletCheckoutDataFormFragment caller, boolean animate);
	}

	private ViewGroup mFormContentC;
	private TextView mHeadingText;
	private TextView mHeadingButton;
	private TextView mFormEntryMessageTv;
	private TextView mBoardingMessageTv;

	private ICheckoutDataFormListener mListener;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, ICheckoutDataFormListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		ViewGroup rootView = Ui.inflate(R.layout.fragment_tablet_checkout_data_form, container, false);
		mFormContentC = Ui.findView(rootView, R.id.content_container);
		mHeadingText = Ui.findView(rootView, R.id.header_tv);
		mHeadingButton = Ui.findView(rootView, R.id.header_text_button_tv);
		mFormEntryMessageTv = Ui.findView(rootView, R.id.form_entry_message_tv);
		mBoardingMessageTv = Ui.findView(rootView, R.id.header_name_match_message);

		if (showBoardingMessage()) {
			mBoardingMessageTv.setVisibility(View.VISIBLE);
		}

		setUpFormContent(mFormContentC);

		return rootView;
	}

	public TextView getFormEntryMessageTextView() {
		return mFormEntryMessageTv;
	}

	public void closeForm(boolean animate) {
		mListener.onFormRequestingClosure(this, animate);
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

	public void showNameMatchHeaderText(boolean show) {
		mHeadingText.setVisibility(show ? View.GONE : View.VISIBLE);
		mBoardingMessageTv.setVisibility(show ? View.VISIBLE : View.GONE);
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

	public abstract void setUpFormContent(ViewGroup formContainer);

	public abstract void onFormClosed();

	public abstract void onFormOpened();

	public abstract boolean showBoardingMessage();
}
