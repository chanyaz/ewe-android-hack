package com.expedia.bookings.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.dialog.CouponDialogFragment;
import com.expedia.bookings.dialog.CouponDialogFragment.CouponDialogFragmentListener;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.HotelBookingFragment.HotelBookingState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.squareup.otto.Subscribe;

public class CheckoutCouponFragment extends LobableFragment implements OnClickListener, CouponDialogFragmentListener {

	private TextView mCouponTextButton;
	private ViewGroup mCouponAppliedContainer;
	private TextView mCouponSavedTextView;
	private View mCouponRemoveView;

	private CouponDialogFragment mCouponDialogFragment;
	private ThrobberDialog mCouponRemoveThrobberDialog;
	private ThrobberDialog mGoogleWalletCouponApplyThrobber;
	private HotelBookingFragment mHotelBookingFragment;

	private boolean mIsCouponBeingReplaced = false;

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	public static CheckoutCouponFragment newInstance(LineOfBusiness lob) {
		CheckoutCouponFragment frag = new CheckoutCouponFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHotelBookingFragment = Ui.findSupportFragment((FragmentActivity) getActivity(), HotelBookingFragment.TAG);

		if (mHotelBookingFragment == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			mHotelBookingFragment = new HotelBookingFragment();
			ft.add(mHotelBookingFragment, HotelBookingFragment.TAG);
			ft.commit();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.include_tablet_coupon_container, null);

		mCouponTextButton = Ui.findView(view, R.id.coupon_button);
		mCouponTextButton.setOnClickListener(this);
		mCouponTextButton.setPaintFlags(mCouponTextButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		mCouponAppliedContainer = Ui.findView(view, R.id.coupon_applied_container);
		mCouponSavedTextView = Ui.findView(view, R.id.coupon_saved_text_view);
		mCouponRemoveView = Ui.findView(view, R.id.coupon_clear);
		mCouponRemoveView.setOnClickListener(this);

		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		mFragmentModLock.setSafe(true);
		Events.register(this);
		updateViews();
		updateViewVisibilities();
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mFragmentModLock.setSafe(false);
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.coupon_button: {
			OmnitureTracking.trackHotelCouponExpand();
			mCouponDialogFragment = new CouponDialogFragment();
			mCouponDialogFragment.show(getChildFragmentManager(), CouponDialogFragment.TAG);
			OmnitureTracking.trackHotelCouponExpand();
			break;
		}
		case R.id.coupon_clear: {
			OmnitureTracking.trackHotelCouponRemoved();
			clearCoupon();
			break;
		}
		}

	}

	private static final String COUPON_REMOVE_THROBBER = "COUPON_REMOVE_THROBBER";

	public void clearCoupon() {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				if (mCouponRemoveThrobberDialog == null) {
					mCouponRemoveThrobberDialog = ThrobberDialog.newInstance(getString(R.string.coupon_removing_dialog));
					mCouponRemoveThrobberDialog.setCancelable(false);
					mCouponRemoveThrobberDialog.show(getFragmentManager(), COUPON_REMOVE_THROBBER);
				}
			}
		});

		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_REMOVE);
	}

	public void onReplaceCoupon(String couponCode, boolean showReplaceWarning) {
		if (!mIsCouponBeingReplaced) {
			Log.d("CheckoutCouponFragment.onReplaceCoupon(" + couponCode + ")");
			mIsCouponBeingReplaced = true;
			if (WalletUtils.isCouponWalletCoupon(couponCode)) {
				showGoogleWalletCouponLoadingThrobber();
				if (showReplaceWarning) {
					showReplacingWithWalletCouponDialog();
				}
			}
			mHotelBookingFragment.startDownload(HotelBookingState.COUPON_REPLACE, couponCode);
		}
	}

	public void showReplacingWithWalletCouponDialog() {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				Fragment frag = getFragmentManager().findFragmentByTag("WALLET_REPLACE_DIALOG");
				if (isResumed() && frag == null) {
					SimpleDialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_replaced_message));
					df.show(getFragmentManager(), "WALLET_REPLACE_DIALOG");
				}
			}
		});
	}

	private static final String GW_LOADING_THROBBER = "GW_LOADING_THROBBER";

	public void showGoogleWalletCouponLoadingThrobber() {
			mFragmentModLock.runWhenSafe(new Runnable() {
				@Override
				public void run() {
					if (mGoogleWalletCouponApplyThrobber == null) {
						mGoogleWalletCouponApplyThrobber = ThrobberDialog.newInstance(getString(R.string.wallet_promo_applying));
						mGoogleWalletCouponApplyThrobber.setCancelable(false);
						mGoogleWalletCouponApplyThrobber.show(getFragmentManager(), GW_LOADING_THROBBER);
					}
				}
			});
	}

	@Override
	public void onApplyCoupon(String couponCode) {
		if (WalletUtils.isCouponWalletCoupon(couponCode)) {
			showGoogleWalletCouponLoadingThrobber();
		}
		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_APPLY, couponCode);
	}

	@Override
	public void onCancelApplyCoupon() {
		mHotelBookingFragment.cancelDownload(HotelBookingState.COUPON_APPLY);
	}

	private void updateViews() {
		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (Db.getTripBucket().getHotel() != null && Db.getTripBucket().getHotel().isCouponApplied()) {
			Rate rate = Db.getTripBucket().getHotel().getCouponRate();

			// Show off the savings!
			mCouponSavedTextView.setText(getString(R.string.coupon_saved_TEMPLATE, rate
					.getTotalPriceAdjustments().getFormattedMoney()));
		}
	}

	private void updateViewVisibilities() {
		// Show/hide either the coupon button or the coupon applied layout
		View couponShow;
		if (Db.getTripBucket().getHotel() != null && Db.getTripBucket().getHotel().isCouponApplied()) {
			couponShow = mCouponAppliedContainer;
			mCouponTextButton.setVisibility(View.GONE);
		}
		else {
			couponShow = mCouponTextButton;
			mCouponAppliedContainer.setVisibility(View.GONE);
		}
		couponShow.setVisibility(View.VISIBLE);
	}

	private void dismissDialogs() {
		mCouponRemoveThrobberDialog = Ui.findSupportFragment(this, COUPON_REMOVE_THROBBER);
		if (mCouponRemoveThrobberDialog != null && mCouponRemoveThrobberDialog.isAdded()) {
			mCouponRemoveThrobberDialog.dismiss();
		}
		mGoogleWalletCouponApplyThrobber = Ui.findSupportFragment(this, GW_LOADING_THROBBER);
		if (mGoogleWalletCouponApplyThrobber != null && mGoogleWalletCouponApplyThrobber.isAdded()) {
			mGoogleWalletCouponApplyThrobber.dismiss();
		}
		mCouponDialogFragment = Ui.findChildSupportFragment(this, CouponDialogFragment.TAG);
		if (mCouponDialogFragment != null && mCouponDialogFragment.isAdded()) {
			mCouponDialogFragment.dismiss();
		}
	}

	///////////////////////////////////
	/// Otto Event Subscriptions

	@Subscribe
	public void onCouponApplied(Events.CouponApplyDownloadSuccess event) {
		updateViews();
		updateViewVisibilities();
		dismissDialogs();
		mIsCouponBeingReplaced = false;
	}

	@Subscribe
	public void onCouponRemoved(Events.CouponRemoveDownloadSuccess event) {
		updateViewVisibilities();
		dismissDialogs();
	}

	@Subscribe
	public void onCouponCancel(Events.CouponDownloadCancel event) {
		dismissDialogs();
	}

	@Subscribe
	public void onCouponDownloadError(Events.CouponDownloadError event) {
		// Do something on error
		dismissDialogs();
		mIsCouponBeingReplaced = false;
	}

}
