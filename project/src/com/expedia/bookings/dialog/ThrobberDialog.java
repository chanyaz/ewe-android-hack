package com.expedia.bookings.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

import com.mobiata.android.Log;

public class ThrobberDialog extends DialogFragment {
	private ViewGroup mRoot;
	private CharSequence mMessage;

	public ThrobberDialog() {
		setCancelable(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO - proper style
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.SocialMessageChooserDialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRoot = (ViewGroup) inflater.inflate(R.layout.dialog_throbber, container, false);

		TextView messageView = Ui.findView(mRoot, R.id.message);
		return mRoot;
	}

	public void setMessage(CharSequence message) {
		mMessage = message;
	}

}
