package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListPopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.section.TravelerAutoCompleteAdapter;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.TravelerUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TravelerButton extends LinearLayout {

	public TravelerButton(Context context) {
		super(context);
	}

	public TravelerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TravelerButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private static final String DL_FETCH_TRAVELER_INFO = "DL_FETCH_TRAVELER_INFO";
	private static final String FTAG_FETCH_TRAVELER_INFO = "FTAG_FETCH_TRAVELER_INFO";

	private TravelerAutoCompleteAdapter mTravelerAdapter;
	private ListPopupWindow mStoredTravelerPopup;
	private Traveler mTraveler;
	private ITravelerButtonListener mTravelerButtonListener;

	public interface ITravelerButtonListener {
		public void onTravelerChosen(Traveler traveler);
		public void onAddNewTravelerSelected();
	}

	@InjectView(R.id.select_traveler_button)
	Button selectTraveler;

	@OnClick(R.id.select_traveler_button)
	public void onShowTraveler() {
		showSavedTravelers();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.checkout_traveler_button, this);
		ButterKnife.inject(this);
		mTravelerAdapter = new TravelerAutoCompleteAdapter(getContext(), false, R.drawable.car_driver_checkout_circle);
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (dl.isDownloading(getTravelerDownloadKey())) {
			dl.registerDownloadCallback(getTravelerDownloadKey(), mTravelerDetailsCallback);
		}
	}

	public void setTravelButtonListener(ITravelerButtonListener listener) {
		mTravelerButtonListener = listener;
	}

	private void onStoredTravelerSelected(int position) {
		// Todo - Commenting this code temporarily. Since for cars MVP we don't support "Add new Driver/Traveler" option. When we do want to add it back just uncomment this.
		/*if (position == mTravelerAdapter.getCount() - 1) {
			if (mTravelerButtonListener != null) {
				mTravelerButtonListener.onAddNewTravelerSelected();
			}
			mStoredTravelerPopup.dismiss();
			return;
		}
		else if (position == 0) {
			return;
		}*/

		// If adapter header do nothing.
		if (position == 0) {
			return;
		}
		mTraveler = mTravelerAdapter.getItem(position);
		if (mTraveler.isSelectable()) {
			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			if (dl.isDownloading(getTravelerDownloadKey())) {
				dl.cancelDownload(getTravelerDownloadKey());
			}

			// Begin loading flight details in the background, if we haven't already
			// Show a loading dialog
			ThrobberDialog df = ThrobberDialog
				.newInstance(getResources().getString(R.string.loading_traveler_info));
			df.show(((ActionBarActivity) getContext()).getSupportFragmentManager(), FTAG_FETCH_TRAVELER_INFO);
			dl.startDownload(getTravelerDownloadKey(), mTravelerDetailsDownload,
				mTravelerDetailsCallback);
			mStoredTravelerPopup.dismiss();
		}

	}

	private void showSavedTravelers() {
		if (mStoredTravelerPopup == null) {
			mStoredTravelerPopup = new ListPopupWindow(getContext());
			mStoredTravelerPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onStoredTravelerSelected(position);
				}
			});
		}
		mStoredTravelerPopup.setAnchorView(selectTraveler);
		mStoredTravelerPopup.setAdapter(mTravelerAdapter);
		mStoredTravelerPopup.show();
	}

	private BackgroundDownloader.Download<SignInResponse> mTravelerDetailsDownload = new BackgroundDownloader.Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getContext());
			BackgroundDownloader.getInstance().addDownloadListener(getTravelerDownloadKey(), services);
			return services.travelerDetails(mTraveler, 0);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {

			ThrobberDialog df = (ThrobberDialog) ((ActionBarActivity) getContext()).getSupportFragmentManager()
				.findFragmentByTag(FTAG_FETCH_TRAVELER_INFO);
			if (df != null) {
				df.dismiss();
			}

			if (results == null || results.hasErrors()) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
					getResources().getString(R.string.unable_to_load_traveler_message));
				dialogFragment.show(((ActionBarActivity) getContext()).getSupportFragmentManager(), "errorFragment");
				if (results != null && results.hasErrors()) {
					String error = results.getErrors().get(0).getPresentableMessage(getContext());
					Log.e("Traveler Details Error:" + error);
				}
				else {
					Log.e("Traveler Details Results == null!");
				}
			}
			else {
				Traveler selectedTraveler = results.getTraveler();
				Traveler previousTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
				Db.getWorkingTravelerManager().shiftWorkingTraveler(selectedTraveler);
				TravelerUtils.resetPreviousTravelerSelectState(previousTraveler);
				selectTraveler.setText(selectedTraveler.getFullName());
				if (mTravelerButtonListener != null) {
					mTravelerButtonListener.onTravelerChosen(selectedTraveler);
				}
			}
		}
	};

	private String getTravelerDownloadKey() {
		return DL_FETCH_TRAVELER_INFO + (mTraveler != null ? mTraveler.getTuid().toString() : "");
	}

	public void dismissPopup() {
		if (mStoredTravelerPopup != null) {
			mStoredTravelerPopup.dismiss();
		}
	}
}
