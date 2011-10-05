package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.utils.StrUtils;

public class GeocodeDisambiguationDialogFragment extends DialogFragment {

	private static final String KEY_ADDRESSES = "addresses";

	public static GeocodeDisambiguationDialogFragment newInstance(List<Address> addresses) {
		GeocodeDisambiguationDialogFragment fragment = new GeocodeDisambiguationDialogFragment();
		fragment.setInitialState(addresses);
		return fragment;
	}

	private List<Address> mAddresses;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		if (savedInstanceState != null) {
			mAddresses = savedInstanceState.getParcelableArrayList(KEY_ADDRESSES);
		}

		CharSequence[] freeformLocations = StrUtils.formatAddresses(mAddresses);

		builder.setTitle(R.string.ChooseLocation);
		builder.setItems(freeformLocations, new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				((TabletActivity) getActivity()).onGeocodeSuccess(mAddresses.get(which));
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				((TabletActivity) getActivity()).onGeocodeFailure();
			}
		});

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ArrayList<Address> tmp = new ArrayList<Address>(mAddresses);
		outState.putParcelableArrayList(KEY_ADDRESSES, tmp);
	}

	private void setInitialState(List<Address> addresses) {
		mAddresses = addresses;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		((TabletActivity) getActivity()).onGeocodeFailure();
	}
}
