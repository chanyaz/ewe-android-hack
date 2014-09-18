package com.expedia.bookings.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.expedia.bookings.utils.AboutUtils;

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
