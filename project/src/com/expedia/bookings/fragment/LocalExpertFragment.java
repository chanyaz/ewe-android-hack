package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.R;

public class LocalExpertFragment extends Fragment {

	public static LocalExpertFragment newInstance() {
		return new LocalExpertFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View  view = inflater.inflate(R.layout.fragment_local_expert, container, false);

		return view;
	}
}