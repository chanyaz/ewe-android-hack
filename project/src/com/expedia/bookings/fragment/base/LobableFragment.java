package com.expedia.bookings.fragment.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.interfaces.ILOBable;

/*
 * This fragment implements ILOBable, and saves the LOB on rotation.
 * 
 * We are deloping many fragment that depend on LOB for tablet checkout 2014, so it seems reasonable to abstract this stuff.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class LobableFragment extends Fragment implements ILOBable {

	private static final String STATE_LOB = "STATE_LOB";
	private LineOfBusiness mLob;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_LOB)) {
				LineOfBusiness lob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_LOB));
				if (lob != null) {
					setLob(lob);
				}
			}
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_LOB, mLob.name());
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
