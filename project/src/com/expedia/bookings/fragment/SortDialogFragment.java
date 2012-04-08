package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Filter.Sort;

/**
 * This is a temporary Fragment designed for testing sorting with the tablet version of the app.
 * 
 * At some point we will need to:
 * 
 * 1. Make this a better-placed popup.
 * 2. Unify the sort options with PhoneSearchActivity
 */
public class SortDialogFragment extends DialogFragment {

	private static final String ARG_SHOW_DISTANCES = "ARG_SHOW_DISTANCES";

	private SortDialogFragmentListener mListener;

	public static SortDialogFragment newInstance(boolean showDistances) {
		SortDialogFragment fragment = new SortDialogFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_SHOW_DISTANCES, showDistances);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof SortDialogFragmentListener)) {
			throw new RuntimeException("SortDialogFragment Activity must implement SortDialogFragmentListener!");
		}

		mListener = (SortDialogFragmentListener) activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		List<CharSequence> items = new ArrayList<CharSequence>();
		items.add(getString(R.string.sort_description_popular));
		items.add(getString(R.string.sort_description_price));
		items.add(getString(R.string.sort_description_rating));

		if (getArguments().getBoolean(ARG_SHOW_DISTANCES)) {
			items.add(getString(R.string.sort_description_distance));
		}

		builder.setItems(items.toArray(new CharSequence[0]), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Sort newSort = Sort.POPULAR; // Default to popular
				switch (which) {
				case 0:
					newSort = Sort.POPULAR;
					break;
				case 1:
					newSort = Sort.PRICE;
					break;
				case 2:
					newSort = Sort.RATING;
					break;
				case 3:
					newSort = Sort.DISTANCE;
					break;
				}

				mListener.onSortChanged(newSort);
			}
		});

		return builder.create();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SortDialogFragmentListener {
		public void onSortChanged(Sort newSort);
	}
}
