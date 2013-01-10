package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.ItinItemAdapter;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.util.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ItinItemListFragment extends ListFragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	private AccountButton mAccountButton;

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_itinerary_list, null);

		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountButton.setListener(this);

		this.setListAdapter(new ItinItemAdapter(this.getActivity()));
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshAccountButtonState();

		this.getListView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				int first = arg0.getFirstVisiblePosition();
				int last = Math.min(arg0.getLastVisiblePosition() + 1, arg0.getChildCount());
				for (int i = first; i < last && i >= 0 && i < arg0.getChildCount(); i++) {
					arg0.getChildAt(i).invalidate();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {

				mAccountButton.bind(false, true, Db.getUser(), true);
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, true);
			}
		}
		else {
			mAccountButton.bind(false, false, null, true);
		}
	}

	@Override
	public void accountLoginClicked() {
		Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
		loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, PathMode.FLIGHTS.name());
		startActivity(loginIntent);
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
		df.setDoLogoutListener(this);
		df.show(this.getFragmentManager(), ConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null, true);
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);

	}
}
