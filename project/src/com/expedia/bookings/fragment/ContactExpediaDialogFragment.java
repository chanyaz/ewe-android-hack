package com.expedia.bookings.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.expedia.bookings.utils.AboutUtils;

public class ContactExpediaDialogFragment extends DialogFragment {

	public static ContactExpediaDialogFragment newInstance() {
		return new ContactExpediaDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AboutUtils aboutUtils = new AboutUtils(getActivity());
		return aboutUtils.createContactExpediaDialog(null);
	}
}
