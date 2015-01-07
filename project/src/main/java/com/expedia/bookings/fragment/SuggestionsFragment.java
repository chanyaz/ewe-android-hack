package com.expedia.bookings.fragment;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.widget.SuggestionsAdapter;
import com.mobiata.android.util.Ui;

public class SuggestionsFragment extends ListFragment {

	private SuggestionsFragmentListener mListener;

	private SuggestionsAdapter mAdapter;

	// Sometimes we want to prep text to filter before start; by default
	// we start with a blank query (to kick off the defaults)
	private CharSequence mTextToFilter = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, SuggestionsFragmentListener.class);

		activity.getContentResolver().registerContentObserver(
			SuggestionProvider.getContentFilterUri(getActivity()), true,
			new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestions, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (mAdapter == null) {
			mAdapter = new SuggestionsAdapter(getActivity());
		}

		setListAdapter(mAdapter);

		filter(mTextToFilter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		SuggestionV2 suggestion = mAdapter.getSuggestion(position);

		SuggestionProvider.addSuggestionToRecents(getActivity(), suggestion);

		mListener.onSuggestionClicked(this, suggestion);
	}

	public void filter(CharSequence text) {
		if (text == null) {
			text = "";
		}

		mTextToFilter = text;

		if (getView() != null) {
			mAdapter.getFilter().filter(text);
		}
	}

	public SuggestionV2 getBestChoiceForFilter() {
		if (mAdapter != null && mAdapter.getCount() > 0) {
			return mAdapter.getSuggestion(0);
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SuggestionsFragmentListener {
		public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion);
	}
}
