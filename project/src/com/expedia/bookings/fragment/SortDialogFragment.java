package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Filter;
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

	public static SortDialogFragment newInstance() {
		return new SortDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		List<CharSequence> items = new ArrayList<CharSequence>();
		items.add(getString(R.string.sort_description_popular));
		items.add(getString(R.string.sort_description_price));
		items.add(getString(R.string.sort_description_rating));

		if (((SearchResultsFragmentActivity) getActivity()).mInstance.mShowDistance) {
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

				Filter filter = ((SearchResultsFragmentActivity) getActivity()).mInstance.mFilter;
				filter.setSort(newSort);
				filter.notifyFilterChanged();
			}
		});

		return builder.create();
	}
}
