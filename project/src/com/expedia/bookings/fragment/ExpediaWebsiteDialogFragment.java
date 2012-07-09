package com.expedia.bookings.fragment;

import com.expedia.bookings.utils.AboutUtils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class ExpediaWebsiteDialogFragment extends DialogFragment {
	public static ExpediaWebsiteDialogFragment newInstance() {
		return new ExpediaWebsiteDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AboutUtils aboutUtils = new AboutUtils(getActivity());
		return aboutUtils.createExpediaWebsiteDialog(null);
	}
}
