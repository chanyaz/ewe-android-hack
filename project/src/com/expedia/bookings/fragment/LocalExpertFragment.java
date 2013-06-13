package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.R;

import com.expedia.bookings.data.LocalExpertSite;

public class LocalExpertFragment extends Fragment {

	public static final String TAG = LocalExpertFragment.class.getName();

	private static final String ARG_SITE = "ARG_SITE";

	private LocalExpertSite mSite;

	public static LocalExpertFragment newInstance(LocalExpertSite site) {
		LocalExpertFragment fragment = new LocalExpertFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_SITE, site);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSite = getArguments().getParcelable(ARG_SITE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View  view = inflater.inflate(R.layout.fragment_local_expert, container, false);

		return view;
	}
}
