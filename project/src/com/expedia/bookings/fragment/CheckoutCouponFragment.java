package com.expedia.bookings.fragment;

import android.graphics.Paint;
import android.os.Bundle;
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
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

public class CheckoutCouponFragment extends LobableFragment implements OnClickListener, CouponDialogFragmentListener {

	private TextView mCouponTextButton;
	private ViewGroup mCouponAppliedContainer;
	private TextView mCouponSavedTextView;
	private View mCouponRemoveView;

	private CouponDialogFragment mCouponDialogFragment;
	private ThrobberDialog mCouponRemoveThrobberDialog;
	private HotelBookingFragment mHotelBookingFragment;

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
		// Register on Otto bus
		Events.register(this);
		updateViews();
		updateViewVisibilities();
	}

	@Override
	public void onPause() {
		super.onPause();
		// UnRegister on Otto bus
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
			mCouponDialogFragment = new CouponDialogFragment();
			mCouponDialogFragment.show(getChildFragmentManager(), CouponDialogFragment.TAG);
			break;
		}
		case R.id.coupon_clear: {
			clearCoupon();
			break;
		}
		}

	}

	private void clearCoupon() {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				mCouponRemoveThrobberDialog = ThrobberDialog.newInstance(getString(R.string.coupon_removing_dialog));
				mCouponRemoveThrobberDialog.setCancelable(false);
				mCouponRemoveThrobberDialog.show(getFragmentManager(), ThrobberDialog.TAG);
			}
		});

		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_REMOVE);
	}

	@Override
	public void onApplyCoupon(String couponCode) {
		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_APPLY, couponCode);
	}

	@Override
	public void onCancelApplyCoupon() {
		mHotelBookingFragment.cancelDownload(HotelBookingState.COUPON_APPLY);
	}

	private void updateViews() {
		Rate rate = Db.getHotelSearch().getSelectedRate();
		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (Db.getHotelSearch().isCouponApplied()) {
			rate = Db.getHotelSearch().getCouponRate();

			// Show off the savings!
			mCouponSavedTextView.setText(getString(R.string.coupon_saved_TEMPLATE, rate
					.getTotalPriceAdjustments().getFormattedMoney()));
		}
	}

	private void updateViewVisibilities() {
		// Show/hide either the coupon button or the coupon applied layout
		View couponShow;
		if (Db.getHotelSearch().isCouponApplied()) {
			couponShow = mCouponAppliedContainer;
			mCouponTextButton.setVisibility(View.GONE);
		}
		else {
			couponShow = mCouponTextButton;
			mCouponAppliedContainer.setVisibility(View.GONE);
		}
		if (Db.getHotelSearch().getSelectedProperty().isMerchant()) {
			couponShow.setVisibility(View.VISIBLE);
		}
		else {
			mCouponTextButton.setVisibility(View.GONE);
			mCouponAppliedContainer.setVisibility(View.GONE);
		}
	}

	private void dismissDialogs() {
		mCouponRemoveThrobberDialog = Ui.findSupportFragment(this, ThrobberDialog.TAG);
		if (mCouponRemoveThrobberDialog != null && mCouponRemoveThrobberDialog.isAdded()) {
			mCouponRemoveThrobberDialog.dismiss();
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
		dismissDialogs();
		updateViews();
		updateViewVisibilities();
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
		dismissDialogs();
		// Do something on error
	}

}
