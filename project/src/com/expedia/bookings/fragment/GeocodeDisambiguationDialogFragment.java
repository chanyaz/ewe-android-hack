package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;

public class GeocodeDisambiguationDialogFragment extends DialogFragment {

	private static final String KEY_ADDRESSES = "addresses";

	public static GeocodeDisambiguationDialogFragment newInstance(List<Address> addresses) {
		GeocodeDisambiguationDialogFragment fragment = new GeocodeDisambiguationDialogFragment();
		fragment.setInitialState(addresses);
		return fragment;
	}

	private GeocodeDisambiguationDialogFragmentListener mListener;

	private List<Address> mAddresses;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, GeocodeDisambiguationDialogFragmentListener.class);
	}

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
				mListener.onLocationPicked(mAddresses.get(which));
			}
		});
		builder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mListener.onGeocodeDisambiguationFailure();
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

		mListener.onGeocodeDisambiguationFailure();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface GeocodeDisambiguationDialogFragmentListener {
		public void onLocationPicked(Address address);

		public void onGeocodeDisambiguationFailure();
	}
}
