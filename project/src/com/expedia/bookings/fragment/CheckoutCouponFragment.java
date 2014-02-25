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
import com.expedia.bookings.fragment.HotelBookingFragment.CouponDownloadStatusListener;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.mobiata.android.util.Ui;

public class CheckoutCouponFragment extends LobableFragment implements OnClickListener, CouponDialogFragmentListener,
		CouponDownloadStatusListener {

	private TextView mCouponTextButton;
	private ViewGroup mCouponAppliedContainer;
	private TextView mCouponSavedTextView;
	private View mCouponRemoveView;

	private CouponDialogFragment mCouponDialogFragment;
	private ThrobberDialog mCouponRemoveThrobberDialog;
	private HotelBookingFragment mHotelBookingFragment;

	private CouponStatusListener mCouponStatusListener;

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
		mHotelBookingFragment.addCouponDownloadStatusListener(this);
		mCouponStatusListener = Ui.findFragmentListener(this, CouponStatusListener.class);
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

		mHotelBookingFragment.clearCoupon();
	}

	@Override
	public void onApplyCoupon(String code) {
		mHotelBookingFragment.applyCoupon(code);
	}

	@Override
	public void onCancelApplyCoupon() {
		mHotelBookingFragment.cancelCoupon();
	}

	@Override
	public void onFinishHandleWalletError() {
	}

	@Override
	public void onPostApply(Rate rate) {
		dismissDialogs();
		updateViews();
		updateViewVisibilities();
		mCouponStatusListener.onCouponApplied(rate);
	}

	@Override
	public void onPostRemove(Rate rate) {
		updateViewVisibilities();
		dismissDialogs();
		mCouponStatusListener.onCouponRemoved(rate);
	}

	@Override
	public void onCouponCancel() {
		dismissDialogs();
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
		if (mCouponRemoveThrobberDialog != null && mCouponRemoveThrobberDialog.isAdded()) {
			mCouponRemoveThrobberDialog.dismiss();
		}
		if (mCouponDialogFragment != null && mCouponDialogFragment.isAdded()) {
			mCouponDialogFragment.dismiss();
		}
	}

	public interface CouponStatusListener {
		public void onCouponApplied(Rate rate);

		public void onCouponRemoved(Rate rate);
	}

}
