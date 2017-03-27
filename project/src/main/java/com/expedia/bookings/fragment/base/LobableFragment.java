package com.expedia.bookings.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.interfaces.ILOBable;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.Ui;

/*
 * This fragment implements ILOBable, and saves the LOB on rotation.
 * 
 * We are deloping many fragment that depend on LOB for tablet checkout 2014, so it seems reasonable to abstract this stuff.
 */
public abstract class LobableFragment extends Fragment implements ILOBable {

	private static final String STATE_LOB = "STATE_LOB";
	private LineOfBusiness mLob;

	protected UserStateManager userStateManager;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOB)) {
				LineOfBusiness lob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_LOB));
				if (lob != null) {
					setLob(lob);
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mLob != null) {
			outState.putString(STATE_LOB, mLob.name());
		}
	}

	public void setLob(LineOfBusiness lob) {
		if (lob != mLob) {
			mLob = lob;
			onLobSet(lob);
		}
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	public abstract void onLobSet(LineOfBusiness lob);
}
